package com.egfs.biometrictest.biometric;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface PunchLogRepository extends JpaRepository<PunchLog, Long> {
    List<PunchLog> findByUserIdOrderByPunchTimeDesc(String userId);
    List<PunchLog> findByPunchTimeBetween(OffsetDateTime start, OffsetDateTime end);
    List<PunchLog> findAllByOrderByPunchTimeDesc();
}
