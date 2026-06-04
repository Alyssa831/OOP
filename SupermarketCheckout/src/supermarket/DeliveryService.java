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
     * @param customer The customer requesting delivery
     * @param address Delivery address
     * @param cartWeight Current cart weight in kg
     * @return The assigned time slot window, or null if no slot available
     */
    public String requestDelivery(Customer customer, String address, double cartWeight) {
        if (cartWeight > 50) {
            System.out.println("❌ Delivery not supported: weight exceeds 50kg");
            return null;
        }
        
        // Find best available slot
        TimeSlot bestSlot = slotManager.findBestAvailableSlot((int)cartWeight);
        
        if (bestSlot == null) {
            System.out.println("❌ No delivery slots available. Please try again later.");
            return null;
        }
        
        // Store the assigned slot for this customer
        String slotKey = bestSlot.getSlotKey();
        customerAssignedSlot.put(customer.getUsername(), slotKey);
        
        // Calculate estimated fee to show customer
        double estimatedFee = calculateDeliveryFee(cartWeight, 0, customer, slotKey, 0);
        
        // Show customer their assigned slot
        System.out.println("✅ Delivery requested to: " + address);
        System.out.println("   📦 Assigned time slot: " + bestSlot.getTimeWindow());
        System.out.printf("   💰 Estimated delivery fee: %.2f EUR%n", estimatedFee);
        
        if (bestSlot.isPeakHour()) {
            System.out.println("   ⚠️ Peak hour surcharge applied (+50%)");
        }
        
        if (isEcoFriendlySlot(slotKey, address)) {
            System.out.println("   🌱 Eco-friendly discount applied (-30%): Truck already in your area");
        }
        
        return slotKey;
    }
    
    /**
     * Calculate delivery fee with all applicable rules
     */
    public double calculateDeliveryFee(double totalWeight, double subtotal, Customer customer, String slotKey, double distance) {
        String address = customer.getAddress();
        
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
    
    /**
     * Book the delivery slot after successful payment
     */
    public void bookDelivery(Customer customer, double totalWeight) {
        String slotKey = customerAssignedSlot.get(customer.getUsername());
        if (slotKey != null) {
            slotManager.bookSlot(slotKey, (int)totalWeight);
            
            // Record for eco-friendly detection
            String area = extractAreaCode(customer.getAddress());
            TimeSlot slot = slotManager.getSlot(slotKey);
            if (slot != null) {
                String key = slot.getStartTime().toString() + "_" + area;
                deliveriesByArea.put(key, deliveriesByArea.getOrDefault(key, 0) + 1);
            }
            
            // Clear the assignment
            customerAssignedSlot.remove(customer.getUsername());
        }
    }
    
    /**
     * Get the assigned slot key for a customer (if any)
     */
    public String getAssignedSlot(Customer customer) {
        return customerAssignedSlot.get(customer.getUsername());
    }
    
    /**
     * Check if a slot is eco-friendly (truck already in area)
     */
    public boolean isEcoFriendlySlot(String slotKey, String address) {
        TimeSlot slot = slotManager.getSlot(slotKey);
        if (slot == null) return false;
        
        String area = extractAreaCode(address);
        String key = slot.getStartTime().toString() + "_" + area;
        return deliveriesByArea.getOrDefault(key, 0) > 0;
    }
    
    /**
     * Get slot info for display
     */
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

    public String extractAreaCode(String address) {
        String[] words = address.trim().split("\\s+");
        String zone = words[words.length - 1]; // get zone: versailles, paris, etc
        
        // FIXED: Use .equals() for string comparison
        if (zone.equals("paris")) {
            String[] parts = address.split(",");
            if (parts.length >= 2) {
                String postalPart = parts[1].trim();  // "75001 paris"
                String[] postalWords = postalPart.split("\\s+");
                String postalCode = postalWords[0];   // "75001"
                if (postalCode.matches("\\d{5}")) {
                    String arrondissement = postalCode.substring(3); // Get last two digits
                    zone += arrondissement;  // becomes "paris01", "paris16", etc.
                }
            }
        }
        return zone;
    }
    
}