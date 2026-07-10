package com.warmingup.backend.appointment.controller;

import com.warmingup.backend.appointment.dto.AppointmentCreateRequest;
import com.warmingup.backend.appointment.dto.AppointmentResponse;
import com.warmingup.backend.appointment.service.AppointmentService;
import com.warmingup.backend.auth.AuthSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment", description = "약속 API")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AuthSession authSession;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "약속 생성")
    public AppointmentResponse create(
            @RequestBody @Valid AppointmentCreateRequest request,
            HttpServletRequest servletRequest
    ) {
        return appointmentService.createAppointment(authSession.getLoginUserId(servletRequest), request);
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "약속 단건 조회")
    public AppointmentResponse getAppointment(@PathVariable Long appointmentId, HttpServletRequest request) {
        return appointmentService.getAppointment(authSession.getLoginUserId(request), appointmentId);
    }

    @PatchMapping("/{appointmentId}/items/{itemId}/complete")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "약속 항목 완료 처리")
    public void completeItem(
            @PathVariable Long appointmentId,
            @PathVariable Long itemId,
            HttpServletRequest request
    ) {
        appointmentService.completeItem(authSession.getLoginUserId(request), appointmentId, itemId);
    }
}
