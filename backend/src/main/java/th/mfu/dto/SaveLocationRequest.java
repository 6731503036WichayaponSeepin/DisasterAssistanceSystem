package th.mfu.dto;

public class SaveLocationRequest {

    private double latitude;
    private double longitude;

    // 🔵 ฟิลด์ใหม่สำหรับข้อมูลที่อยู่
    private String road;         // ถนน
    private String subdistrict;  // ตำบล
    private String district;     // อำเภอ
    private String province;     // จังหวัด
    private String postcode;     // รหัสไปรษณีย์

    public SaveLocationRequest() {}

    // ---------- Getter / Setter ----------

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

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getSubdistrict() {
        return subdistrict;
    }

    public void setSubdistrict(String subdistrict) {
        this.subdistrict = subdistrict;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }
}
