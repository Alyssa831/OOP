package supermarket;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the category-discount PricingPolicy (R6).
 * Unknown categories default to the full price (multiplier 1.0).
 */
public class CatDisTest {

    private Item dairyItem(double price) {
        return new Item("yogurt", "dairy", price, 0.5, 10);
    }

    private Item meatItem(double price) {
        return new Item("steak", "meat", price, 0.5, 10);
    }

    @Test
    public void unknownCategoryDefaultsToFullPrice() {
        CatDis pp = new CatDis();
        assertEquals(10.0, pp.priceAfterDiscount(dairyItem(10), 10), 0.001);
    }

    @Test
    public void registeredCategoryAppliesMultiplier() {
        CatDis pp = new CatDis();
        pp.setCatDis("dairy", 0.95); // 5% off dairy
        assertEquals(9.5, pp.priceAfterDiscount(dairyItem(10), 10), 0.001);
    }

    @Test
    public void multipleCategoriesIndependent() {
        CatDis pp = new CatDis();
        pp.setCatDis("dairy", 0.9);  // 10% off dairy
        pp.setCatDis("meat", 0.85);  // 15% off meat
        assertEquals(9.0, pp.priceAfterDiscount(dairyItem(10), 10), 0.001);
        assertEquals(8.5, pp.priceAfterDiscount(meatItem(10), 10), 0.001);
    }

    @Test
    public void setCatDisOverwrites() {
        CatDis pp = new CatDis();
        pp.setCatDis("dairy", 0.9);
        pp.setCatDis("dairy", 0.5);  // 50% off
        assertEquals(5.0, pp.priceAfterDiscount(dairyItem(10), 10), 0.001);
    }
}
