package supermarket;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DeliverySlotManager {
    private static DeliverySlotManager instance;
    private Map<String, TimeSlot> slots;
    
    private DeliverySlotManager() {
        this.slots = new LinkedHashMap<>();
        initializeSlots();
    }
    
    public static DeliverySlotManager getInstance() {
        if (instance == null) {
            instance = new DeliverySlotManager();
        }
        return instance;
    }
    
    private void initializeSlots() {
        LocalDate today = LocalDate.now();
        for (int day = 0; day < 7; day++) {
            LocalDate date = today.plusDays(day);
            for (int hour = 8; hour < 22; hour += 2) {
                LocalDateTime start = date.atTime(hour, 0);
                LocalDateTime end = start.plusHours(2);
                TimeSlot slot = new TimeSlot(start, end);
                slots.put(slot.getSlotKey(), slot);
            }
        }
    }
    
    public TimeSlot getSlot(String slotKey) {
        return slots.get(slotKey);
    }
    
    /**
     * Find the best available slot for a given cart weight
     * Returns the soonest available slot with capacity
     */
    public TimeSlot findBestAvailableSlot(int cartWeightKg) {
        return slots.values().stream()
            .filter(TimeSlot::isInFuture)
            .filter(slot -> slot.hasCapacity(cartWeightKg))
            .min((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()))
            .orElse(null);
    }
    
    public List<TimeSlot> getAvailableSlots(int cartWeightKg) {
        return slots.values().stream()
            .filter(TimeSlot::isInFuture)
            .filter(slot -> slot.hasCapacity(cartWeightKg))
            .collect(Collectors.toList());
    }
    
    public boolean hasCapacity(String slotKey, int weightKg) {
        TimeSlot slot = slots.get(slotKey);
        return slot != null && slot.hasCapacity(weightKg) && slot.isInFuture();
    }
    
    public boolean bookSlot(String slotKey, int weightKg) {
        TimeSlot slot = slots.get(slotKey);
        if (slot != null && slot.hasCapacity(weightKg) && slot.isInFuture()) {
            slot.addLoad(weightKg);
            return true;
        }
        return false;
    }
}