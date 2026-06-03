package supermarket;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the variable price calculator (R3 — price may depend on quantity).
 * Tiers: q >= 10 -> 20% off unit; q >= 4 -> 10% off unit; else full price.
 */
public class PriceCalculatorTest {

    private Item apple() {
        return new Item("apple", "fruit-and-vegetables", 1.20, 0.2, 100);
    }

    private Item chicken() {
        return new Item("chicken", "meat", 7.00, 1.0, 20);
    }

    @Test
    public void fullPriceBelowFour() {
        PriceCalculator pc = new PriceCalculator();
        assertEquals(1.20, pc.calculatePrice(apple(), 1), 0.001);
        assertEquals(1.20, pc.calculatePrice(apple(), 3), 0.001);
    }

    @Test
    public void tenPercentOffFromFour() {
        PriceCalculator pc = new PriceCalculator();
        assertEquals(1.08, pc.calculatePrice(apple(), 4), 0.001);
        assertEquals(1.08, pc.calculatePrice(apple(), 9), 0.001);
        assertEquals(6.30, pc.calculatePrice(chicken(), 5), 0.001);
    }

    @Test
    public void twentyPercentOffFromTen() {
        PriceCalculator pc = new PriceCalculator();
        assertEquals(0.96, pc.calculatePrice(apple(), 10), 0.001);
        assertEquals(0.96, pc.calculatePrice(apple(), 50), 0.001);
        assertEquals(5.60, pc.calculatePrice(chicken(), 15), 0.001);
    }

    @Test
    public void usesItemPriceNotFixedConstant() {
        PriceCalculator pc = new PriceCalculator();
        assertNotEquals(pc.calculatePrice(apple(), 5),
                        pc.calculatePrice(chicken(), 5), 0.001);
    }
}
