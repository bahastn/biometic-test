package com.egfs.biometrictest.biometric;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "biometric_templates",
       uniqueConstraints = @UniqueConstraint(name = "uk_user_finger", columnNames = {"user_id","finger_index"}))
public class BiometricTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 32)
    private String userId;

    @Column(name = "name", length = 64)
    private String name;

    @Column(name = "finger_index")
    private Integer fingerIndex; // 0..9 for fingerprints (iFace702 may also include face templates)

    @Lob
    @Column(name = "template_data", nullable = false)
    private String templateData; // zkemkeeper returns Base64-like string; store as text

    @Column(name = "alg_version", length = 32)
    private String algorithmVersion;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // getters/setters...
    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getFingerIndex() { return fingerIndex; }
    public void setFingerIndex(Integer fingerIndex) { this.fingerIndex = fingerIndex; }
    public String getTemplateData() { return templateData; }
    public void setTemplateData(String templateData) { this.templateData = templateData; }
    public String getAlgorithmVersion() { return algorithmVersion; }
    public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}

