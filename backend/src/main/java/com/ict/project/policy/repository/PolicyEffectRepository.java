package com.ict.project.policy.repository;

import com.ict.project.policy.entity.PolicyEffectEntity;
import com.ict.project.policy.entity.PolicyEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyEffectRepository extends JpaRepository<PolicyEffectEntity, Long> {

	List<PolicyEffectEntity> findByPolicy_PolicyId(Long policyId);

}