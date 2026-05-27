package supermarket;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the R10 TimeSlot — capacity tracking and peak-hour detection.
 */
public class TimeSlotTest {

    /** A weekday at the given hour. Finds the next Wednesday. */
    private LocalDateTime weekdayAt(int hour) {
        LocalDateTime d = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        while (d.getDayOfWeek() != DayOfWeek.WEDNESDAY) d = d.plusDays(1);
        return d.withHour(hour);
    }

    /** A weekend at the given hour (finds the next Saturday). */
    private LocalDateTime weekendAt(int hour) {
        LocalDateTime d = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        while (d.getDayOfWeek() != DayOfWeek.SATURDAY) d = d.plusDays(1);
        return d.withHour(hour);
    }

    @Test
    public void weekdayEveningIsPeak() {
        LocalDateTime start = weekdayAt(18);
        TimeSlot s = new TimeSlot(start, start.plusHours(2));
        assertTrue(s.isPeakHour());
        assertEquals(1.5, s.getPeakMultiplier(), 0.001);
    }

    @Test
    public void weekdayMorningIsNotPeak() {
        LocalDateTime start = weekdayAt(10);
        TimeSlot s = new TimeSlot(start, start.plusHours(2));
        assertFalse(s.isPeakHour());
        assertEquals(1.0, s.getPeakMultiplier(), 0.001);
    }

    @Test
    public void weekendEveningIsNotPeak() {
        LocalDateTime start = weekendAt(18);
        TimeSlot s = new TimeSlot(start, start.plusHours(2));
        assertFalse(s.isPeakHour());
    }

    @Test
    public void capacityTrackingAndOverbookingPrevention() {
        LocalDateTime start = weekdayAt(10);
        TimeSlot s = new TimeSlot(start, start.plusHours(2), 50); // capacity 50kg
        assertTrue(s.hasCapacity(30));
        s.addLoad(30);
        assertEquals(30, s.getCurrentLoad());
        assertEquals(20, s.getRemainingCapacity());

        // Adding more than remaining should throw.
        assertThrows(RuntimeException.class, () -> s.addLoad(30));
    }

    @Test
    public void hasCapacityRespectsRemaining() {
        LocalDateTime start = weekdayAt(10);
        TimeSlot s = new TimeSlot(start, start.plusHours(2), 50);
        s.addLoad(40);
        assertTrue(s.hasCapacity(10));
        assertFalse(s.hasCapacity(11));
    }
}
