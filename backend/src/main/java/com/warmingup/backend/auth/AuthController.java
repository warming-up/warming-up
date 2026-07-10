package com.warmingup.backend.auth;

import com.warmingup.backend.auth.dto.JoinRequest;
import com.warmingup.backend.auth.dto.LoginRequest;
import com.warmingup.backend.auth.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원가입")
    public UserResponse join(@RequestBody @Valid JoinRequest request) {
        return authService.join(request);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public UserResponse login(@RequestBody @Valid LoginRequest request, HttpServletRequest servletRequest) {
        UserResponse response = authService.login(request);
        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(SessionConst.LOGIN_USER, response.id());
        return response;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "로그아웃")
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
