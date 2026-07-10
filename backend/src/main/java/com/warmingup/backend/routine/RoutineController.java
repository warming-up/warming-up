package com.warmingup.backend.routine;

import com.warmingup.backend.auth.SessionConst;
import com.warmingup.backend.routine.dto.RoutineCreateRequest;
import com.warmingup.backend.routine.dto.RoutineResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/routines")
@RequiredArgsConstructor
@Tag(name = "Routine", description = "루틴 API")
public class RoutineController {

    private final RoutineService routineService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "루틴 생성")
    public RoutineResponse create(@RequestBody @Valid RoutineCreateRequest request, HttpSession session) {
        return routineService.create(getLoginUserId(session), request);
    }

    @GetMapping
    @Operation(summary = "루틴 목록 조회")
    public List<RoutineResponse> findAll(HttpSession session) {
        return routineService.findAll(getLoginUserId(session));
    }

    @GetMapping("/{routineId}")
    @Operation(summary = "루틴 단건 조회")
    public RoutineResponse findById(@PathVariable Long routineId, HttpSession session) {
        return routineService.findById(getLoginUserId(session), routineId);
    }

    @PutMapping("/{routineId}")
    @Operation(summary = "루틴 수정")
    public RoutineResponse update(@PathVariable Long routineId,
                                  @RequestBody @Valid RoutineCreateRequest request,
                                  HttpSession session) {
        return routineService.update(getLoginUserId(session), routineId, request);
    }

    @DeleteMapping("/{routineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "루틴 삭제")
    public void delete(@PathVariable Long routineId, HttpSession session) {
        routineService.delete(getLoginUserId(session), routineId);
    }

    private Long getLoginUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return userId;
    }
}
