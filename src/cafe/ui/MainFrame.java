package cafe.ui;

import cafe.dao.CafeDAO;
import cafe.model.BasketItem;
import cafe.model.ProductDTO;
import cafe.service.BasketService;
import cafe.service.MemberService;
import cafe.service.PaymentService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * 카페 POS 메인 화면.
 * UI 표시와 사용자 이벤트 처리만 담당하며,
 * 비즈니스 로직은 각 Service에 위임합니다.
 */
public class MainFrame extends JFrame {
    // ── 공유 DAO (service들과 같은 인스턴스 사용) ──────────────────────────
    private final CafeDAO       dao            = new CafeDAO();

    // ── Service ───────────────────────────────────────────────────────────
    private final BasketService  basketService  = new BasketService();
    private final MemberService  memberService  = new MemberService(dao);
    private final PaymentService paymentService = new PaymentService(dao);

    // ── UI 컴포넌트 ────────────────────────────────────────────────────────
    private JPanel   menuPanel;
    private JTextArea basketArea;
    private JLabel   totalLabel;
    private JTextField phoneField;

    public MainFrame() {
        setTitle("카페 POS 시스템");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildMenuPanel(),   BorderLayout.WEST);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildSouthPanel(),  BorderLayout.SOUTH);

        setVisible(true);
    }

    // ── 패널 빌더 ─────────────────────────────────────────────────────────

    private JPanel buildMenuPanel() {
        menuPanel = new JPanel();
        menuPanel.setBorder(BorderFactory.createTitledBorder("메뉴판"));
        menuPanel.setPreferredSize(new Dimension(340, 0));
        loadMenuButtons();
        return menuPanel;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("장바구니 / 회원 정보"));

        basketArea = new JTextArea();
        basketArea.setEditable(false);
        basketArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        panel.add(new JScrollPane(basketArea), BorderLayout.CENTER);
        panel.add(buildMemberPanel(), BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildMemberPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        phoneField = new JTextField(13);
        JButton btnMemberCheck  = new JButton("회원조회");
        JButton btnOrderHistory = new JButton("주문내역");
        JButton btnAdminMode    = new JButton("관리자 모드");

        btnMemberCheck.addActionListener(e  -> handleMemberCheck());
        btnOrderHistory.addActionListener(e -> openOrderHistoryDialog());
        btnAdminMode.addActionListener(e    -> openAdminMode());

        panel.add(new JLabel("전화번호:"));
        panel.add(phoneField);
        panel.add(btnMemberCheck);
        panel.add(btnOrderHistory);
        panel.add(btnAdminMode);
        return panel;
    }

    private JPanel buildSouthPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        totalLabel = new JLabel("총 결제 금액: 0원", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        panel.add(totalLabel, BorderLayout.NORTH);

        JPanel btnGroup = new JPanel(new GridLayout(1, 2, 10, 10));
        JButton btnCancel = new JButton("전체 취소");
        JButton btnPay    = new JButton("결제하기");
        btnPay.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        btnPay.setBackground(Color.ORANGE);

        btnCancel.addActionListener(e -> handleCancel());
        btnPay.addActionListener(e    -> handlePayment());

        btnGroup.add(btnCancel);
        btnGroup.add(btnPay);
        panel.add(btnGroup, BorderLayout.CENTER);
        return panel;
    }

    // ── 메뉴 버튼 로드 (재고 변경 후에도 호출됨) ─────────────────────────

    public void loadMenuButtons() {
        menuPanel.removeAll();

        ArrayList<ProductDTO> prodList = dao.getProductList();
        menuPanel.setLayout(new GridLayout(Math.max(prodList.size(), 1), 1, 10, 10));

        for (ProductDTO prod : prodList) {
            JButton btn = new JButton();
            btn.setFont(new Font("맑은 고딕", Font.BOLD, 14));

            if (prod.isSoldOut()) {
                btn.setText(String.format("%s [품절]", prod.name));
                btn.setEnabled(false);
                btn.setBackground(new Color(220, 120, 120));
            } else {
                btn.setText(String.format("%s %,d원 / 재고 %d", prod.name, prod.price, prod.stock));
                btn.addActionListener(e -> showOrderDialog(prod));
            }
            menuPanel.add(btn);
        }

        menuPanel.revalidate();
        menuPanel.repaint();
    }

    // ── 주문 다이얼로그 ───────────────────────────────────────────────────

    private void showOrderDialog(ProductDTO prod) {
        JDialog dialog = new JDialog(this, prod.name + " 주문", true);
        dialog.setSize(320, 260);
        dialog.setLayout(new GridLayout(4, 1, 5, 5));
        dialog.setLocationRelativeTo(this);

        // 온도 선택
        JPanel tempPanel = new JPanel();
        JRadioButton rbHot = new JRadioButton("HOT");
        JRadioButton rbIce = new JRadioButton("ICE");
        ButtonGroup group  = new ButtonGroup();
        group.add(rbHot);
        group.add(rbIce);

        if      (prod.temperatureType == 1) { rbHot.setSelected(true);  rbIce.setEnabled(false); }
        else if (prod.temperatureType == 2) { rbIce.setSelected(true);  rbHot.setEnabled(false); }
        else                                { rbIce.setSelected(true); }

        tempPanel.add(new JLabel("온도:"));
        tempPanel.add(rbHot);
        tempPanel.add(rbIce);

        // 수량 선택
        JPanel qtyPanel   = new JPanel();
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, prod.stock, 1));
        qtyPanel.add(new JLabel("수량:"));
        qtyPanel.add(qtySpinner);

        JLabel priceLabel  = new JLabel(String.format("단가: %,d원", prod.price), SwingConstants.CENTER);
        JButton btnConfirm = new JButton("장바구니 담기");

        btnConfirm.addActionListener(e -> {
            String temperature = rbHot.isSelected() ? "HOT" : "ICE";
            int quantity       = (int) qtySpinner.getValue();
            basketService.addItem(prod, temperature, quantity);
            refreshBasket();
            dialog.dispose();
        });

        dialog.add(tempPanel);
        dialog.add(qtyPanel);
        dialog.add(priceLabel);
        dialog.add(btnConfirm);
        dialog.setVisible(true);
    }

    // ── 이벤트 핸들러 ─────────────────────────────────────────────────────

    private void handleCancel() {
        if (basketService.isEmpty()) return;
        basketService.clear();
        refreshBasket();
        JOptionPane.showMessageDialog(this, "장바구니가 초기화되었습니다.");
    }

    private void handleMemberCheck() {
        String phone = phoneField.getText().replace(" ", "").replace("-", ""); 
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "전화번호를 입력해 주세요.");
            return;
        }

        int stamp = memberService.checkMember(phone);

        if (stamp != -1) {
            memberService.setCurrentMember(phone, stamp);
            JOptionPane.showMessageDialog(this, "회원 확인 완료\n현재 스탬프: " + stamp + "개");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "등록되지 않은 번호입니다. 신규 회원으로 등록하시겠습니까?",
                "회원 등록",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String name = JOptionPane.showInputDialog(this, "고객 이름을 입력하세요:");
            if (name != null && !name.trim().isEmpty()) {
                boolean success = memberService.registerMember(phone, name.trim());
                JOptionPane.showMessageDialog(this, success ? "회원 등록 완료" : "회원 등록 실패");
            }
        }
    }

    private void handlePayment() {
        if (basketService.isEmpty()) {
            JOptionPane.showMessageDialog(this, "장바구니가 비어 있습니다.");
            return;
        }

        int     totalSum      = basketService.calcTotal();
        boolean useDiscount   = false;
        int     stampToDeduct = 0;

        // 스탬프 할인 여부 확인
        if (memberService.canUseStampDiscount()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "스탬프 10개를 사용하여 2,000원 할인받겠습니까?",
                    "스탬프 할인",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                useDiscount   = true;
                stampToDeduct = PaymentService.STAMP_DISCOUNT_THRESHOLD;
            }
        }

        int finalPrice = paymentService.calcFinalPrice(totalSum, useDiscount);

        boolean success = paymentService.pay(
                memberService.getCurrentPhone(),
                basketService.getItems(),
                stampToDeduct,
                memberService.getCurrentStamp(),
                finalPrice
        );

        if (success) {
            String msg = "주문이 정상 처리되었습니다.\n";
            if (useDiscount) msg += "스탬프 할인 2,000원이 적용되었습니다.\n";
            msg += String.format("최종 결제 금액: %,d원", finalPrice);

            JOptionPane.showMessageDialog(this, msg);
            basketService.clear();
            refreshBasket();
            phoneField.setText("");
            memberService.clearCurrentMember();
            loadMenuButtons();
        } else {
            JOptionPane.showMessageDialog(this, "결제 처리 실패. DB 연결 또는 재고를 확인하세요.");
        }
    }

    // ── 다이얼로그 열기 ───────────────────────────────────────────────────

    private void openOrderHistoryDialog() {
        JDialog dialog = new JDialog(this, "주문 내역", true);
        dialog.setSize(620, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JTextArea historyArea = new JTextArea(dao.getOrderHistoryText());
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.BOLD, 13));
//        historyArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14)); 폰트 변경



        JButton btnClose = new JButton("닫기");
        btnClose.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnClose);

        dialog.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void openAdminMode() {
        String pw = JOptionPane.showInputDialog(this, "관리자 비밀번호를 입력하세요:");
        if (pw == null) return;

        if (pw.equals("1234")) {
            new AdminFrame(this, dao).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "비밀번호가 일치하지 않습니다.");
        }
    }

    // ── 장바구니 화면 갱신 ────────────────────────────────────────────────

    private void refreshBasket() {
        basketArea.setText("");
        for (BasketItem item : basketService.getItems()) {
            basketArea.append(String.format(
                    "• %s / %s / %d잔 / %,d원 x %d = %,d원\n",
                    item.menuName, item.temperature,
                    item.quantity, item.unitPrice,
                    item.quantity, item.getTotalPrice()
            ));
        }
        totalLabel.setText(String.format("총 결제 금액: %,d원", basketService.calcTotal()));
    }

    // ── 진입점 ────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
