package cafe.model;

public class BasketItem {
    public int prodId;
    public String menuName;
    public String temperature;
    public int quantity;
    public int unitPrice;

    public BasketItem(int prodId, String menuName, String temperature, int quantity, int unitPrice) {
        this.prodId = prodId;
        this.menuName = menuName;
        this.temperature = temperature;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getTotalPrice() {
        return quantity * unitPrice;
    }

    public boolean isSameMenu(int prodId, String temperature) {
        return this.prodId == prodId && this.temperature.equals(temperature);
    }
}
