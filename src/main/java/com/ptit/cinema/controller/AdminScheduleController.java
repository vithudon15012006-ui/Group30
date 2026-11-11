package com.ptit.cinema.controller;

import com.ptit.cinema.model.AdminScheduleModels.Options;
import com.ptit.cinema.model.AdminScheduleModels.PriceUpdate;
import com.ptit.cinema.model.AdminScheduleModels.ScheduleRequest;
import com.ptit.cinema.model.AdminScheduleModels.ScheduleView;
import com.ptit.cinema.model.AdminScheduleModels.TicketPrices;
import com.ptit.cinema.service.AdminScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/schedules")
public class AdminScheduleController {
    private final AdminScheduleService schedules;

    public AdminScheduleController(AdminScheduleService schedules) {
        this.schedules = schedules;
    }

    @GetMapping("/options")
    public Options options() {
        return schedules.options();
    }

    @GetMapping
    public List<ScheduleView> schedules() {
        return schedules.schedules();
    }

    @PutMapping("/prices")
    public TicketPrices updatePrices(@RequestBody PriceUpdate request, Authentication authentication) {
        return schedules.updatePrices(request, authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleView create(@RequestBody ScheduleRequest request, Authentication authentication) {
        return schedules.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    public ScheduleView update(
            @PathVariable String id,
            @RequestBody ScheduleRequest request,
            Authentication authentication
    ) {
        return schedules.update(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable String id, Authentication authentication) {
        schedules.delete(id, authentication.getName());
        return Map.of("message", "Đã xóa suất chiếu.");
    }
}
