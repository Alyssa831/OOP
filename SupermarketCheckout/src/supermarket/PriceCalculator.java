package supermarket;

public class PriceCalculator {
    private double p1 = 0.8; // 20% discount for 10 or more items
    private double p2 = 0.9; // 10% discount for 4 to 9 items
    public double calculatePrice(Item item, int quantity) {
        double newPrice = item.getPrice();

        if (quantity >= 10) {
            return p1*newPrice;
        }
        else if (quantity >= 4) {
            return p2*newPrice;
        }

        return newPrice;
    }
}