package cafe.service;

import cafe.model.BasketItem;
import cafe.model.ProductDTO;
import java.util.ArrayList;

public class BasketService {
    private final ArrayList<BasketItem> items = new ArrayList<>();

    public ArrayList<BasketItem> getItems() {
        return items;
    }
<<<<<<< HEAD
    
=======
>>>>>>> origin/master
    // 🌟 파라미터에 options와 옵션가가 더해진 finalUnitPrice를 추가로 받습니다.
    public void addItem(ProductDTO prod, String temperature, String options, int quantity, int finalUnitPrice) {
        
        // 1. 장바구니에 이미 '같은 메뉴 + 같은 온도 + 같은 옵션'이 담겨있는지 검사
        for (BasketItem item : items) {
            if (item.isSameMenu(prod.prodId, temperature, options)) {
                item.quantity += quantity; // 수량만 누적
                return;
            }
        }

        // 2. 새로운 옵션 조합이라면 새롭게 적재 (최종 단가 인입)
        items.add(new BasketItem(prod.prodId, prod.name, temperature, options, quantity, finalUnitPrice));
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int calcTotal() {
        int total = 0;
        for (BasketItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }
}
