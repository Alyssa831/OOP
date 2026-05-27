package supermarket;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Normal plan: no item discount, full delivery fee.
 */
public class NormalTest {

    @Test
    public void noDiscountOnItems() {
        Normal n = new Normal();
        assertEquals(100.0, n.billAfterDiscount(100, 0), 0.001);
    }

    @Test
    public void fullDeliveryFeeAdded() {
        Normal n = new Normal();
        assertEquals(115.0, n.billAfterDiscount(100, 15), 0.001);
    }

    @Test
    public void zeroSubtotalAndDelivery() {
        Normal n = new Normal();
        assertEquals(0.0, n.billAfterDiscount(0, 0), 0.001);
    }
}
