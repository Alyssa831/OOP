package supermarket;

import java.util.*;

public class DeliveryService {
    private static DeliveryService instance;
    private DeliverySlotManager slotManager;
    private Map<String, Integer> deliveriesByArea;
    
    private DeliveryService() {
        this.slotManager = DeliverySlotManager.getInstance();
        this.deliveriesByArea = new HashMap<>();
    }
    
    public static DeliveryService getInstance() {
        if (instance == null) {
            instance = new DeliveryService();
        }
        return instance;
    }
    
    public double calculateDeliveryFee(double totalWeight, double subtotal, Customer customer, String timeSlot) {
        String address = customer.getAddress();
        
        if (totalWeight > 50) {
            System.out.println("Delivery not supported: weight exceeds 50kg");
            return 0.0;
        }
        
        int distanceKm = calculateDistance(address);
        double baseFee = calculateBaseFee(totalWeight, distanceKm, subtotal);
        
        TimeSlot slot = slotManager.getSlot(timeSlot);
        if (!slot.hasCapacity((int)totalWeight)) {
            System.out.println("No capacity for " + timeSlot);
            return 0.0;
        }
        
        double withPeak = slot.isPeakHour() ? baseFee * 1.5 : baseFee;
        double withEco = applyEcoDiscount(withPeak, slot, address);
        double finalFee = applyPlanDiscount(withEco, customer);
        
        return finalFee;
    }
    
    public void bookDelivery(double totalWeight, Customer customer, String timeSlot) {
        TimeSlot slot = slotManager.getSlot(timeSlot);
        slot.addLoad((int)totalWeight);
        
        String area = extractAreaCode(customer.getAddress());
        String key = slot.getStartTime().toString() + "_" + area;
        deliveriesByArea.put(key, deliveriesByArea.getOrDefault(key, 0) + 1);
    }
    
    public List<String> getAvailableSlots(double totalWeight) {
        return slotManager.getAvailableSlotStrings((int)totalWeight);
    }
    
    public boolean hasCapacity(double totalWeight, String timeSlot) {
        return slotManager.hasCapacity(timeSlot, (int)totalWeight);
    }
    
    public void displayAllSlots() {
        slotManager.displayAllSlots();
    }
    
    private double calculateBaseFee(double weightKg, int distanceKm, double subtotal) {
        if (weightKg < 10 && distanceKm <= 30) {
            return 15.0;
        } else if (weightKg >= 10 && weightKg <= 50) {
            return 15.0 + (subtotal * 0.05);
        }
        return 15.0;
    }
    
    private double applyEcoDiscount(double fee, TimeSlot slot, String address) {
        String area = extractAreaCode(address);
        String key = slot.getStartTime().toString() + "_" + area;
        
        if (deliveriesByArea.getOrDefault(key, 0) > 0) {
            System.out.println("🌱 Eco discount: -30%");
            return fee * 0.7;
        }
        return fee;
    }
    
    private double applyPlanDiscount(double fee, Customer customer) {
        DiscountPlan plan = customer.getDp();
        if (plan instanceof Prime) {
            System.out.println("💎 Prime: 50% off delivery");
            return fee * 0.5;
        } else if (plan instanceof Platinum) {
            System.out.println("👑 Platinum: Free delivery");
            return 0.0;
        }
        return fee;
    }
    
    private int calculateDistance(String address) {
        if (address == null) return 10;
        String postalCode = extractPostalCode(address);
        if (postalCode.isEmpty()) return 10;
        try {
            return Integer.parseInt(postalCode.substring(postalCode.length() - 2)) % 50;
        } catch (NumberFormatException e) {
            return 10;
        }
    }
    
    private String extractAreaCode(String address) {
        String postalCode = extractPostalCode(address);
        return postalCode.length() >= 3 ? postalCode.substring(0, 3) : postalCode;
    }
    
    private String extractPostalCode(String address) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\b\\d{5}\\b");
        java.util.regex.Matcher m = p.matcher(address);
        return m.find() ? m.group() : "";
    }
}