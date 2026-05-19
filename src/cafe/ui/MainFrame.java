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
        dialog.setSize(380, 340); // 정렬이 예쁘게 잡히도록 크기 조정
        dialog.setLocationRelativeTo(this);
        
        // 정렬이 무너지지 않도록 상하(Y축)로 패널을 쌓는 BoxLayout 채택
        JPanel mainWrapper = new JPanel();
        mainWrapper.setLayout(new BoxLayout(mainWrapper, BoxLayout.Y_AXIS));
        mainWrapper.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. 온도 선택 패널 (HOT / ICE)
        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JRadioButton rbHot = new JRadioButton("HOT");
        JRadioButton rbIce = new JRadioButton("ICE");
        ButtonGroup tempGroup = new ButtonGroup();
        tempGroup.add(rbHot);
        tempGroup.add(rbIce);

        if      (prod.temperatureType == 1) { rbHot.setSelected(true);  rbIce.setEnabled(false); }
        else if (prod.temperatureType == 2) { rbIce.setSelected(true);  rbHot.setEnabled(false); }
        else                                { rbIce.setSelected(true); }

        tempPanel.add(new JLabel("☕ 온도 선택:   "));
        tempPanel.add(rbHot);
        tempPanel.add(rbIce);

        // 2. 샷 조절 패널 (보통 / 연하게 / 샷추가) -> 라디오 버튼으로 택1
        JPanel shotPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JRadioButton rbNormal  = new JRadioButton("보통");
        JRadioButton rbLight   = new JRadioButton("연하게");
        JRadioButton rbAddShot = new JRadioButton("샷추가(+700원)");
        ButtonGroup shotGroup = new ButtonGroup();
        shotGroup.add(rbNormal);
        shotGroup.add(rbLight);
        shotGroup.add(rbAddShot);
        rbNormal.setSelected(true); // 기본값

        shotPanel.add(new JLabel("🎯 샷 조절:     "));
        shotPanel.add(rbNormal);
        shotPanel.add(rbLight);
        shotPanel.add(rbAddShot);

        // 3. 디카페인 선택 패널 -> 체크박스로 독립적 선택 가능!
        JPanel decafPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox cbDecaf = new JCheckBox("디카페인으로 변경 (+1,500원)");
        decafPanel.add(new JLabel("🌱 디카페인:   "));
        decafPanel.add(cbDecaf);

        // ❌ 커피 메뉴가 아니면 샷 조절 및 디카페인 옵션을 비활성화 처리
        if (!prod.isCoffee) {
            rbNormal.setEnabled(false);
            rbLight.setEnabled(false);
            rbAddShot.setEnabled(false);
            cbDecaf.setEnabled(false);
        }

        // 4. 수량 선택 패널
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, prod.stock, 1));
        qtyPanel.add(new JLabel("🛍️ 주문 수량:   "));
        qtyPanel.add(qtySpinner);

        // 5. 실시간 단가 표시 레이블
        JLabel priceLabel = new JLabel(String.format("최종 단가: %,d원", prod.price), SwingConstants.CENTER);
        priceLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // 가운데 정렬

        // 옵션이나 체크박스를 건드릴 때마다 금액을 실시간 계산해 주는 리스너
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
        cbDecaf.addActionListener(priceUpdater); // 체크박스에도 리스너 연동!

        // 6. 장바구니 담기 버튼
        JButton btnConfirm = new JButton("장바구니 담기");
        btnConfirm.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnConfirm.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnConfirm.addActionListener(e -> {
            String temperature = rbHot.isSelected() ? "HOT" : "ICE";
            int quantity       = (int) qtySpinner.getValue();
            
            // 기본값 세팅
            String options = "기본";
            int finalUnitPrice = prod.price;

            if (prod.isCoffee) {
                ArrayList<String> selectedOpts = new ArrayList<>();
                
                // 1) 샷 옵션 체크
                if (rbLight.isSelected()) {
                    selectedOpts.add("연하게");
                } else if (rbAddShot.isSelected()) {
                    selectedOpts.add("샷추가");
                    finalUnitPrice += 700;
                }

                // 2) 디카페인 옵션 체크 (독립적 추가 가능)
                if (cbDecaf.isSelected()) {
                    selectedOpts.add("디카페인");
                    finalUnitPrice += 1500;
                }

                // 선택된 옵션이 있다면 문자열 합치기 (예: "샷추가, 디카페인")
                if (!selectedOpts.isEmpty()) {
                    options = String.join(", ", selectedOpts);
                }
            }

            // 서비스단으로 데이터 전송
            basketService.addItem(prod, temperature, options, quantity, finalUnitPrice);
            refreshBasket();
            dialog.dispose();
        });

        // 메인 패널에 순서대로 정렬해서 배치 (절대 안 겹침!)
        mainWrapper.add(tempPanel);
        mainWrapper.add(shotPanel);
        mainWrapper.add(decafPanel);
        mainWrapper.add(qtyPanel);
        mainWrapper.add(Box.createVerticalStrut(10)); // 마진 공간 확보
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
        historyArea.setFont(new Font("맑은 고딕", Font.BOLD, 14));

       

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
