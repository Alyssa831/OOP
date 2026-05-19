package supermarket;
import java.util.HashMap;
import java.util.Map;

public class Bank {
	private Map<Integer,Integer> card_pin = new HashMap<Integer, Integer>();

	public void addCardPin(Integer cardNumber, Integer pin) {
		card_pin.put(cardNumber,pin);
	}

	public Map<Integer, Integer> getCard_pin() {
		return card_pin;
	}
	
	
	
}
