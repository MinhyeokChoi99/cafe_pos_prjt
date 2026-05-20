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
    private final CafeDAO        dao            = new CafeDAO();

    // ── Service ───────────────────────────────────────────────────────────
    private final BasketService  basketService  = new BasketService();
    private final MemberService  memberService  = new MemberService(dao);
    private final PaymentService paymentService = new PaymentService(dao);

    // ── 🎨 바나프레소 감성 커스텀 컬러 정의 ─────────────────────────────────
    private final Color COLOR_BG        = new Color(255, 240, 242); // 메인 연분홍 배경
    private final Color COLOR_CARD_BG   = Color.WHITE;               // 카드 및 텍스트 영역 내부
    private final Color COLOR_MAIN      = new Color(232, 90, 113);  // 포인트 딥 핑크
    private final Color COLOR_BORDER    = new Color(255, 211, 218); // 연핑크 테두리
    private final Color COLOR_TEXT_DARK = new Color(60, 60, 60);    // 다크 그레이 텍스트

    // ── UI 컴포넌트 ────────────────────────────────────────────────────────
    private JPanel     menuPanel;
    private JTextArea  basketArea;
    private JLabel     totalLabel;
    private JTextField phoneField;
    private JLabel     memberStatusLabel;                      // yoonsuh: 헤더 회원 상태 표시
    private java.time.LocalDate currentTargetDate;             // master:  주문내역 날짜 네비게이션

    public MainFrame() {
        setTitle("카페 POS 시스템");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(COLOR_BG);

        add(buildHeaderPanel(),  BorderLayout.NORTH);
        add(buildMenuPanel(),    BorderLayout.WEST);
        add(buildCenterPanel(),  BorderLayout.CENTER);
        add(buildSouthPanel(),   BorderLayout.SOUTH);

        setVisible(true);
    }

    // ── 패널 빌더 ─────────────────────────────────────────────────────────

    /** 상단 바나프레소 헤더 (yoonsuh) */
    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_MAIN);
        panel.setPreferredSize(new Dimension(0, 62));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("☕ BANAPRESSO");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);

        memberStatusLabel = new JLabel("비회원 주문");
        memberStatusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        memberStatusLabel.setForeground(new Color(255, 220, 228));

        panel.add(titleLabel,        BorderLayout.WEST);
        panel.add(memberStatusLabel, BorderLayout.EAST);
        return panel;
    }

    private JComponent buildMenuPanel() {
        menuPanel = new JPanel();
        menuPanel.setBackground(COLOR_BG);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        loadMenuButtons();

        JScrollPane menuScrollPane = new JScrollPane(menuPanel);
        menuScrollPane.setPreferredSize(new Dimension(350, 0));
        menuScrollPane.setBackground(COLOR_BG);
        menuScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        menuScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

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
        basketArea.setFont(new Font("맑은 고딕", Font.BOLD, 18));
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
        totalLabel.setForeground(COLOR_MAIN);
        panel.add(totalLabel, BorderLayout.NORTH);

        JPanel btnGroup = new JPanel(new GridLayout(1, 2, 10, 10));
        btnGroup.setBackground(COLOR_BG);

        JButton btnCancel = createStyledButton("전체 취소", false);
        JButton btnPay    = createStyledButton("결제하기", true);

        btnCancel.addActionListener(e -> handleCancel());
        btnPay.addActionListener(e    -> handlePayment());

        btnGroup.add(btnCancel);
        btnGroup.add(btnPay);
        panel.add(btnGroup, BorderLayout.CENTER);
        return panel;
    }

    // ── 메뉴 이미지/정보 카드 로드 ────────────────────────────────────────

    public void loadMenuButtons() {
        menuPanel.removeAll();

        ArrayList<ProductDTO> prodList = dao.getProductList();
        menuPanel.setLayout(new GridLayout(0, 2, 8, 8));

        for (ProductDTO prod : prodList) {
            JPanel itemCard = new JPanel(new BorderLayout(5, 5));
            itemCard.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1, true));
            itemCard.setBackground(COLOR_CARD_BG);
            itemCard.setPreferredSize(new Dimension(150, 190));

            // 1. 음료 이미지 라벨
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

            // 2. 텍스트 정보 라벨 (재고 10개 이하만 표시)
            String menuInfo;
            if (prod.isSoldOut()) {
                menuInfo = String.format(
                        "<html><center><b style='color:#333333;'>%s</b><br>" +
                        "<font color='#E85A71'><b>%,d원</b></font></center></html>",
                        prod.name, prod.price);
            } else if (prod.stock <= 10) {
                menuInfo = String.format(
                        "<html><center><b style='color:#333333;'>%s</b><br>" +
                        "<font color='#E85A71'><b>%,d원</b></font><br>" +
                        "<font color='#e0486b'><b>재고 %d개</b></font></center></html>",
                        prod.name, prod.price, prod.stock);
            } else {
                menuInfo = String.format(
                        "<html><center><b style='color:#333333;'>%s</b><br>" +
                        "<font color='#E85A71'><b>%,d원</b></font></center></html>",
                        prod.name, prod.price);
            }
            JLabel infoLabel = new JLabel(menuInfo, SwingConstants.CENTER);
            infoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));

            // 3. 품절 여부에 따른 처리
            if (prod.isSoldOut()) {
                // master: 이미지 위에 반투명 그래픽 품절 뱃지
                imgLabel = new JLabel("", SwingConstants.CENTER) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        // 이미지 톤다운
                        g2.setColor(new Color(0, 0, 0, 40));
                        g2.fillRect(0, 0, getWidth(), getHeight());

                        // 품절 뱃지
                        int bw = 100, bh = 45;
                        int bx = (getWidth() - bw) / 2;
                        int by = (getHeight() - bh) / 2;
                        g2.setColor(new Color(COLOR_MAIN.getRed(), COLOR_MAIN.getGreen(), COLOR_MAIN.getBlue(), 180));
                        g2.fillRoundRect(bx, by, bw, bh, 25, 25);

                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("맑은 고딕", Font.BOLD, 16));
                        FontMetrics fm = g2.getFontMetrics();
                        int tx = (getWidth() - fm.stringWidth("품절")) / 2;
                        int ty = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                        g2.drawString("품절", tx, ty);
                        g2.dispose();
                    }
                };
                imgLabel.setPreferredSize(new Dimension(130, 130));

                try {
                    String imgPath = "img/" + prod.prodId + ".jpg";
                    ImageIcon rawIcon = new ImageIcon(imgPath);
                    Image scaledImg = rawIcon.getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH);
                    imgLabel.setIcon(new ImageIcon(scaledImg));
                } catch (Exception e) {
                    imgLabel.setText("☕ 사진 없음");
                }
                itemCard.setBackground(new Color(250, 245, 245));

            } else {
                // yoonsuh: 카드 전체 hover 효과
                itemCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
                itemCard.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) { showOrderDialog(prod); }
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        itemCard.setBorder(BorderFactory.createLineBorder(COLOR_MAIN, 2, true));
                        itemCard.setBackground(new Color(255, 245, 247));
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        itemCard.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1, true));
                        itemCard.setBackground(COLOR_CARD_BG);
                    }
                });
            }

            itemCard.add(imgLabel,  BorderLayout.CENTER);
            itemCard.add(infoLabel, BorderLayout.SOUTH);
            menuPanel.add(itemCard);
        }

        menuPanel.revalidate();
        menuPanel.repaint();
    }

    // ── 버튼 헬퍼 (hover 효과 포함, yoonsuh) ─────────────────────────────

    private JButton createStyledButton(String text, boolean isPrimary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btn.setFocusPainted(false);

        Color normalBg, hoverBg;
        if (isPrimary) {
            normalBg = COLOR_MAIN;
            hoverBg  = new Color(210, 60, 85);
            btn.setBackground(normalBg);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        } else {
            normalBg = COLOR_CARD_BG;
            hoverBg  = new Color(255, 230, 235);
            btn.setBackground(normalBg);
            btn.setForeground(COLOR_MAIN);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                    BorderFactory.createEmptyBorder(7, 14, 7, 14)
            ));
        }

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hoverBg); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(normalBg); }
        });

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

        JLabel titleLabel = new JLabel(prod.name, SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setForeground(COLOR_MAIN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainWrapper.add(titleLabel);
        mainWrapper.add(Box.createVerticalStrut(10));

        // 온도 선택
        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tempPanel.setBackground(COLOR_CARD_BG);
        JRadioButton rbHot = new JRadioButton("HOT");
        JRadioButton rbIce = new JRadioButton("ICE");
        rbHot.setBackground(COLOR_CARD_BG);
        rbIce.setBackground(COLOR_CARD_BG);
        ButtonGroup tempGroup = new ButtonGroup();
        tempGroup.add(rbHot);
        tempGroup.add(rbIce);

        if      (prod.temperatureType == 1) { rbHot.setSelected(true); rbIce.setEnabled(false); }
        else if (prod.temperatureType == 2) { rbIce.setSelected(true); rbHot.setEnabled(false); }
        else                                { rbIce.setSelected(true); }

        JLabel lblTemp = new JLabel("☕ 온도 선택:   ");
        lblTemp.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        tempPanel.add(lblTemp);
        tempPanel.add(rbHot);
        tempPanel.add(rbIce);

        // 샷 조절
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

        // 디카페인
        JPanel decafPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        decafPanel.setBackground(COLOR_CARD_BG);
        JCheckBox cbDecaf = new JCheckBox("디카페인으로 변경 (+1,500원)");
        cbDecaf.setBackground(COLOR_CARD_BG);
        JLabel lblDecaf = new JLabel("🌱 디카페인:   ");
        lblDecaf.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        decafPanel.add(lblDecaf);
        decafPanel.add(cbDecaf);

        if (!prod.isCoffee) {
            rbNormal.setEnabled(false); rbLight.setEnabled(false);
            rbAddShot.setEnabled(false); cbDecaf.setEnabled(false);
        }

        // 수량
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        qtyPanel.setBackground(COLOR_CARD_BG);
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, prod.stock, 1));
        JLabel lblQty = new JLabel("🛍️ 주문 수량:   ");
        lblQty.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        qtyPanel.add(lblQty);
        qtyPanel.add(qtySpinner);

        // 실시간 단가
        JLabel priceLabel = new JLabel(String.format("최종 단가: %,d원", prod.price), SwingConstants.CENTER);
        priceLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        priceLabel.setForeground(COLOR_MAIN);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        java.awt.event.ActionListener priceUpdater = e -> {
            int p = prod.price;
            if (prod.isCoffee) {
                if (rbAddShot.isSelected()) p += 700;
                if (cbDecaf.isSelected())   p += 1500;
            }
            priceLabel.setText(String.format("최종 단가: %,d원", p));
        };
        rbNormal.addActionListener(priceUpdater);
        rbLight.addActionListener(priceUpdater);
        rbAddShot.addActionListener(priceUpdater);
        cbDecaf.addActionListener(priceUpdater);

        JButton btnConfirm = createStyledButton("장바구니 담기", true);
        btnConfirm.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnConfirm.addActionListener(e -> {
            String temperature = rbHot.isSelected() ? "HOT" : "ICE";
            int quantity       = (int) qtySpinner.getValue();
            String options     = "기본";
            int finalUnitPrice = prod.price;

            if (prod.isCoffee) {
                ArrayList<String> selectedOpts = new ArrayList<>();
                if (rbLight.isSelected())       { selectedOpts.add("연하게"); }
                else if (rbAddShot.isSelected()) { selectedOpts.add("샷추가"); finalUnitPrice += 700; }
                if (cbDecaf.isSelected())        { selectedOpts.add("디카페인"); finalUnitPrice += 1500; }
                if (!selectedOpts.isEmpty())     { options = String.join(", ", selectedOpts); }
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
        // yoonsuh: 하이픈/공백 자동 제거
        String phone = phoneField.getText().replace("-", "").replace(" ", "");
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "전화번호를 입력해 주세요.");
            return;
        }

        int stamp = memberService.checkMember(phone);

        if (stamp != -1) {
            memberService.setCurrentMember(phone, stamp);
            memberStatusLabel.setText("★ " + phone + "  |  스탬프 " + stamp + "개"); // yoonsuh
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

        int totalSum      = basketService.calcTotal();
        int discountTimes = 0;
        int stampToDeduct = 0;

        int maxTimes = paymentService.getMaxDiscountTimes(memberService.getCurrentStamp(), totalSum);
        if (memberService.isLoggedIn() && maxTimes > 0) {
            String[] options = new String[maxTimes + 1];
            options[0] = "사용 안함";
            for (int i = 1; i <= maxTimes; i++) {
                options[i] = String.format("%d회  (스탬프 %d개  →  -%,d원)",
                        i,
                        i * PaymentService.STAMP_DISCOUNT_THRESHOLD,
                        i * PaymentService.STAMP_DISCOUNT_AMOUNT);
            }

            JComboBox<String> combo = new JComboBox<>(options);
            combo.setFont(new Font("맑은 고딕", Font.PLAIN, 13));

            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.add(new JLabel(String.format(
                    "<html>보유 스탬프: <b>%d개</b> &nbsp;|&nbsp; 총 주문금액: <b>%,d원</b><br><br>" +
                    "스탬프 10개당 2,000원 할인됩니다.<br>사용할 횟수를 선택하세요:</html>",
                    memberService.getCurrentStamp(), totalSum)), BorderLayout.NORTH);
            panel.add(combo, BorderLayout.CENTER);

            int result = JOptionPane.showConfirmDialog(
                    this, panel, "스탬프 할인",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION && combo.getSelectedIndex() > 0) {
                discountTimes = combo.getSelectedIndex();
                stampToDeduct = discountTimes * PaymentService.STAMP_DISCOUNT_THRESHOLD;
            }
        }

        int finalPrice = paymentService.calcFinalPrice(totalSum, discountTimes);

        boolean success = paymentService.pay(
                memberService.getCurrentPhone(),
                basketService.getItems(),
                stampToDeduct,
                memberService.getCurrentStamp(),
                finalPrice
        );

        if (success) {
            String msg = "주문이 정상 처리되었습니다.\n";
            if (discountTimes > 0) msg += String.format("스탬프 할인 %,d원이 적용되었습니다.\n",
                    discountTimes * PaymentService.STAMP_DISCOUNT_AMOUNT);
            msg += String.format("최종 결제 금액: %,d원", finalPrice);

            JOptionPane.showMessageDialog(this, msg);
            basketService.clear();
            refreshBasket();
            phoneField.setText("");
            memberService.clearCurrentMember();
            memberStatusLabel.setText("비회원 주문"); // yoonsuh
            loadMenuButtons();
        } else {
            JOptionPane.showMessageDialog(this, "결제 처리 실패. DB 연결 또는 재고를 확인하세요.");
        }
    }

    // ── 다이얼로그 열기 ───────────────────────────────────────────────────

    private void openOrderHistoryDialog() {
        currentTargetDate = java.time.LocalDate.now(); // master: 날짜 네비게이션
        JDialog dialog = new JDialog(this, "주문 내역", true);
        dialog.setSize(620, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(COLOR_BG);

        // 날짜 선택 컨트롤 (master)
        JPanel dateControlPanel = new JPanel(new BorderLayout(10, 10));
        dateControlPanel.setBackground(COLOR_BG);
        dateControlPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        JButton btnPrev = createStyledButton("◀ 어제", false);
        JButton btnNext = createStyledButton("내일 ▶", false);

        JLabel dateLabel = new JLabel(currentTargetDate.toString(), SwingConstants.CENTER);
        dateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        dateLabel.setForeground(COLOR_MAIN);

        dateControlPanel.add(btnPrev,  BorderLayout.WEST);
        dateControlPanel.add(dateLabel, BorderLayout.CENTER);
        dateControlPanel.add(btnNext,  BorderLayout.EAST);

        JTextArea historyArea = new JTextArea(dao.getOrderHistoryText(currentTargetDate.toString()));
        historyArea.setEditable(false);
        historyArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        historyArea.setBackground(COLOR_CARD_BG);
        historyArea.setForeground(COLOR_TEXT_DARK);

        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));

        btnPrev.addActionListener(e -> {
            currentTargetDate = currentTargetDate.minusDays(1);
            dateLabel.setText(currentTargetDate.toString());
            historyArea.setText(dao.getOrderHistoryText(currentTargetDate.toString()));
        });

        btnNext.addActionListener(e -> {
            currentTargetDate = currentTargetDate.plusDays(1);
            dateLabel.setText(currentTargetDate.toString());
            historyArea.setText(dao.getOrderHistoryText(currentTargetDate.toString()));
        });

        JButton btnClose = createStyledButton("닫기", true);
        btnClose.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(COLOR_BG);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 15));
        btnPanel.add(btnClose);

        dialog.add(dateControlPanel, BorderLayout.NORTH);
        dialog.add(scrollPane,       BorderLayout.CENTER);
        dialog.add(btnPanel,         BorderLayout.SOUTH);
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
