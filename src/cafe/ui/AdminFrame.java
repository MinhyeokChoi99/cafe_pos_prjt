package cafe.ui;

import cafe.dao.CafeDAO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

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
        
        // salesArea에 금일 매출 전일 매출 집어넣기 + 금일 가장 잘 팔린 메뉴 확인하기(판매수량 기준)
        JTextArea salesArea = new JTextArea("[오늘 vs 어제 매출 정산] 버튼을 누르면 매출이 조회됩니다.");
        salesArea.setEditable(false);
        salesArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JButton btnCalcSales = new JButton("오늘 vs 어제 매출 정산");
        btnCalcSales.addActionListener(e -> {
            String today     = LocalDate.now().toString();
            String yesterday = LocalDate.now().minusDays(1).toString();

            int todaySales     = dao.getSalesByDate(today);
            int yesterdaySales = dao.getSalesByDate(yesterday);
            int diff           = todaySales - yesterdaySales;

            salesArea.setText(String.format(
                    "정산 기준일: %s\n\n어제 총 매출: %,d원\n오늘 현재 매출: %,d원\n%s\n매출 변동: %s %,d원",
                    today, yesterdaySales, todaySales,
                    "-".repeat(30),
                    diff >= 0 ? "증가" : "감소", Math.abs(diff)
            ));
        });

        panel.add(new JScrollPane(salesArea), BorderLayout.CENTER);
        panel.add(btnCalcSales, BorderLayout.SOUTH);
        return panel;
    }

    /** 재고 수정 패널 */
    private JPanel buildStockPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("재고 수정"));

        JTextField menuNameField = new JTextField(12);
        JSpinner stockSpinner    = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        JButton btnUpdate        = new JButton("재고 수정");

        btnUpdate.addActionListener(e -> {
            String name = menuNameField.getText().trim();
            int stock   = (int) stockSpinner.getValue();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "메뉴명을 입력하세요.");
                return;
            }

            if (dao.updateStockByMenuName(name, stock)) {
                JOptionPane.showMessageDialog(this, "[" + name + "] 재고가 " + stock + "개로 수정되었습니다.");
                mainFrame.loadMenuButtons(); // 메뉴판 즉시 갱신
            } else {
                JOptionPane.showMessageDialog(this, "존재하지 않는 메뉴명입니다.");
            }
        });

        panel.add(new JLabel("메뉴명:"));
        panel.add(menuNameField);
        panel.add(new JLabel("재고:"));
        panel.add(stockSpinner);
        panel.add(btnUpdate);
        return panel;
    }
}
