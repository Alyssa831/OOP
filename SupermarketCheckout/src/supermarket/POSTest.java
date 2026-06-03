package supermarket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for POS.process — verifies that PIN check, TAS auth, and balance
 * are honored, and that simulatePayment overrides exactly the next call.
 */
public class POSTest {

    private Bank bank;
    private TAS tas;
    private POS pos;

    @BeforeEach
    public void setUp() {
        bank = new Bank();
        tas = new TAS();
        pos = new POS(bank, tas);
        bank.addCard(new Card(1234, 9999));
        tas.addCardInfo(1234, new Info(100.0, true)); // balance 100, auth yes
    }

    @Test
    public void successOnValidCardPinAndBalance() {
        PaymentOutcome r = pos.process(1234, 9999, 50);
        assertEquals(PaymentOutcome.SUCCESS, r);
    }

    @Test
    public void pinWrongWhenPinMismatches() {
        PaymentOutcome r = pos.process(1234, 0000, 50);
        assertEquals(PaymentOutcome.PIN_WRONG, r);
    }

    @Test
    public void authDeniedOnUnknownCard() {
        PaymentOutcome r = pos.process(9999, 9999, 10);
        assertEquals(PaymentOutcome.AUTH_DENIED, r);
    }

    @Test
    public void insufficientFundsWhenBillExceedsBalance() {
        PaymentOutcome r = pos.process(1234, 9999, 500);
        assertEquals(PaymentOutcome.INSUFFICIENT_FUNDS, r);
    }

    @Test
    public void authDeniedWhenTasNotAuthorised() {
        tas.addCardInfo(1234, new Info(1000.0, false)); // auth false
        PaymentOutcome r = pos.process(1234, 9999, 50);
        assertEquals(PaymentOutcome.AUTH_DENIED, r);
    }

    @Test
    public void simulateNextOverridesNextCallOnly() {
        pos.simulateNext(PaymentOutcome.PIN_WRONG);
        // Even with correct PIN/balance, forced outcome wins.
        assertEquals(PaymentOutcome.PIN_WRONG, pos.process(1234, 9999, 10));
        // Override should be consumed; next call goes through the real flow.
        assertEquals(PaymentOutcome.SUCCESS,   pos.process(1234, 9999, 10));
    }

    @Test
    public void simulateNextSuccessHonoured() {
        pos.simulateNext(PaymentOutcome.SUCCESS);
        // Wrong PIN AND no balance, but forced SUCCESS wins.
        assertEquals(PaymentOutcome.SUCCESS, pos.process(1234, 0, 99999));
    }
}
