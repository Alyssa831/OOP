package supermarket;

import java.time.LocalDateTime;

/**
 * Represents a 2-hour delivery time slot with capacity tracking (R10)
 */
public class TimeSlot {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int currentLoad;
    private int maxCapacity;
    
    public TimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
        this(startTime, endTime, 200);
    }
    
    public TimeSlot(LocalDateTime startTime, LocalDateTime endTime, int maxCapacity) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxCapacity = maxCapacity;
        this.currentLoad = 0;
    }
    
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public int getCurrentLoad() { return currentLoad; }
    public int getMaxCapacity() { return maxCapacity; }
    public int getRemainingCapacity() { return maxCapacity - currentLoad; }
    
    public boolean hasCapacity(int additionalWeightKg) {
        return currentLoad + additionalWeightKg <= maxCapacity;
    }
    
    public void addLoad(int weightKg) {
        if (!hasCapacity(weightKg)) {
            throw new RuntimeException("No capacity available");
        }
        this.currentLoad += weightKg;
    }
    
    public boolean isPeakHour() {
        boolean isWeekday = startTime.getDayOfWeek().getValue() >= 1 && 
                           startTime.getDayOfWeek().getValue() <= 5;
        int hour = startTime.getHour();
        return isWeekday && (hour >= 17 && hour < 21);
    }
    
    public double getPeakMultiplier() {
        return isPeakHour() ? 1.5 : 1.0;
    }
    
    public String getTimeWindow() {
        return String.format("%02d:%02d - %02d:%02d",
            startTime.getHour(), startTime.getMinute(),
            endTime.getHour(), endTime.getMinute());
    }
    
    public String getSlotKey() {
        return String.format("%04d-%02d-%02d_%02d:%02d",
            startTime.getYear(), startTime.getMonthValue(),
            startTime.getDayOfMonth(), startTime.getHour(), startTime.getMinute());
    }
    
    public boolean isInFuture() {
        return startTime.isAfter(LocalDateTime.now());
    }
    
    @Override
    public String toString() {
        return String.format("%s | %d/%d kg | %s",
            getTimeWindow(), currentLoad, maxCapacity,
            isPeakHour() ? "PEAK" : "off-peak");
    }
}