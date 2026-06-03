package supermarket;

import java.util.*;

/**
 * Delivery Service that handles R10 requirements:
 * - Time slots (2-hour windows)
 * - Capacity management
 * - Dynamic pricing (peak hour surcharge)
 * - Eco-friendly discounts (truck already in area)
 *
 * Time slots are completely hidden from the user — the system
 * automatically assigns the best available slot.
 *
 * Distance is supplied by the caller (CheckoutSession) — it comes from
 * the address book held by CheckoutSystem (single source of truth).
 */
public class DeliveryService {
    private static DeliveryService instance;
    private DeliverySlotManager slotManager;
    private Map<String, Integer> deliveriesByArea; // tracks deliveries by time+area for eco discount

    // Store the assigned slot for each customer's pending delivery
    private Map<String, String> customerAssignedSlot;

    private DeliveryService() {
        this.slotManager = DeliverySlotManager.getInstance();
        this.deliveriesByArea = new HashMap<>();
        this.customerAssignedSlot = new HashMap<>();
    }

    public static DeliveryService getInstance() {
        if (instance == null) {
            instance = new DeliveryService();
        }
        return instance;
    }

    /**
     * Request a delivery for a customer. Automatically finds the best available
     * time slot and assigns it.
     *
     * @param customer    The customer requesting delivery
     * @param address     Delivery address
     * @param cartWeight  Current cart weight in kg
     * @return The assigned time slot key, or null if no slot available
     */
    public String requestDelivery(Customer customer, String address, double cartWeight) {
        if (cartWeight > 50) {
            System.out.println("Delivery not supported: weight exceeds 50kg");
            return null;
        }

        TimeSlot bestSlot = slotManager.findBestAvailableSlot((int) cartWeight);
        if (bestSlot == null) {
            System.out.println("No delivery slots available. Please try again later.");
            return null;
        }

        String slotKey = bestSlot.getSlotKey();
        customerAssignedSlot.put(customer.getUsername(), slotKey);

        // Estimate uses distance=0 (best case); the real fee at computeBill
        // uses the actual distance from CheckoutSystem's address book.
        double estimatedFee = calculateDeliveryFee(cartWeight, 0, customer, slotKey, 0);

        System.out.println("Delivery requested to: " + address);
        System.out.println("Assigned time slot: " + bestSlot.getTimeWindow());
        System.out.printf("Estimated delivery fee: %.2f EUR%n", estimatedFee);
        if (bestSlot.isPeakHour()) {
            System.out.println("Peak hour surcharge applied (+50%)");
        }
        if (isEcoFriendlySlot(slotKey, address)) {
            System.out.println("Eco-friendly discount applied (-30%): Truck already in your area");
        }
        return slotKey;
    }

    /**
     * Calculate delivery fee with all applicable rules.
     * Distance is passed in by the caller (from CheckoutSystem's address book).
     */
    public double calculateDeliveryFee(double totalWeight, double subtotal, Customer customer, String slotKey, double distance) {
        // Use the requested delivery address; fall back to the registered one.
        String address = customer.getRequestDelivery() != null
            ? customer.getRequestDelivery() : customer.getAddress();

        if (totalWeight > 50) {
            return 0.0;
        }

        double baseFee = calculateBaseFee(totalWeight, distance, subtotal);

        TimeSlot slot = slotManager.getSlot(slotKey);
        if (slot == null) {
            return baseFee;
        }

        // Apply peak hour surcharge (R10)
        double withPeak = slot.isPeakHour() ? baseFee * 1.5 : baseFee;

        // Apply eco-friendly discount (R10)
        double withEco = applyEcoDiscount(withPeak, slotKey, address);

        // Apply customer plan discount on delivery (R8b)
        double finalFee = applyPlanDiscount(withEco, customer);

        return finalFee;
    }

    /** Book the delivery slot after successful payment. */
    public void bookDelivery(Customer customer, double totalWeight) {
        String slotKey = customerAssignedSlot.get(customer.getUsername());
        if (slotKey != null) {
            slotManager.bookSlot(slotKey, (int) totalWeight);

            // Record for eco-friendly detection
            String area = extractAreaCode(customer.getAddress());
            TimeSlot slot = slotManager.getSlot(slotKey);
            if (slot != null) {
                String key = slot.getStartTime().toString() + "_" + area;
                deliveriesByArea.put(key, deliveriesByArea.getOrDefault(key, 0) + 1);
            }
            customerAssignedSlot.remove(customer.getUsername());
        }
    }

    public String getAssignedSlot(Customer customer) {
        return customerAssignedSlot.get(customer.getUsername());
    }

    public boolean isEcoFriendlySlot(String slotKey, String address) {
        TimeSlot slot = slotManager.getSlot(slotKey);
        if (slot == null) return false;
        String area = extractAreaCode(address);
        String key = slot.getStartTime().toString() + "_" + area;
        return deliveriesByArea.getOrDefault(key, 0) > 0;
    }

    public TimeSlot getSlotInfo(String slotKey) {
        return slotManager.getSlot(slotKey);
    }

    // ========== Private Helper Methods ==========

    private double calculateBaseFee(double weightKg, double distance, double subtotal) {
        if (weightKg < 10 && distance <= 30) {
            return 15.0;
        } else if (weightKg >= 10 && weightKg <= 50) {
            return 15.0 + (subtotal * 0.05);
        }
        return 15.0;
    }

    private double applyEcoDiscount(double fee, String slotKey, String address) {
        if (isEcoFriendlySlot(slotKey, address)) {
            return fee * 0.7;
        }
        return fee;
    }
    
    private double applyPlanDiscount(double fee, Customer customer) {
        CustomerPlan plan = customer.getDp();
        if (plan instanceof Prime) {
            return fee * 0.5;
        } else if (plan instanceof Platinum) {
            return 0.0;
        }
        return fee;
    }

    /**
     * R10: extract a zone key used for eco-friendly slot detection.
     * For "paris" addresses, append the arrondissement (last 2 digits of the
     * 5-digit postal code) so paris01 and paris16 count as different zones.
     */
    public String extractAreaCode(String address) {
        if (address == null || address.isBlank()) return "";
        String[] words = address.trim().split("\\s+");
        String zone = words[words.length - 1]; // last token: versailles, paris, etc.

        if (zone.equals("paris")) {
            String[] parts = address.split(",");
            if (parts.length >= 2) {
                String postalPart = parts[1].trim();              // "75001 paris"
                String[] postalWords = postalPart.split("\\s+");
                String postalCode = postalWords[0];               // "75001"
                if (postalCode.matches("\\d{5}")) {
                    String arrondissement = postalCode.substring(3);
                    zone += arrondissement;                       // "paris01", "paris16", ...
                }
            }
        }
        return zone;
    }
}
