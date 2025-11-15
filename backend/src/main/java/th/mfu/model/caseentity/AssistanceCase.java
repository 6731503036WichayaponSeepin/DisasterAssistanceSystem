package th.mfu.model.caseentity;

import jakarta.persistence.*;
import th.mfu.model.locationdata.LocationData;

import java.time.LocalDateTime;

@Entity
@Table(name = "assistance_case")
public class AssistanceCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ผู้ใช้ที่แจ้งเคส
    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    // ทีมกู้ภัยที่รับเคส (ถ้าใช้)
    @Column(name = "assigned_rescue_team_id")
    private Long assignedRescueTeamId;

    // FK -> location_data.id
   @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private LocationData locationId;


    @Enumerated(EnumType.STRING)
    @Column(name = "case_type")
    private CaseType caseType;

    @Enumerated(EnumType.STRING)
    private CaseSeverity severity = CaseSeverity.LOW;

    @Enumerated(EnumType.STRING)
    private CaseStatus status = CaseStatus.NEW;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReporterUserId() {
        return reporterUserId;
    }

    public void setReporterUserId(Long reporterUserId) {
        this.reporterUserId = reporterUserId;
    }

    public Long getAssignedRescueTeamId() {
        return assignedRescueTeamId;
    }

    public void setAssignedRescueTeamId(Long assignedRescueTeamId) {
        this.assignedRescueTeamId = assignedRescueTeamId;
    }

    public LocationData getLocationId() {
        return locationId;
    }

    public void setLocationId(LocationData locationId) {
        this.locationId = locationId;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    // ===== getters/setters =====

    
}
