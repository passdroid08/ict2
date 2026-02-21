package com.ict.project.policy.repository;

import com.ict.project.policy.entity.PolicyConditionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyConditionRepository extends JpaRepository<PolicyConditionEntity, Long> {
    List<PolicyConditionEntity> findAllByPolicy_PolicyId(Long policyId);
}