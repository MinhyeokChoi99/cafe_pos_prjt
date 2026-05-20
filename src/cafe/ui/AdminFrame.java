package cafe.ui;

import cafe.dao.CafeDAO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * 관리자 모드 다이얼로그.
 * - 오늘 vs 어제 매출 비교
 * - 메뉴 재고 수정
 */
public class AdminFrame extends JDialog {
    private final CafeDAO dao;
    private final MainFrame mainFrame;

    public AdminFrame(MainFrame owner, CafeDAO dao) {
        super(owner, "관리자 모드", true);
        this.dao = dao;
        this.mainFrame = owner;

        setSize(520, 420);
        setLocationRelativeTo(owner);
        setLayout(new GridLayout(3, 1, 10, 10));

        add(buildSalesPanel());
        add(buildStockPanel());

        JButton btnClose = new JButton("관리자 모드 종료");
        btnClose.addActionListener(e -> dispose());
        add(btnClose);
    }

    /** 매출 비교 패널 */
    private JPanel buildSalesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("매출 비교"));

        JTextArea salesArea = new JTextArea("[ 통합 정산 보기 ] 버튼을 누르면 매출이 조회됩니다.");
        salesArea.setEditable(false);
        salesArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JButton btnCalcSales = new JButton("통합 정산 보기");
        btnCalcSales.addActionListener(e -> {
            String today = LocalDate.now().toString();
            String yesterday = LocalDate.now().minusDays(1).toString();

            int todaySales = dao.getSalesByDate(today);
            int yesterdaySales = dao.getSalesByDate(yesterday);
            int diff = todaySales - yesterdaySales;

            int weeklySales = dao.getWeeklySales();
            String bestDay = dao.getBestDayThisWeek();
            List<String> topMenus = dao.getTopMenusToday();

            StringBuilder sb = new StringBuilder();
            sb.append("[ 오늘 vs 어제 매출 ]\n");
            sb.append(String.format("어제: %,d원%n오늘: %,d원%n", yesterdaySales, todaySales));
            sb.append(String.format("매출 변동: %s %,d원%n", diff >= 0 ? "증가" : "감소", Math.abs(diff)));

            sb.append("\n──────────────────────────────\n");
            sb.append("[ 이번 주 통계 ]\n");
            sb.append(String.format("이번 주 누적 매출: %,d원%n", weeklySales));
            sb.append("이번 주 최고 매출일: ").append(bestDay).append("\n");

            sb.append("\n──────────────────────────────\n");
            sb.append("[ 오늘 판매량 TOP 3 ]\n");
            if (topMenus.isEmpty()) {
                sb.append("오늘 판매 데이터가 없습니다.\n");
            } else {
                for (String menu : topMenus)
                    sb.append(menu).append("\n");
            }

            salesArea.setText(sb.toString());
        });

        panel.add(new JScrollPane(salesArea), BorderLayout.CENTER);
        panel.add(btnCalcSales, BorderLayout.SOUTH);
        return panel;
    }

    /** 재고 수정 패널 */
    private JPanel buildStockPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("재고 수정"));

        JComboBox<String> menuComboBox = new JComboBox<>();
        JSpinner stockSpinner    = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        JButton btnUpdate        = new JButton("재고 수정");
        
        for(String menu : dao.getMenuNames()) {
        	menuComboBox.addItem(menu);
        }

        
        
        btnUpdate.addActionListener(e -> {
            String name = (String) menuComboBox.getSelectedItem();
            int stock   = (int) stockSpinner.getValue();
            stockSpinner.setValue(0);

           
            

            if (dao.updateStockByMenuName(name, stock)) {
                JOptionPane.showMessageDialog(this, "[" + name + "] 재고가 " + stock + "개로 수정되었습니다.");
                mainFrame.loadMenuButtons(); // 메뉴판 즉시 갱신
            }
        });

        panel.add(new JLabel("메뉴명:"));
        panel.add(menuComboBox);
        panel.add(new JLabel("재고:"));
        panel.add(stockSpinner);
        panel.add(btnUpdate);
        return panel;
    }
}
