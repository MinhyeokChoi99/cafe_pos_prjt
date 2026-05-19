package cafe.service;

import cafe.dao.CafeDAO;
import cafe.model.BasketItem;

import java.util.ArrayList;

/**
 * 결제 관련 비즈니스 로직을 담당합니다.
 * - 할인 금액 계산
 * - 결제 처리 (DAO 위임)
 */
public class PaymentService {
    public static final int STAMP_DISCOUNT_THRESHOLD = 10;  // 할인 사용 기준 스탬프 수
    public static final int STAMP_DISCOUNT_AMOUNT    = 2000; // 할인 금액(원)

    private final CafeDAO dao;

    public PaymentService(CafeDAO dao) {
        this.dao = dao;
    }

    /**
     * 스탬프 할인 적용 여부에 따른 최종 결제 금액을 반환합니다.
     * @param totalSum    할인 전 합계
     * @param useDiscount 스탬프 할인 사용 여부
     * @return 최종 결제 금액 (0원 미만이면 0)
     */
    public int calcFinalPrice(int totalSum, boolean useDiscount) {
        if (!useDiscount) return totalSum;
        return Math.max(0, totalSum - STAMP_DISCOUNT_AMOUNT);
    }

    /**
     * 결제를 처리합니다.
     * @param phone         회원 전화번호 (비회원이면 null)
     * @param basketItems   장바구니 아이템 목록
     * @param stampToDeduct 차감할 스탬프 수
     * @param currentStamp  현재 보유 스탬프 수
     * @param finalPrice    최종 결제 금액
     * @return 결제 성공 여부
     */
    public boolean pay(String phone,
                       ArrayList<BasketItem> basketItems,
                       int stampToDeduct,
                       int currentStamp,
                       int finalPrice) {
        return dao.processPayment(phone, basketItems, stampToDeduct, currentStamp, finalPrice);
    }
}
