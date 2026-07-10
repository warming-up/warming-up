package com.warmingup.backend.routine;

import com.warmingup.backend.auth.AuthSession;
import com.warmingup.backend.routine.dto.RoutineCreateRequest;
import com.warmingup.backend.routine.dto.RoutineResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routines")
@RequiredArgsConstructor
@Tag(name = "Routine", description = "루틴 API")
public class RoutineController {

    private final RoutineService routineService;
    private final AuthSession authSession;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "루틴 생성")
    public RoutineResponse create(
            @RequestBody @Valid RoutineCreateRequest request,
            HttpServletRequest servletRequest
    ) {
        return routineService.create(authSession.getLoginUserId(servletRequest), request);
    }

    @GetMapping
    @Operation(summary = "루틴 목록 조회")
    public List<RoutineResponse> findAll(HttpServletRequest request) {
        return routineService.findAll(authSession.getLoginUserId(request));
    }

    @GetMapping("/{routineId}")
    @Operation(summary = "루틴 단건 조회")
    public RoutineResponse findById(@PathVariable Long routineId, HttpServletRequest request) {
        return routineService.findById(authSession.getLoginUserId(request), routineId);
    }

    @PutMapping("/{routineId}")
    @Operation(summary = "루틴 수정")
    public RoutineResponse update(@PathVariable Long routineId,
                                  @RequestBody @Valid RoutineCreateRequest request,
                                  HttpServletRequest servletRequest) {
        return routineService.update(authSession.getLoginUserId(servletRequest), routineId, request);
    }

    @DeleteMapping("/{routineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "루틴 삭제")
    public void delete(@PathVariable Long routineId, HttpServletRequest request) {
        routineService.delete(authSession.getLoginUserId(request), routineId);
    }
}
