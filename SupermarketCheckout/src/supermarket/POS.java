package supermarket;

/**
 * A Point Of Sale terminal.
 *
 * Long-lived instance. Holds references to its Bank (for PIN lookup) and TAS
 * (for authorisation), and a one-shot simulatePayment override slot.
 *
 * The public API is a single process() method (Tell, Don't Ask): the caller
 * passes card+pin+bill and gets back a PaymentOutcome. POS handles the
 * internal sequence — PIN check, then TAS authorisation — privately.
 */
public class POS {
	private Bank bank;
	private TAS tas;

	// One-shot override armed by simulatePayment <outcome>.
	// Read and cleared on the next call to process().
	private PaymentOutcome forcedNext;

	public POS(Bank bank, TAS tas) {
		super();
		this.bank = bank;
		this.tas = tas;
	}

	/**
	 * Arm a one-shot override so the next process() call returns the given
	 * outcome instead of running the real bank conversation.
	 */
	public void simulateNext(PaymentOutcome outcome) {
		this.forcedNext = outcome;
	}

	/**
	 * Process one payment. Returns the outcome.
	 * If a simulatePayment override is armed, returns that and clears the
	 * override — the real bank conversation is skipped.
	 */
	public PaymentOutcome process(int cardnumber, int pin, double bill) {
		// simulatePayment override (only valid for ONE call)
		if (forcedNext != null) {
			PaymentOutcome r = forcedNext;
			forcedNext = null;
			return r;
		}

		// PIN check (per §2.5: locally against the chip — here, against Bank)
		Integer storedPin = bank.getCard_pin().get(cardnumber);
		if (storedPin == null) return PaymentOutcome.AUTH_DENIED; // card unknown
		if (pin != storedPin) return PaymentOutcome.PIN_WRONG;

		// TAS authorisation (per §2.6)
		if (!tas.isAuth(cardnumber)) return PaymentOutcome.AUTH_DENIED;
		if (!tas.checkBalance(cardnumber, bill)) return PaymentOutcome.INSUFFICIENT_FUNDS;

		// All checks passed — debit the account
		tas.setBalance(cardnumber, bill);
		return PaymentOutcome.SUCCESS;
	}
}
