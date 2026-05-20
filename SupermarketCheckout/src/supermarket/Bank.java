package supermarket;

import java.util.HashMap;
import java.util.Map;

/**
 * The bank that issues cards. Per §2.5 the PIN lives on the card chip,
 * so the bank holds Card objects (each carrying its own PIN), not a
 * separate PIN map.
 */
public class Bank {
	private Map<Integer, Card> cards = new HashMap<>();

	public void addCard(Card card) {
		cards.put(card.getCardNumber(), card);
	}

	public Card getCard(int cardNumber) {
		return cards.get(cardNumber);
	}
}
