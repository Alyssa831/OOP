package supermarket;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Prime plan (R5): 20% off items when the subtotal is >= 50€.
 * The plan adds the delivery fee as-is — delivery's plan discount is owned by
 * DeliveryService (R8b), so Prime.billAfterDiscount shouldn't re-discount it.
 */
public class PrimeTest {

    @Test
    public void noItemDiscountBelowThreshold() {
        Prime p = new Prime();
        // 40€ < 50€ -> no item discount; delivery added as-is.
        assertEquals(40.0, p.billAfterDiscount(40, 0), 0.001);
        assertEquals(50.0, p.billAfterDiscount(40, 10), 0.001);
    }

    @Test
    public void twentyPercentOffAtAndAboveThreshold() {
        Prime p = new Prime();
        // 50€ -> 50 * 0.8 = 40
        assertEquals(40.0, p.billAfterDiscount(50, 0), 0.001);
        // 100€ -> 100 * 0.8 = 80
        assertEquals(80.0, p.billAfterDiscount(100, 0), 0.001);
    }

    @Test
    public void deliveryFeeIsAddedNotDiscounted() {
        // Prime no longer halves delivery here — DeliveryService already did.
        Prime p = new Prime();
        // 100 * 0.8 + 10 = 90
        assertEquals(90.0, p.billAfterDiscount(100, 10), 0.001);
    }
}
