package th.mfu.dto;

public class CaseSelectionResponse {

    private Long caseId;
    private String reporterName;
    private String reporterPhone;
    private String severity;   // HIGH / MEDIUM / LOW
    private String address;
    private Double lat;
    private Double lon;

    public CaseSelectionResponse(Long caseId, String reporterName, String reporterPhone,
                                 String severity, String address, Double lat, Double lon) {
        this.caseId = caseId;
        this.reporterName = reporterName;
        this.reporterPhone = reporterPhone;
        this.severity = severity;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
    }

    // getters & setters

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
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

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

}