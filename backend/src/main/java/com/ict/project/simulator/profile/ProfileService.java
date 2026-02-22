package com.ict.project.simulator.profile;



import com.ict.project.simulator.dto.ProfileSnapshotDto;


public interface ProfileService {

    /**
     * 시뮬레이터 계산에 필요한 "사용자 프로필/재무/선호" 정보를 한 번에 모아서 반환합니다.
     * - 없는 정보는 null 또는 빈 리스트로 채워집니다.
     */
    ProfileSnapshotDto loadProfileSnapshot(Long userId);
    
}