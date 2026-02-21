package com.ict.project.simulator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ict.project.simulator.entity.CostResultEntity;

@Repository
public interface SimulationResultRepository extends JpaRepository<CostResultEntity, Long> {
}
