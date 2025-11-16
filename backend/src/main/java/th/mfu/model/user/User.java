package th.mfu.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import th.mfu.model.Detail;
import th.mfu.model.locationdata.LocationData;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   

    // 🔹 ผู้ใช้เชื่อมกับข้อมูลส่วนกลาง (Detail)
    @ManyToOne
    @JoinColumn(name = "detail_id", nullable = false)
    private Detail detail;

    // 🔹 ผู้ใช้มีที่อยู่
    @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "location_id", nullable = true)
private LocationData locationId;


    // 🔹 Role
    @Column(nullable = false)
    private String role = "USER";

    public User() {}

    public User(  Detail detail, String role) {
        
        
        this.detail = detail;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Detail getDetail() {
        return detail;
    }

    public void setDetail(Detail detail) {
        this.detail = detail;
    }

    public LocationData getLocationId() {
        return locationId;
    }

    public void setLocationId(LocationData locationId) {
        this.locationId = locationId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // ✅ Getters & Setters
   
}