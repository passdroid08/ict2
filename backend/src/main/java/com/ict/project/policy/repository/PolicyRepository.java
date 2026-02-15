package com.ict.project.policy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ict.project.policy.model.PolicyEntity;


public interface PolicyRepository extends JpaRepository<PolicyEntity, Long> {

}
