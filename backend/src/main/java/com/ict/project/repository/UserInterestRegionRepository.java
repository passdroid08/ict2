package com.ict.project.repository;

import com.ict.project.entity.UserInterestRegionEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInterestRegionRepository extends JpaRepository<UserInterestRegionEntity, Long> {
    List<UserInterestRegionEntity> findAllByUsers_UserId(Long userId);
}