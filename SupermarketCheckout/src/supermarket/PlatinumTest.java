package supermarket;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Platinum plan (R5): 30% off the items regardless of total.
 * Delivery fee is added as-is (DeliveryService already zeros it for platinum).
 */
public class PlatinumTest {

    @Test
    public void thirtyPercentOffAlways() {
        Platinum p = new Platinum();
        assertEquals(70.0, p.billAfterDiscount(100, 0), 0.001);    // 100 * 0.7
        assertEquals(7.0,  p.billAfterDiscount(10,  0), 0.001);    // 10 * 0.7
    }

    @Test
    public void appliesEvenBelowFiftyEuros() {
        Platinum p = new Platinum();
        // No threshold for platinum — even small carts get 30% off.
        assertEquals(14.0, p.billAfterDiscount(20, 0), 0.001);
    }

    @Test
    public void deliveryFeeAddedAfterDiscount() {
        Platinum p = new Platinum();
        // 100 * 0.7 + 5 = 75 (delivery already discounted by DeliveryService)
        assertEquals(75.0, p.billAfterDiscount(100, 5), 0.001);
    }

    @Test
    public void zeroSubtotalHandled() {
        Platinum p = new Platinum();
        assertEquals(0.0, p.billAfterDiscount(0, 0), 0.001);
    }
}
