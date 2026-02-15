package com.ict.project.simulator.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ict.project.simulator.model.CostResult;

public interface SimulationResultRepository extends JpaRepository<CostResult, Long> {
}
