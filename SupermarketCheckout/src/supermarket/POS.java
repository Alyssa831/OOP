package supermarket;

/**
 * A Point Of Sale terminal.
 *
 * Long-lived instance. Holds references to its Bank and TAS, plus a one-shot
 * simulatePayment override slot. The public API is a single process() method
 * (Tell, Don't Ask) returning a PaymentOutcome enum.
 *
 * Per §2.5, PIN verification is LOCAL — the POS asks the Card (the chip)
 * to verify the PIN. The TAS is only consulted for the balance / authorisation.
 */
public class POS {
	private Bank bank;
	private TAS tas;
	private PaymentOutcome forcedNext;   // one-shot override armed by simulatePayment

	public POS(Bank bank, TAS tas) {
		this.bank = bank;
		this.tas = tas;
	}

	/** Arm a one-shot override consumed by the next process() call. */
	public void simulateNext(PaymentOutcome outcome) {
		this.forcedNext = outcome;
	}

	/** Run one payment; return the outcome. */
	public PaymentOutcome process(int cardnumber, int pin, double bill) {
		// simulatePayment override (consumed once)
		if (forcedNext != null) {
			PaymentOutcome r = forcedNext;
			forcedNext = null;
			return r;
		}

		// PIN check — local, against the card chip (§2.5)
		Card card = bank.getCard(cardnumber);
		if (card == null) return PaymentOutcome.AUTH_DENIED;          // card unknown
		if (!card.matchesPin(pin)) return PaymentOutcome.PIN_WRONG;

		// TAS authorisation (§2.6)
		if (!tas.isAuth(cardnumber)) return PaymentOutcome.AUTH_DENIED;
		if (!tas.checkBalance(cardnumber, bill)) return PaymentOutcome.INSUFFICIENT_FUNDS;

		// All checks passed — debit the account
		tas.setBalance(cardnumber, bill);
		return PaymentOutcome.SUCCESS;
	}
}
