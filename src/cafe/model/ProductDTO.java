package cafe.model;

public class ProductDTO {
    public int prodId;
    public String name;
    public int price;
    public boolean isCoffee;
    public int temperatureType; // 0: HOT/ICE 가능, 1: HOT ONLY, 2: ICE ONLY
    public int stock;

    public ProductDTO(int prodId, String name, int price, boolean isCoffee, int temperatureType, int stock) {
        this.prodId = prodId;
        this.name = name;
        this.price = price;
        this.isCoffee = isCoffee;
        this.temperatureType = temperatureType;
        this.stock = stock;
    }

    public boolean isSoldOut() {
        return stock <= 0;
    }
}
