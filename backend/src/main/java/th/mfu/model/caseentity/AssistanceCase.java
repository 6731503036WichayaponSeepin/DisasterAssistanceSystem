package th.mfu.model.caseentity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import th.mfu.model.locationdata.LocationData;

@Entity
@Table(name = "assistance_case")
public class AssistanceCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    @Column(name = "assigned_rescue_team_id")
    private Long assignedRescueTeamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private LocationData locationId;

    @Enumerated(EnumType.STRING)
    private CaseType caseType;

    @Enumerated(EnumType.STRING)
    private CaseSeverity severity = CaseSeverity.LOW;

    @Enumerated(EnumType.STRING)
    private CaseStatus status = CaseStatus.NEW;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();


    /* =======================
       Virtual Fields for FE
    ======================== */

    @Transient
    private String reporterName;

    @Transient
    private String reporterPhone;

    @Transient
    private String fullAddress;

    /* =======================
       GETTERS / SETTERS
    ======================== */

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

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterPhone() {
        return reporterPhone;
    }

    public void setReporterPhone(String reporterPhone) {
        this.reporterPhone = reporterPhone;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    @Transient
public String getAddress() {
    return this.fullAddress;
}

}