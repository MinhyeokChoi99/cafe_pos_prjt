package cafe.dao;

import cafe.model.BasketItem;
import cafe.model.ProductDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CafeDAO {
    private final String url = "jdbc:mysql://localhost:3306/cafe_pos";
    private final String user = "root";
    private final String password = "root";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    // 회원 조회: 회원이면 스탬프 수 반환, 없으면 -1 반환
    public int checkMemberStamp(String phone) {
        String sql = "SELECT stamp_cnt FROM member WHERE mem_phone = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("stamp_cnt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    // 신규 회원 등록
    public boolean registerMember(String phone, String name) {
        String sql = "INSERT INTO member (mem_phone, mem_name, stamp_cnt) VALUES (?, ?, 0)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            pstmt.setString(2, name);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 메뉴 목록 조회
    public ArrayList<ProductDTO> getProductList() {
        ArrayList<ProductDTO> list = new ArrayList<>();
        String sql = """
                SELECT prod_id, prod_name, prod_price, is_coffee, temperature_type, stock
                FROM product
                ORDER BY prod_id
                """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new ProductDTO(
                        rs.getInt("prod_id"),
                        rs.getString("prod_name"),
                        rs.getInt("prod_price"),
                        rs.getInt("is_coffee") == 1,
                        rs.getInt("temperature_type"),
                        rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // 메뉴 추가
    public boolean addNewProduct(String name, int price, boolean isCoffee, int temperatureType, int stock) {
        String sql = """
                INSERT INTO product (prod_name, prod_price, is_coffee, temperature_type, stock)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, price);
            pstmt.setInt(3, isCoffee ? 1 : 0);
            pstmt.setInt(4, temperatureType);
            pstmt.setInt(5, stock);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 재고 수량 직접 수정
    public boolean updateStockByMenuName(String menuName, int stock) {
        String sql = "UPDATE product SET stock = ? WHERE prod_name = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, stock);
            pstmt.setString(2, menuName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 결제 처리: orders 1건 생성 → order_item 여러 건 생성 → 재고 차감 → 회원 스탬프 갱신
    public boolean processPayment(String phone,
                                  ArrayList<BasketItem> basketList,
                                  int stampToDeduct,
                                  int currentStamp,
                                  int finalPaySum) {
        String insertOrderSql  = "INSERT INTO orders (mem_phone, total_price) VALUES (?, ?)";
        String insertItemSql   = "INSERT INTO order_item (order_id, prod_id, temperature, quantity, unit_price) VALUES (?, ?, ?, ?, ?)";
        String decreaseStockSql = "UPDATE product SET stock = stock - ? WHERE prod_id = ? AND stock >= ?";
        String updateMemberSql = "UPDATE member SET stamp_cnt = ? WHERE mem_phone = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 주문 생성
            int orderId;
            try (PreparedStatement pstmt = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                if (phone == null) pstmt.setNull(1, Types.VARCHAR);
                else               pstmt.setString(1, phone);
                pstmt.setInt(2, finalPaySum);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("주문번호 생성에 실패했습니다.");
                    orderId = rs.getInt(1);
                }
            }

            // 주문 아이템 삽입 + 재고 차감
            try (PreparedStatement itemPstmt  = conn.prepareStatement(insertItemSql);
                 PreparedStatement stockPstmt = conn.prepareStatement(decreaseStockSql)) {

                for (BasketItem item : basketList) {
                    itemPstmt.setInt(1, orderId);
                    itemPstmt.setInt(2, item.prodId);
                    itemPstmt.setString(3, item.temperature);
                    itemPstmt.setInt(4, item.quantity);
                    itemPstmt.setInt(5, item.unitPrice);
                    itemPstmt.addBatch();

                    stockPstmt.setInt(1, item.quantity);
                    stockPstmt.setInt(2, item.prodId);
                    stockPstmt.setInt(3, item.quantity);
                    if (stockPstmt.executeUpdate() == 0) {
                        throw new SQLException("재고 부족: " + item.menuName);
                    }
                }
                itemPstmt.executeBatch();
            }

            // 회원 스탬프 갱신
            if (phone != null) {
                int orderedCount = basketList.stream().mapToInt(i -> i.quantity).sum();
                int newStamp = Math.max(0, currentStamp - stampToDeduct + orderedCount);

                try (PreparedStatement pstmt = conn.prepareStatement(updateMemberSql)) {
                    pstmt.setInt(1, newStamp);
                    pstmt.setString(2, phone);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            return false;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); } }
        }
    }

    // 특정 날짜 총매출 조회 (형식: yyyy-MM-dd)
    public int getSalesByDate(String dateStr) {
        String sql = "SELECT COALESCE(SUM(total_price), 0) FROM orders WHERE DATE(order_date) = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dateStr);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 전체 주문 내역 조회
    public String getOrderHistoryText() {
        String sql = """
                SELECT
                    o.order_id, o.order_date, o.total_price, o.mem_phone,
                    m.mem_name, p.prod_name,
                    oi.temperature, oi.quantity, oi.unit_price,
                    (oi.quantity * oi.unit_price) AS item_total
                FROM orders o
                LEFT JOIN member m    ON o.mem_phone = m.mem_phone
                JOIN order_item oi    ON o.order_id  = oi.order_id
                JOIN product p        ON oi.prod_id  = p.prod_id
                ORDER BY o.order_id DESC, oi.order_item_id
                """;

        StringBuilder sb = new StringBuilder();
        int currentOrderId = -1;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int orderId = rs.getInt("order_id");

                if (orderId != currentOrderId) {
                    currentOrderId = orderId;
                    if (sb.length() > 0) sb.append("\n");

                    String phone      = rs.getString("mem_phone");
                    String memberName = rs.getString("mem_name");
                    String customerText = (phone == null) ? "비회원"
                            : (memberName == null) ? phone
                            : memberName + " / " + phone;
                    
                    sb.append("--------------------------------------\n");
                    sb.append(String.format("[주문번호 %d] %s\n고객: %s\n총액: %,d원\n",
                            orderId, rs.getTimestamp("order_date"),
                            customerText, rs.getInt("total_price")));
                }

                sb.append(String.format("  - %s / %s / %d잔 / %,d원 x %d = %,d원\n",
                        rs.getString("prod_name"), rs.getString("temperature"),
                        rs.getInt("quantity"), rs.getInt("unit_price"),
                        rs.getInt("quantity"), rs.getInt("item_total")));
            }

            return sb.length() == 0 ? "조회된 주문 내역이 없습니다." : sb.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return "주문 내역 조회 중 오류가 발생했습니다. DB 상태를 확인하세요.";
        }
    }
    
    // 특정 날짜 주문 내역 조회 (형식: yyyy-MM-dd)
    public String getOrderHistoryText(String dateStr) {
        String sql = """
                SELECT
                    o.order_id, o.order_date, o.total_price, o.mem_phone,
                    m.mem_name, p.prod_name,
                    oi.temperature, oi.quantity, oi.unit_price,
                    (oi.quantity * oi.unit_price) AS item_total
                FROM orders o
                LEFT JOIN member m    ON o.mem_phone = m.mem_phone
                JOIN order_item oi    ON o.order_id  = oi.order_id
                JOIN product p        ON oi.prod_id  = p.prod_id
                WHERE DATE(o.order_date) = ?
                ORDER BY o.order_id DESC, oi.order_item_id
                """;

        StringBuilder sb = new StringBuilder();
        int currentOrderId = -1;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dateStr);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");

                    if (orderId != currentOrderId) {
                        currentOrderId = orderId;
                        if (sb.length() > 0) sb.append("\n");

                        String phone      = rs.getString("mem_phone");
                        String memberName = rs.getString("mem_name");
                        String customerText = (phone == null) ? "비회원"
                                : (memberName == null) ? phone
                                : memberName + " / " + phone;

                        sb.append("--------------------------------------\n");
                        sb.append(String.format("[주문번호 %d] %s\n고객: %s\n총액: %,d원\n",
                                orderId, rs.getTimestamp("order_date"),
                                customerText, rs.getInt("total_price")));
                    }

                    sb.append(String.format("  - %s / %s / %d잔 / %,d원 x %d = %,d원\n",
                            rs.getString("prod_name"), rs.getString("temperature"),
                            rs.getInt("quantity"), rs.getInt("unit_price"),
                            rs.getInt("quantity"), rs.getInt("item_total")));
                }
            }

            return sb.length() == 0 ? dateStr + "의 주문 내역이 없습니다." : sb.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return "주문 내역 조회 중 오류가 발생했습니다.";
        }
    }

    public List<String> getMenuNames() {
        List<String> menuNames = new ArrayList<>();

        String sql = "SELECT prod_name FROM product ORDER BY prod_name";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()
        ) {
            while (rs.next()) {
                menuNames.add(rs.getString("prod_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return menuNames;
    }

    // 오늘 판매량 TOP 3 메뉴
    public List<String> getTopMenusToday() {
        List<String> result = new ArrayList<>();
        String sql = """
                SELECT p.prod_name, SUM(oi.quantity) AS total_qty
                FROM order_item oi
                JOIN orders o  ON oi.order_id = o.order_id
                JOIN product p ON oi.prod_id  = p.prod_id
                WHERE DATE(o.order_date) = CURDATE()
                GROUP BY p.prod_name
                ORDER BY total_qty DESC
                LIMIT 3
                """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            int rank = 1;
            while (rs.next()) {
                result.add(rank + "위: " + rs.getString("prod_name")
                        + " (" + rs.getInt("total_qty") + "잔)");
                rank++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // 이번 주 누적 매출
    public int getWeeklySales() {
        String sql = """
                SELECT COALESCE(SUM(total_price), 0)
                FROM orders
                WHERE YEARWEEK(order_date, 1) = YEARWEEK(NOW(), 1)
                """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 이번 주 최고 매출일
    public String getBestDayThisWeek() {
        String sql = """
                SELECT DATE(order_date) AS day, SUM(total_price) AS daily_total
                FROM orders
                WHERE YEARWEEK(order_date, 1) = YEARWEEK(NOW(), 1)
                GROUP BY DATE(order_date)
                ORDER BY daily_total DESC
                LIMIT 1
                """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getString("day")
                        + " (" + String.format("%,d", rs.getInt("daily_total")) + "원)";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "데이터 없음";
    }

    // 시간대별 주문 건수 조회 (아침 06~11시 / 점심 11~17시 / 저녁 17~22시)
    public int[] getOrderCountByTimeSlot(String dateStr) {
        String sql = "SELECT " +
                "SUM(CASE WHEN HOUR(order_date) >= 6  AND HOUR(order_date) < 11 THEN 1 ELSE 0 END) AS morning, " +
                "SUM(CASE WHEN HOUR(order_date) >= 11 AND HOUR(order_date) < 17 THEN 1 ELSE 0 END) AS lunch, " +
                "SUM(CASE WHEN HOUR(order_date) >= 17 AND HOUR(order_date) < 22 THEN 1 ELSE 0 END) AS evening " +
                "FROM orders WHERE DATE(order_date) = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dateStr);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new int[]{ rs.getInt("morning"), rs.getInt("lunch"), rs.getInt("evening") };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{0, 0, 0};
    }

    // 월별 매출 조회 (특정 연도의 1~12월 매출 합계)
    public int[] getMonthlySales(int year) {
        String sql = "SELECT MONTH(order_date) AS month, COALESCE(SUM(total_price), 0) AS total " +
                "FROM orders WHERE YEAR(order_date) = ? GROUP BY MONTH(order_date)";

        int[] result = new int[12];
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int month = rs.getInt("month");
                    result[month - 1] = rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
