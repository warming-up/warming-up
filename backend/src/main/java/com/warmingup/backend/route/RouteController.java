package com.warmingup.backend.route;

import com.warmingup.backend.auth.AuthSession;
import com.warmingup.backend.route.dto.RouteEtaRequest;
import com.warmingup.backend.route.dto.RouteEtaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Tag(name = "Route", description = "이동 경로 API")
public class RouteController {

    private final RouteService routeService;
    private final AuthSession authSession;

    @PostMapping("/eta")
    @Operation(summary = "현재 위치와 도착지 기준 자동차 이동시간 계산")
    public RouteEtaResponse calculateEta(
            @RequestBody @Valid RouteEtaRequest request,
            HttpServletRequest servletRequest
    ) {
        authSession.getLoginUserId(servletRequest);
        return routeService.calculateEta(request);
    }
}
