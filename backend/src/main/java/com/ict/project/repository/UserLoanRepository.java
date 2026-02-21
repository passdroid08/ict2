package com.ict.project.repository;

import com.ict.project.entity.UserLoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLoanRepository extends JpaRepository<UserLoanEntity, Long> {

    // UserLoanEntity 안에 'users' 필드가 있고, UsersEntity 안에 'userId'가 있다는 전제
    List<UserLoanEntity> findAllByUsers_UserId(Long userId);
}