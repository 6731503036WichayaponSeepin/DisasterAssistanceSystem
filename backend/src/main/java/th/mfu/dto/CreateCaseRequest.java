// th/mfu/dto/CreateCaseRequest.java
package th.mfu.dto;

import th.mfu.model.caseentity.CaseType;

public class CreateCaseRequest {

    private CaseType caseType;      // SOS หรือ SUSTENANCE
    private Double latitude;        // จากแมพ
    private Double longitude;       // จากแมพ
    private String note;            // optional
    private Long reporterAddressId; // optional

    public CaseType getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseType caseType) {
        this.caseType = caseType;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getReporterAddressId() {
        return reporterAddressId;
    }

    public void setReporterAddressId(Long reporterAddressId) {
        this.reporterAddressId = reporterAddressId;
    }
}
