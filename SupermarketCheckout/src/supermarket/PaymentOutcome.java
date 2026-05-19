package supermarket;

/**
 * The possible outcomes of a pay() call on the POS.
 * Using an enum (instead of String) gives compile-time safety —
 * no typos like "INSUFFICIENT_FUND", and switch statements are exhaustive.
 */
public enum PaymentOutcome {
	SUCCESS,
	PIN_WRONG,
	INSUFFICIENT_FUNDS,
	AUTH_DENIED
}
