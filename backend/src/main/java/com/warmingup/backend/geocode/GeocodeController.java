package com.warmingup.backend.geocode;

import com.warmingup.backend.auth.AuthSession;
import com.warmingup.backend.geocode.dto.GeocodeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geocode")
@RequiredArgsConstructor
@Validated
@Tag(name = "Geocode", description = "주소-좌표 변환 API")
public class GeocodeController {

    private final GeocodeService geocodeService;
    private final AuthSession authSession;

    @GetMapping
    @Operation(summary = "장소 주소를 위도/경도 좌표로 변환")
    public GeocodeResponse geocode(
            @RequestParam @NotBlank(message = "주소는 필수입니다.") String address,
            HttpServletRequest servletRequest
    ) {
        authSession.getLoginUserId(servletRequest);
        return geocodeService.geocode(address);
    }
}
