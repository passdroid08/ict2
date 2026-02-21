package com.ict.project.repository;

import com.ict.project.entity.UserPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreferenceEntity, Long> {
    List<UserPreferenceEntity> findAllByUsers_UserId(Long userId);
}