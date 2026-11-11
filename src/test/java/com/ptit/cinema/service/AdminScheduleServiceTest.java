package com.ptit.cinema.service;

import com.ptit.cinema.model.AdminScheduleModels.ScheduleRequest;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminScheduleServiceTest {
    @Test
    void rejectsPastShowtimes() {
        ScheduleRequest request = new ScheduleRequest(
                "P001", "R01", "NV001", LocalDate.now().minusDays(1), "10:00", "12:00"
        );

        assertThrows(IllegalArgumentException.class, () -> AdminScheduleService.validate(request));
    }

    @Test
    void rejectsOutOfRangePrices() {
        assertThrows(IllegalArgumentException.class, () -> AdminScheduleService.validatePrice(0, "thường"));
    }
}
