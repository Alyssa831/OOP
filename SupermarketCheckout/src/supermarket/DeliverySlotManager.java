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
        TimeSlot slot = slots.get(slotKey);
        if (slot == null) {
            throw new IllegalArgumentException("Invalid slot: " + slotKey);
        }
        return slot;
    }
    
    public List<String> getAvailableSlotStrings(int cartWeightKg) {
        return slots.values().stream()
            .filter(TimeSlot::isInFuture)
            .filter(slot -> slot.hasCapacity(cartWeightKg))
            .map(TimeSlot::getSlotKey)
            .collect(Collectors.toList());
    }
    
    public List<TimeSlot> getAvailableSlots(int cartWeightKg) {
        return slots.values().stream()
            .filter(TimeSlot::isInFuture)
            .filter(slot -> slot.hasCapacity(cartWeightKg))
            .collect(Collectors.toList());
    }
    
    public boolean hasCapacity(String slotKey, int weightKg) {
        try {
            TimeSlot slot = getSlot(slotKey);
            return slot.hasCapacity(weightKg) && slot.isInFuture();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    public boolean bookSlot(String slotKey, int weightKg) {
        try {
            TimeSlot slot = getSlot(slotKey);
            if (slot.hasCapacity(weightKg) && slot.isInFuture()) {
                slot.addLoad(weightKg);
                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    public void displayAllSlots() {
        System.out.println("\n========== DELIVERY SLOTS ==========");
        for (TimeSlot slot : slots.values()) {
            System.out.printf("%s | %3d/%3d kg | %s%n",
                slot.getTimeWindow(),
                slot.getCurrentLoad(), slot.getMaxCapacity(),
                slot.isPeakHour() ? "🔴 PEAK" : "🟢 off-peak");
        }
        System.out.println("====================================\n");
    }
}