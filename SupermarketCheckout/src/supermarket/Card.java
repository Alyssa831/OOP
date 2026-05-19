package supermarket;

/**
 * A bank card. Per §2.5 of the brief, the PIN is stored on the card chip
 * (not on the bank's server) — so the POS verifies the PIN against the Card
 * locally, and the TAS only handles balance/authorisation.
 */
public class Card {
	private int cardNumber;
	private int pin;

	public Card(int cardNumber, int pin) {
		this.cardNumber = cardNumber;
		this.pin = pin;
	}

	public int getCardNumber() {
		return cardNumber;
	}

	public int getPin() {
		return pin;
	}

	/**
	 * Local PIN check — what §2.5 describes ("the POS verifies that the
	 * entered PIN matches that stored on the card chip").
	 */
	public boolean matchesPin(int entered) {
		return this.pin == entered;
	}
}
