package cafe.service;

import cafe.model.BasketItem;
import cafe.model.ProductDTO;

import java.util.ArrayList;

/**
 * 장바구니 관련 비즈니스 로직을 담당합니다.
 * - 아이템 추가 / 초기화
 * - 총액 계산
 */
public class BasketService {
    private final ArrayList<BasketItem> basketList = new ArrayList<>();

    /** 장바구니에 상품 추가. 같은 메뉴+온도가 이미 있으면 수량만 증가. */
    public void addItem(ProductDTO prod, String temperature, int quantity) {
        for (BasketItem item : basketList) {
            if (item.isSameMenu(prod.prodId, temperature)) {
                item.quantity += quantity;
                return;
            }
        }
        basketList.add(new BasketItem(prod.prodId, prod.name, temperature, quantity, prod.price));
    }

    /** 장바구니 전체 비우기 */
    public void clear() {
        basketList.clear();
    }

    /** 장바구니가 비어있는지 확인 */
    public boolean isEmpty() {
        return basketList.isEmpty();
    }

    /** 장바구니 목록 반환 (읽기 전용 사본) */
    public ArrayList<BasketItem> getItems() {
        return new ArrayList<>(basketList);
    }

    /** 장바구니 전체 합계 계산 */
    public int calcTotal() {
        return basketList.stream().mapToInt(BasketItem::getTotalPrice).sum();
    }
}
