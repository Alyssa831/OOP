package supermarket;

/**
 * The role currently active in the CLUI session.
 * Replaces the previous magic-number scheme (0/1/2/3 on CLUI).
 */
public enum Role {
	NONE,
	MANAGER,
	CASHIER,
	CUSTOMER
}
