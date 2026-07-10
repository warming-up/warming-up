package com.warmingup.backend.appointment.controller;

import com.warmingup.backend.appointment.dto.AppointmentCreateRequest;
import com.warmingup.backend.appointment.dto.AppointmentResponse;
import com.warmingup.backend.appointment.service.AppointmentService;
import com.warmingup.backend.auth.SessionConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment", description = "약속 API")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "약속 생성")
    public AppointmentResponse create(@RequestBody @Valid AppointmentCreateRequest request, HttpSession session) {
        return appointmentService.createAppointment(getLoginUserId(session), request);
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "약속 단건 조회")
    public AppointmentResponse getAppointment(@PathVariable Long appointmentId, HttpSession session) {
        return appointmentService.getAppointment(getLoginUserId(session), appointmentId);
    }

    @PatchMapping("/{appointmentId}/items/{itemId}/complete")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "약속 항목 완료 처리")
    public void completeItem(
            @PathVariable Long appointmentId,
            @PathVariable Long itemId,
            HttpSession session
    ) {
        appointmentService.completeItem(getLoginUserId(session), appointmentId, itemId);
    }

    private Long getLoginUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return userId;
    }
}
