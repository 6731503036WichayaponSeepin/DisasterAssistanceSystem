package th.mfu.dto;

public class SaveLocationRequest {

    private double latitude;
    private double longitude;
    private String description; // เก็บที่อยู่โดยประมาณจาก Nominatim หรือข้อความอื่น ๆ

    public SaveLocationRequest() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
