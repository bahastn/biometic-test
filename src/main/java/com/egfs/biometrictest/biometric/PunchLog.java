package com.egfs.biometrictest.biometric;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "punch_logs")
public class PunchLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 32)
    private String userId;

    @Column(name = "name", length = 64)
    private String name;

    @Column(name = "punch_time", nullable = false)
    private OffsetDateTime punchTime;

    @Column(name = "verify_mode")
    private Integer verifyMode; // 0=password, 1=fingerprint, 2=card, etc.

    @Column(name = "in_out_mode")
    private Integer inOutMode; // 0=check-in, 1=check-out, 2=break-out, 3=break-in, etc.

    @Column(name = "work_code", length = 16)
    private String workCode;

    @Column(name = "synced_at", nullable = false)
    private OffsetDateTime syncedAt = OffsetDateTime.now();

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OffsetDateTime getPunchTime() {
        return punchTime;
    }

    public void setPunchTime(OffsetDateTime punchTime) {
        this.punchTime = punchTime;
    }

    public Integer getVerifyMode() {
        return verifyMode;
    }

    public void setVerifyMode(Integer verifyMode) {
        this.verifyMode = verifyMode;
    }

    public Integer getInOutMode() {
        return inOutMode;
    }

    public void setInOutMode(Integer inOutMode) {
        this.inOutMode = inOutMode;
    }

    public String getWorkCode() {
        return workCode;
    }

    public void setWorkCode(String workCode) {
        this.workCode = workCode;
    }

    public OffsetDateTime getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(OffsetDateTime syncedAt) {
        this.syncedAt = syncedAt;
    }
}
