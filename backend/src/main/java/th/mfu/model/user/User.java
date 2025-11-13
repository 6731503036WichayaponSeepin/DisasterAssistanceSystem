package th.mfu.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import th.mfu.model.Detail;

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
    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    // 🔹 Role
    @Column(nullable = false)
    private String role = "USER";

    public User() {}

    public User( Address address, Detail detail, String role) {
        
        this.address = address;
        this.detail = detail;
        this.role = role;
    }

    // ✅ Getters & Setters
    public Long getId() { return id; }

    
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public Detail getDetail() { return detail; }
    public void setDetail(Detail detail) { this.detail = detail; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}