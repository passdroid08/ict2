package com.ict.project.simulator.controller;

import com.ict.project.simulator.dto.SimulationCalculateRequestDto;
import com.ict.project.simulator.dto.SimulationCalculateResponseDto;
import com.ict.project.simulator.service.SimulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/simulator")
public class SimulatorController {
	
	public record ErrorResponse(String message) {}

    private final SimulatorService simulatorService;

    // 정책 체크/해제 즉시 재계산(선택된 policyIds 포함해서 요청)
    @PostMapping("/calculate")
    public SimulationCalculateResponseDto calculate(@RequestBody SimulationCalculateRequestDto request) {
        return simulatorService.calculate(request);
    }

    // USER_FINANCE 없음 같은 케이스를 400으로 내려주기
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}