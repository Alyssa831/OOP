package supermarket;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Card class — local chip-PIN check per §2.5.
 */
public class CardTest {

    @Test
    public void matchesPinReturnsTrueOnCorrect() {
        Card c = new Card(1234, 5678);
        assertTrue(c.matchesPin(5678));
    }

    @Test
    public void matchesPinReturnsFalseOnWrong() {
        Card c = new Card(1234, 5678);
        assertFalse(c.matchesPin(0000));
        assertFalse(c.matchesPin(5679));
    }

    @Test
    public void zeroPinSupported() {
        Card c = new Card(1002, 0);
        assertTrue(c.matchesPin(0));
        assertFalse(c.matchesPin(1));
    }

    @Test
    public void gettersReturnConstructorValues() {
        Card c = new Card(1234, 9999);
        assertEquals(1234, c.getCardNumber());
        assertEquals(9999, c.getPin());
    }
}
