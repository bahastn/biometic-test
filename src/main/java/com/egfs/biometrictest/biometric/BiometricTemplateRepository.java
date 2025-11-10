package com.egfs.biometrictest.biometric;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BiometricTemplateRepository extends JpaRepository<BiometricTemplate, Long> {
    Optional<BiometricTemplate> findByUserIdAndFingerIndex(String userId, Integer fingerIndex);
}

