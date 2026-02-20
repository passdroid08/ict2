package com.ict.project.simulator.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ict.project.simulator.entity.CostResultEntity;

public interface SimulationResultRepository extends JpaRepository<CostResultEntity, Long> {
}
