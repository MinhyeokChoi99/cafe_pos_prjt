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
    public static final int STAMP_DISCOUNT_THRESHOLD = 10;   // 1회 사용에 필요한 스탬프 수
    public static final int STAMP_DISCOUNT_AMOUNT    = 2000; // 1회 할인 금액(원)

    private final CafeDAO dao;

    public PaymentService(CafeDAO dao) {
        this.dao = dao;
    }

    /**
     * 사용 가능한 최대 할인 횟수를 반환합니다.
     * 스탬프 보유량과 총 주문금액 중 더 작은 쪽으로 제한됩니다.
     * ex) 스탬프 70개(7회 가능)여도 주문금액 3,000원이면 최대 1회만 선택 가능
     */
    public int getMaxDiscountTimes(int currentStamp, int totalSum) {
        int byStamp = currentStamp / STAMP_DISCOUNT_THRESHOLD;
        int byPrice = totalSum / STAMP_DISCOUNT_AMOUNT;
        return Math.min(byStamp, byPrice);
    }

    /**
     * 할인 횟수에 따른 최종 결제 금액을 반환합니다.
     * @param totalSum     할인 전 합계
     * @param discountTimes 스탬프 할인 사용 횟수 (0이면 할인 없음)
     * @return 최종 결제 금액 (0원 미만이면 0)
     */
    public int calcFinalPrice(int totalSum, int discountTimes) {
        return Math.max(0, totalSum - STAMP_DISCOUNT_AMOUNT * discountTimes);
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
