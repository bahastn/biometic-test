package com.egfs.biometrictest.biometric;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PunchLogTests {

    @Autowired
    private PunchLogRepository punchLogRepository;

    @Test
    void testPunchLogCreation() {
        // Given
        PunchLog punchLog = new PunchLog();
        punchLog.setUserId("100");
        punchLog.setName("Test User");
        punchLog.setPunchTime(OffsetDateTime.now());
        punchLog.setVerifyMode(1);
        punchLog.setInOutMode(0);
        punchLog.setWorkCode("0");

        // When
        PunchLog savedPunch = punchLogRepository.save(punchLog);

        // Then
        assertThat(savedPunch.getId()).isNotNull();
        assertThat(savedPunch.getUserId()).isEqualTo("100");
        assertThat(savedPunch.getName()).isEqualTo("Test User");
        assertThat(savedPunch.getVerifyMode()).isEqualTo(1);
        assertThat(savedPunch.getInOutMode()).isEqualTo(0);
    }

    @Test
    void testFindByUserId() {
        // Given
        PunchLog punch1 = new PunchLog();
        punch1.setUserId("101");
        punch1.setName("John Doe");
        punch1.setPunchTime(OffsetDateTime.now().minusHours(2));
        punch1.setVerifyMode(1);
        punch1.setInOutMode(0);
        punchLogRepository.save(punch1);

        PunchLog punch2 = new PunchLog();
        punch2.setUserId("101");
        punch2.setName("John Doe");
        punch2.setPunchTime(OffsetDateTime.now().minusHours(1));
        punch2.setVerifyMode(1);
        punch2.setInOutMode(1);
        punchLogRepository.save(punch2);

        // When
        var punches = punchLogRepository.findByUserIdOrderByPunchTimeDesc("101");

        // Then
        assertThat(punches).hasSize(2);
        assertThat(punches.get(0).getPunchTime()).isAfter(punches.get(1).getPunchTime());
    }

    @Test
    void testFindAllOrderedByPunchTime() {
        // Given
        PunchLog punch1 = new PunchLog();
        punch1.setUserId("102");
        punch1.setPunchTime(OffsetDateTime.now().minusHours(3));
        punch1.setVerifyMode(1);
        punch1.setInOutMode(0);
        punchLogRepository.save(punch1);

        PunchLog punch2 = new PunchLog();
        punch2.setUserId("103");
        punch2.setPunchTime(OffsetDateTime.now().minusHours(1));
        punch2.setVerifyMode(1);
        punch2.setInOutMode(0);
        punchLogRepository.save(punch2);

        // When
        var allPunches = punchLogRepository.findAllByOrderByPunchTimeDesc();

        // Then
        assertThat(allPunches).isNotEmpty();
        // Verify descending order
        for (int i = 0; i < allPunches.size() - 1; i++) {
            assertThat(allPunches.get(i).getPunchTime())
                .isAfterOrEqualTo(allPunches.get(i + 1).getPunchTime());
        }
    }
}
