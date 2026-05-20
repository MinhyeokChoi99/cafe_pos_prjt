package cafe.ui;

import cafe.dao.CafeDAO;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.JFormattedTextField;
import java.awt.*;
import java.time.LocalDate;

/**
 * 관리자 모드 다이얼로그.
 * - 시간대별 주문 건수 (아침 / 점심 / 저녁)
 * - 월별 매출 추이
 * - 메뉴 재고 수정
 */
public class AdminFrame extends JDialog {
    private final CafeDAO dao;
    private final MainFrame mainFrame;

    // 메인 프레임 컬러와 통일
    private final Color COLOR_BG      = new Color(255, 240, 242);
    private final Color COLOR_CARD_BG = Color.WHITE;
    private final Color COLOR_MAIN    = new Color(232, 90, 113);
    private final Color COLOR_BORDER  = new Color(255, 211, 218);
    private final Color COLOR_TEXT    = new Color(60, 60, 60);

    public AdminFrame(MainFrame owner, CafeDAO dao) {
        super(owner, "관리자 모드", true);
        this.dao = dao;
        this.mainFrame = owner;

        setSize(580, 600);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new GridLayout(3, 1, 10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildTimeSlotPanel());
        add(buildMonthlyPanel());
        add(buildStockPanel());
    }

    // ── 시간대별 주문 건수 패널 ───────────────────────────────────────────

    private JPanel buildTimeSlotPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(COLOR_CARD_BG);
        panel.setBorder(styledBorder("시간대별 주문 건수"));

        // 날짜 선택 + 조회 버튼
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topRow.setBackground(COLOR_CARD_BG);

        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(java.util.Date.from(
                LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        dateSpinner.setPreferredSize(new Dimension(130, 28));

        // 위아래 버튼을 누를 때 항상 일(dd) 단위로만 증감하도록
        // 스피너 내부 버튼에 직접 ActionListener를 달아 Calendar로 제어합니다.
        for (java.awt.Component comp : dateSpinner.getComponents()) {
            if (comp instanceof javax.swing.plaf.basic.BasicArrowButton) {
                javax.swing.plaf.basic.BasicArrowButton arrow =
                        (javax.swing.plaf.basic.BasicArrowButton) comp;
                // 기존 리스너 제거
                for (java.awt.event.ActionListener al : arrow.getActionListeners()) {
                    arrow.removeActionListener(al);
                }
                arrow.addActionListener(e -> {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime((java.util.Date) dateSpinner.getValue());
                    int direction = (arrow.getDirection() == javax.swing.SwingConstants.NORTH) ? 1 : -1;
                    cal.add(java.util.Calendar.DAY_OF_MONTH, direction);
                    dateSpinner.setValue(cal.getTime());
                });
            }
        }

        JButton btnQuery = styledButton("조회");

        topRow.add(new JLabel("날짜:"));
        topRow.add(dateSpinner);
        topRow.add(btnQuery);

        // 결과 표시: 막대 그래프 스타일 패널
        JPanel resultPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        resultPanel.setBackground(COLOR_CARD_BG);
        resultPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JLabel[] barLabels   = new JLabel[3];
        JLabel[] countLabels = new JLabel[3];
        String[] slotNames   = {"☀ 아침\n06:00~11:00", "☀ 점심\n11:00~17:00", "🌙 저녁\n17:00~22:00"};
        Color[]  barColors   = {new Color(255, 183, 77), new Color(232, 90, 113), new Color(100, 149, 237)};

        for (int i = 0; i < 3; i++) {
            JPanel slotCard = new JPanel(new BorderLayout(4, 4));
            slotCard.setBackground(COLOR_CARD_BG);
            slotCard.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1, true));

            // 막대 (높이로 비율 표현)
            barLabels[i] = new JLabel("", SwingConstants.CENTER);
            barLabels[i].setOpaque(true);
            barLabels[i].setBackground(barColors[i]);
            barLabels[i].setPreferredSize(new Dimension(0, 0)); // 초기 높이 0

            JPanel barWrapper = new JPanel(new BorderLayout());
            barWrapper.setBackground(new Color(245, 245, 245));
            barWrapper.setBorder(BorderFactory.createEmptyBorder(4, 10, 0, 10));
            barWrapper.add(barLabels[i], BorderLayout.SOUTH);

            countLabels[i] = new JLabel("- 건", SwingConstants.CENTER);
            countLabels[i].setFont(new Font("맑은 고딕", Font.BOLD, 14));
            countLabels[i].setForeground(COLOR_MAIN);

            String[] parts = slotNames[i].split("\n");
            JPanel namePanel = new JPanel(new GridLayout(2, 1));
            namePanel.setBackground(COLOR_CARD_BG);
            JLabel l1 = new JLabel(parts[0], SwingConstants.CENTER);
            JLabel l2 = new JLabel(parts[1], SwingConstants.CENTER);
            l1.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            l2.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
            l2.setForeground(Color.GRAY);
            namePanel.add(l1);
            namePanel.add(l2);

            slotCard.add(barWrapper,   BorderLayout.CENTER);
            slotCard.add(countLabels[i], BorderLayout.NORTH);
            slotCard.add(namePanel,    BorderLayout.SOUTH);
            resultPanel.add(slotCard);
        }

