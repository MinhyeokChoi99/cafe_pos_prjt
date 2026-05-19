package cafe.ui;

import cafe.dao.CafeDAO;
import cafe.model.BasketItem;
import cafe.model.ProductDTO;
import cafe.service.BasketService;
import cafe.service.MemberService;
import cafe.service.PaymentService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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

    // ── 🎨 바나프레소 감성 커스텀 컬러 정의 ─────────────────────────────────
    private final Color COLOR_BG       = new Color(255, 240, 242); // 메인 연분홍 배경
    private final Color COLOR_CARD_BG  = Color.WHITE;               // 카드 및 텍스트 영역 내부
    private final Color COLOR_MAIN     = new Color(232, 90, 113);  // 포인트 딥 핑크
    private final Color COLOR_BORDER   = new Color(255, 211, 218); // 연핑크 테두리
    private final Color COLOR_TEXT_DARK= new Color(60, 60, 60);    // 다크 그레이 텍스트

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
        getContentPane().setBackground(COLOR_BG); // 메인 전체 배경색 적용

        // 🌟 buildMenuPanel에서 ScrollPane을 반환하므로 그대로 붙여주면 됨!
        add(buildMenuPanel(),   BorderLayout.WEST);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildSouthPanel(),  BorderLayout.SOUTH);

        setVisible(true);
    }

    // ── 패널 빌더 ─────────────────────────────────────────────────────────

    private JComponent buildMenuPanel() {
        menuPanel = new JPanel();
        menuPanel.setBackground(COLOR_BG);
        
        // 🌟 중요: BoxLayout이나 GridBagLayout 대신, 내부 컴포넌트 크기를 자유롭게 조절할 수 있도록 컴포넌트 배치 방식 세팅
        // 여백 관리를 수월하게 하기 위해 빈 보더 배치
        menuPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        loadMenuButtons();

        // 🌟 [핵심 리팩토링] 메뉴판 Panel을 스크롤 창(JScrollPane)으로 감쌉니다!
        JScrollPane menuScrollPane = new JScrollPane(menuPanel);
        menuScrollPane.setPreferredSize(new Dimension(350, 0)); // 가로폭 기존 유지
        menuScrollPane.setBackground(COLOR_BG);
        
        // 마우스 휠 스크롤 속도를 시원시원하고 부드럽게 올리는 마법의 설정 (기본값은 너무 느림!)
        menuScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        menuScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // 가로 스크롤 차단
        
        // 타이틀 테두리는 스크롤 외곽에 이쁘게 씌워주기
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 2, true), " 메뉴판 ");
        border.setTitleFont(new Font("맑은 고딕", Font.BOLD, 14));
        border.setTitleColor(COLOR_MAIN);
        menuScrollPane.setBorder(border);

        return menuScrollPane;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_BG);

        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 2, true), " 장바구니 / 회원 정보 ");
        border.setTitleFont(new Font("맑은 고딕", Font.BOLD, 14));
        border.setTitleColor(COLOR_MAIN);
        panel.setBorder(border);

        basketArea = new JTextArea();
        basketArea.setEditable(false);
        basketArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        basketArea.setBackground(COLOR_CARD_BG);
        basketArea.setForeground(COLOR_TEXT_DARK);
        
        JScrollPane scrollPane = new JScrollPane(basketArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(buildMemberPanel(), BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildMemberPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(COLOR_BG);

        phoneField = new JTextField(13);
        phoneField.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        JButton btnMemberCheck  = createStyledButton("회원조회", false);
        JButton btnOrderHistory = createStyledButton("주문내역", false);
        JButton btnAdminMode    = createStyledButton("관리자 모드", false);

        btnMemberCheck.addActionListener(e  -> handleMemberCheck());
        btnOrderHistory.addActionListener(e -> openOrderHistoryDialog());
        btnAdminMode.addActionListener(e    -> openAdminMode());

        JLabel lblPhone = new JLabel("전화번호:");
        lblPhone.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        lblPhone.setForeground(COLOR_TEXT_DARK);

        panel.add(lblPhone);
        panel.add(phoneField);
        panel.add(btnMemberCheck);
        panel.add(btnOrderHistory);
        panel.add(btnAdminMode);
        return panel;
    }

    private JPanel buildSouthPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        totalLabel = new JLabel("총 결제 금액: 0원", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        totalLabel.setForeground(COLOR_MAIN); // 총 금액을 딥 핑크로 강조
        panel.add(totalLabel, BorderLayout.NORTH);

        JPanel btnGroup = new JPanel(new GridLayout(1, 2, 10, 10));
        btnGroup.setBackground(COLOR_BG);
        
        JButton btnCancel = createStyledButton("전체 취소", false);
        JButton btnPay    = createStyledButton("결제하기", true); // 메인 컬러 강조 버튼

        btnCancel.addActionListener(e -> handleCancel());
        btnPay.addActionListener(e    -> handlePayment());

        btnGroup.add(btnCancel);
        btnGroup.add(btnPay);
        panel.add(btnGroup, BorderLayout.CENTER);
        return panel;
    }

    // ── 메뉴 이미지/정보 카드 로드 (재고 변경 후에도 호출됨) ─────────────────

    public void loadMenuButtons() {
        menuPanel.removeAll();

        ArrayList<ProductDTO> prodList = dao.getProductList();
        
        // 🌟 가로 2열 고정 배치. 메뉴가 추가되어 행이 늘어날 때 컴포넌트가 찌그러지지 않도록 세팅합니다.
        menuPanel.setLayout(new GridLayout(0, 2, 8, 8));

        for (ProductDTO prod : prodList) {
            JPanel itemCard = new JPanel(new BorderLayout(5, 5));
            itemCard.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1, true));
            itemCard.setBackground(COLOR_CARD_BG);
            
            // 🌟 [핵심 설정] 카드의 치수를 무조건 고정해서 그리드가 맘대로 크기를 조절하지 못하게 락(Lock)을 겁니다!
            itemCard.setPreferredSize(new Dimension(150, 190));

            // 1. 음료 이미지용 라벨 세팅 (정사각형 뷰 고정)
            JLabel imgLabel = new JLabel("", SwingConstants.CENTER);
            imgLabel.setPreferredSize(new Dimension(130, 130));

            try {
                String imgPath = "img/" + prod.prodId + ".jpg";
                ImageIcon rawIcon = new ImageIcon(imgPath);
                Image scaledImg = rawIcon.getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH);
                imgLabel.setIcon(new ImageIcon(scaledImg));
            } catch (Exception e) {
                imgLabel.setText("☕ 사진 없음");
                imgLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                imgLabel.setForeground(Color.GRAY);
            }

            // 2. 텍스트 정보 라벨 세팅
            String menuInfo = String.format("<html><center><b style='color:#333333;'>%s</b><br><font color='#E85A71'><b>%,d원</b></font><br><font color='#888888'>재고 %d</font></center></html>",
                    prod.name, prod.price, prod.stock);
            JLabel infoLabel = new JLabel(menuInfo, SwingConstants.CENTER);
            infoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));

            // 3. 품절 여부에 따른 이벤트 처리 및 스타일링
            if (prod.isSoldOut()) {
                imgLabel.setOpaque(true);
                imgLabel.setBackground(new Color(240, 220, 225)); // 품절 시 톤 다운된 핑크그레이
                imgLabel.setText("<html><font color='#CC3344'><b>[품절]</b></font></html>");
                itemCard.setBackground(new Color(250, 245, 245));
            } else {
                imgLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                imgLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        showOrderDialog(prod);
                    }
                });
            }

            itemCard.add(imgLabel, BorderLayout.CENTER);
            itemCard.add(infoLabel, BorderLayout.SOUTH);
            menuPanel.add(itemCard);
        }

        menuPanel.revalidate();
        menuPanel.repaint();
    }

    // ── 🛠️ 일관된 디자인 버튼을 만들어주는 헬퍼 메서드 ───────────────────────
    private JButton createStyledButton(String text, boolean isPrimary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        if (isPrimary) {
            btn.setBackground(COLOR_MAIN);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(COLOR_CARD_BG);
            btn.setForeground(COLOR_MAIN);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                    BorderFactory.createEmptyBorder(7, 14, 7, 14)
            ));
        }
        return btn;
    }

    
    // ── 주문 다이얼로그 ───────────────────────────────────────────────────

    private void showOrderDialog(ProductDTO prod) {
        JDialog dialog = new JDialog(this, prod.name + " 옵션 선택", true);
        dialog.setSize(380, 350);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(COLOR_CARD_BG);
        
        JPanel mainWrapper = new JPanel();
        mainWrapper.setLayout(new BoxLayout(mainWrapper, BoxLayout.Y_AXIS));
        mainWrapper.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainWrapper.setBackground(COLOR_CARD_BG);

        // 상단 메뉴명 컴포넌트 핑크 강조
        JLabel titleLabel = new JLabel(prod.name, SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setForeground(COLOR_MAIN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainWrapper.add(titleLabel);
        mainWrapper.add(Box.createVerticalStrut(10));

        // 1. 온도 선택 패널 (HOT / ICE)
        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tempPanel.setBackground(COLOR_CARD_BG);
        JRadioButton rbHot = new JRadioButton("HOT");
        JRadioButton rbIce = new JRadioButton("ICE");
        rbHot.setBackground(COLOR_CARD_BG);
        rbIce.setBackground(COLOR_CARD_BG);
        ButtonGroup tempGroup = new ButtonGroup();
        tempGroup.add(rbHot);
        tempGroup.add(rbIce);

        if      (prod.temperatureType == 1) { rbHot.setSelected(true);  rbIce.setEnabled(false); }
        else if (prod.temperatureType == 2) { rbIce.setSelected(true);  rbHot.setEnabled(false); }
        else                                { rbIce.setSelected(true); }

        JLabel lblTemp = new JLabel("☕ 온도 선택:   ");
        lblTemp.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        tempPanel.add(lblTemp);
        tempPanel.add(rbHot);
        tempPanel.add(rbIce);

        // 2. 샷 조절 패널
        JPanel shotPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        shotPanel.setBackground(COLOR_CARD_BG);
        JRadioButton rbNormal  = new JRadioButton("보통");
        JRadioButton rbLight   = new JRadioButton("연하게");
        JRadioButton rbAddShot = new JRadioButton("샷추가(+700원)");
        rbNormal.setBackground(COLOR_CARD_BG);
        rbLight.setBackground(COLOR_CARD_BG);
        rbAddShot.setBackground(COLOR_CARD_BG);
        ButtonGroup shotGroup = new ButtonGroup();
        shotGroup.add(rbNormal);
        shotGroup.add(rbLight);
        shotGroup.add(rbAddShot);
        rbNormal.setSelected(true);

        JLabel lblShot = new JLabel("🎯 샷 조절:     ");
        lblShot.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        shotPanel.add(lblShot);
        shotPanel.add(rbNormal);
        shotPanel.add(rbLight);
        shotPanel.add(rbAddShot);

        // 3. 디카페인 선택 패널
        JPanel decafPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        decafPanel.setBackground(COLOR_CARD_BG);
        JCheckBox cbDecaf = new JCheckBox("디카페인으로 변경 (+1,500원)");
        cbDecaf.setBackground(COLOR_CARD_BG);
        
        JLabel lblDecaf = new JLabel("🌱 디카페인:   ");
        lblDecaf.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        decafPanel.add(lblDecaf);
        decafPanel.add(cbDecaf);

        if (!prod.isCoffee) {
            rbNormal.setEnabled(false); rbLight.setEnabled(false); rbAddShot.setEnabled(false); cbDecaf.setEnabled(false);
        }

        // 4. 수량 선택 패널
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        qtyPanel.setBackground(COLOR_CARD_BG);
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, prod.stock, 1));
        
        JLabel lblQty = new JLabel("🛍️ 주문 수량:   ");
        lblQty.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        qtyPanel.add(lblQty);
        qtyPanel.add(qtySpinner);

        // 5. 실시간 단가 표시 레이블
        JLabel priceLabel = new JLabel(String.format("최종 단가: %,d원", prod.price), SwingConstants.CENTER);
        priceLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        priceLabel.setForeground(COLOR_MAIN);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        java.awt.event.ActionListener priceUpdater = e -> {
            int currentPrice = prod.price;
            if (prod.isCoffee) {
                if (rbAddShot.isSelected()) currentPrice += 700;
                if (cbDecaf.isSelected())   currentPrice += 1500;
            }
            priceLabel.setText(String.format("최종 단가: %,d원", currentPrice));
        };
        rbNormal.addActionListener(priceUpdater);
        rbLight.addActionListener(priceUpdater);
        rbAddShot.addActionListener(priceUpdater);
        cbDecaf.addActionListener(priceUpdater);

        // 6. 장바구니 담기 버튼 구조 스타일업
        JButton btnConfirm = createStyledButton("장바구니 담기", true);
        btnConfirm.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnConfirm.addActionListener(e -> {
            String temperature = rbHot.isSelected() ? "HOT" : "ICE";
            int quantity       = (int) qtySpinner.getValue();
            String options = "기본";
            int finalUnitPrice = prod.price;

            if (prod.isCoffee) {
                ArrayList<String> selectedOpts = new ArrayList<>();
                if (rbLight.isSelected()) { selectedOpts.add("연하게"); }
                else if (rbAddShot.isSelected()) { selectedOpts.add("샷추가"); finalUnitPrice += 700; }
                if (cbDecaf.isSelected()) { selectedOpts.add("디카페인"); finalUnitPrice += 1500; }
                if (!selectedOpts.isEmpty()) { options = String.join(", ", selectedOpts); }
            }

            basketService.addItem(prod, temperature, options, quantity, finalUnitPrice);
            refreshBasket();
            dialog.dispose();
        });

        mainWrapper.add(tempPanel);
        mainWrapper.add(shotPanel);
        mainWrapper.add(decafPanel);
        mainWrapper.add(qtyPanel);
        mainWrapper.add(Box.createVerticalStrut(10));
        mainWrapper.add(priceLabel);
        mainWrapper.add(Box.createVerticalStrut(10));
        mainWrapper.add(btnConfirm);

        dialog.add(mainWrapper);
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
        String phone = phoneField.getText().trim();
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
        int useDiscount   = 0;
        int     stampToDeduct = 0;

        if (memberService.canUseStampDiscount()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "스탬프 10개를 사용하여 2,000원 할인받겠습니까?",
                    "스탬프 할인",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                useDiscount   = 1;
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
            if (useDiscount>0) msg += "스탬프 할인 2,000원이 적용되었습니다.\n";
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
        historyArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JButton btnRefresh = createStyledButton("새로고침", false);
        btnRefresh.addActionListener(e -> historyArea.setText(dao.getOrderHistoryText()));

        JButton btnClose = createStyledButton("닫기", true);
        btnClose.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(COLOR_BG);
        btnPanel.add(btnRefresh);
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
                    "• %s [%s] / %s / %d잔 / %,d원\n",
                    item.menuName, item.options, item.temperature,
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