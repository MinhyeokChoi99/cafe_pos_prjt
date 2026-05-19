package cafe.model;

public class BasketItem {
    public int prodId;
    public String menuName;
    public String temperature;
    public String options;     // 🌟 세부 옵션 문자열 필드 추가
    public int quantity;
    public int unitPrice;      // 옵션가가 포함된 최종 1잔당 단가

    // 생성자 수정 (options 추가)
    public BasketItem(int prodId, String menuName, String temperature, String options, int quantity, int unitPrice) {
        this.prodId = prodId;
        this.menuName = menuName;
        this.temperature = temperature;
        this.options = options; 
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getTotalPrice() {
        return quantity * unitPrice;
    }

    // 🌟 중복 메뉴 판별 로직 고도화 (옵션까지 똑같아야 같은 메뉴로 인정)
    public boolean isSameMenu(int prodId, String temperature, String options) {
        return this.prodId == prodId 
               && this.temperature.equals(temperature)
               && this.options.equals(options); 
    }
}
