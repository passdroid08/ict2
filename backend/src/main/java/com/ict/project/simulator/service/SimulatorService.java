package com.ict.project.simulator.service;

import com.ict.project.simulator.dto.SimulationCalculateRequestDto;
import com.ict.project.simulator.dto.SimulationCalculateResponseDto;

public interface SimulatorService {
    SimulationCalculateResponseDto calculate(SimulationCalculateRequestDto request);
}
