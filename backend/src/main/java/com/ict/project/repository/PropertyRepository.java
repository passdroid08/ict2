package com.ict.project.repository;

import com.ict.project.entity.PropertyEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<PropertyEntity, Long> {
    // TODO: 후보 매물 조회 조건이 확정되면
    // findAllByLocation_... / findAllByTradeType... / custom query 등을 추가
}