        btnQuery.addActionListener(e -> {
            // 날짜 포맷 추출
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String dateStr = sdf.format(dateSpinner.getValue());

            int[] counts = dao.getOrderCountByTimeSlot(dateStr);
            int max = Math.max(1, Math.max(counts[0], Math.max(counts[1], counts[2])));

            for (int i = 0; i < 3; i++) {
                countLabels[i].setText(counts[i] + " 건");
                // 막대 높이: 최대 50px 기준 비율 계산
                int barH = (int) ((counts[i] / (double) max) * 50);
                barLabels[i].setPreferredSize(new Dimension(0, Math.max(barH, 2)));
                barLabels[i].getParent().revalidate();
                barLabels[i].getParent().repaint();
            }
        });

        panel.add(topRow,      BorderLayout.NORTH);
        panel.add(resultPanel, BorderLayout.CENTER);
        return panel;
    }

    // ── 월별 매출 추이 패널 ───────────────────────────────────────────────

    private JPanel buildMonthlyPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(COLOR_CARD_BG);
        panel.setBorder(styledBorder("월별 매출 추이"));

        // 연도 선택 + 조회 버튼
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topRow.setBackground(COLOR_CARD_BG);

        int currentYear = LocalDate.now().getYear();
        SpinnerNumberModel yearModel = new SpinnerNumberModel(currentYear, 2000, currentYear + 1, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);
        yearSpinner.setPreferredSize(new Dimension(80, 28));

        JButton btnQuery = styledButton("조회");

        topRow.add(new JLabel("연도:"));
        topRow.add(yearSpinner);
        topRow.add(btnQuery);

        // 결과 텍스트 영역
        JTextArea resultArea = new JTextArea("연도를 선택하고 [조회] 버튼을 누르세요.");
        resultArea.setEditable(false);
        resultArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        resultArea.setBackground(COLOR_CARD_BG);
        resultArea.setForeground(COLOR_TEXT);

        String[] monthNames = {"1월","2월","3월","4월","5월","6월","7월","8월","9월","10월","11월","12월"};

        btnQuery.addActionListener(e -> {
            int year   = (int) yearSpinner.getValue();
            int[] data = dao.getMonthlySales(year);

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("  ── %d년 월별 매출 ──\n\n", year));

            for (int i = 0; i < 12; i++) {
                sb.append(String.format("  %3s │ %,7d원\n", monthNames[i], data[i]));
            }

            resultArea.setText(sb.toString());
        });

        panel.add(topRow,                     BorderLayout.NORTH);
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        return panel;
    }

    // ── 재고 수정 패널 ────────────────────────────────────────────────────

    private JPanel buildStockPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(COLOR_CARD_BG);
        panel.setBorder(styledBorder("재고 수정"));

        JComboBox<String> menuComboBox = new JComboBox<>();
        for (String menu : dao.getMenuNames()) menuComboBox.addItem(menu);

        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        JButton  btnUpdate    = styledButton("재고 수정");
        JButton  btnClose     = styledButton("닫기");

        btnUpdate.addActionListener(e -> {
            String name = (String) menuComboBox.getSelectedItem();
            int stock   = (int) stockSpinner.getValue();
            stockSpinner.setValue(0);

            if (dao.updateStockByMenuName(name, stock)) {
                JOptionPane.showMessageDialog(this, "[" + name + "] 재고가 " + stock + "개로 수정되었습니다.");
                mainFrame.loadMenuButtons();
            }
        });

        btnClose.addActionListener(e -> dispose());

        panel.add(new JLabel("메뉴명:"));
        panel.add(menuComboBox);
        panel.add(new JLabel("재고:"));
        panel.add(stockSpinner);
        panel.add(btnUpdate);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnClose);
        return panel;
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────

    private TitledBorder styledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 2, true), " " + title + " ");
        border.setTitleFont(new Font("맑은 고딕", Font.BOLD, 13));
        border.setTitleColor(COLOR_MAIN);
        return border;
    }

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btn.setBackground(COLOR_MAIN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }
}
