// th/mfu/model/caseentity/AssistanceCase.java
package th.mfu.model.caseentity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assistance_case")
public class AssistanceCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ใครเป็นคนแจ้ง ดึงจาก user ที่ล็อกอิน
    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    // ถ้ามีที่อยู่ที่ user เลือกก็เก็บ ไม่มีก็ว่าง
    @Column(name = "reporter_address_id")
    private Long reporterAddressId;

    // เจ้าหน้าที่ที่ถูก assign
    @Column(name = "assigned_rescue_id")
    private Long assignedRescueId;



    private Double latitude;
    private Double longitude;

    // ประเภทเคสตามปุ่มในหน้าจอ
    @Enumerated(EnumType.STRING)
    @Column(name = "case_type")
    private CaseType caseType;

    // ระบบประเมินให้
    @Enumerated(EnumType.STRING)
    private CaseSeverity severity = CaseSeverity.LOW;

    @Enumerated(EnumType.STRING)
    private CaseStatus status = CaseStatus.NEW;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ===== getters / setters =====

    public Long getId() {
        return id;
    }

    public Long getReporterUserId() {
        return reporterUserId;
    }

    public void setReporterUserId(Long reporterUserId) {
        this.reporterUserId = reporterUserId;
    }

    public Long getReporterAddressId() {
        return reporterAddressId;
    }

    public void setReporterAddressId(Long reporterAddressId) {
        this.reporterAddressId = reporterAddressId;
    }

    public Long getAssignedRescueId() {
        return assignedRescueId;
    }

    public void setAssignedRescueId(Long assignedRescueId) {
        this.assignedRescueId = assignedRescueId;
    }

  

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public CaseType getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseType caseType) {
        this.caseType = caseType;
    }

    public CaseSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(CaseSeverity severity) {
        this.severity = severity;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
