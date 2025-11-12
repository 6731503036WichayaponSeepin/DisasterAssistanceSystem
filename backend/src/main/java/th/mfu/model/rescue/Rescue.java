package th.mfu.model.rescue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import th.mfu.model.Detail;

@Entity
public class Rescue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 รหัสเฉพาะของกู้ภัย (เช่น RS-001)
    @Column(nullable = false, unique = true, length = 20)
    private String rescueId;

    // 🔹 ข้อมูลส่วนตัวของกู้ภัย (ชื่อ, เบอร์โทร, ที่อยู่ ฯลฯ)
    @ManyToOne
    @JoinColumn(name = "detail_id", nullable = false)
    private Detail detail;

    // 🔹 หน่วยสังกัด (AffiliatedUnit)
    @ManyToOne
    @JoinColumn(name = "affiliated_unit_id", nullable = false)
    private AffiliatedUnit affiliatedUnit;

    // 🔹 ทีมกู้ภัย (RescueTeam) — อาจมีหรือไม่มีทีมก็ได้
    @ManyToOne
    @JoinColumn(name = "rescue_team_id")
    @JsonIgnoreProperties({"members", "leader"})
    private RescueTeam rescueTeam;

    // 🔹 บทบาทในระบบ (ใช้สำหรับตรวจสิทธิ์การเข้า /api/rescue-teams/**)
    @Column(nullable = false)
    private String role = "RESCUE"; // ค่าเริ่มต้นคือ RESCUE

    // 🔹 Constructors
    public Rescue() {}

    public Rescue(String rescueId, Detail detail, AffiliatedUnit affiliatedUnit) {
        this.rescueId = rescueId;
        this.detail = detail;
        this.affiliatedUnit = affiliatedUnit;
        this.role = "RESCUE";
    }

    // 🔹 Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRescueId() {
        return rescueId;
    }

    public void setRescueId(String rescueId) {
        this.rescueId = rescueId;
    }

    public Detail getDetail() {
        return detail;
    }

    public void setDetail(Detail detail) {
        this.detail = detail;
    }

    public AffiliatedUnit getAffiliatedUnit() {
        return affiliatedUnit;
    }

    public void setAffiliatedUnit(AffiliatedUnit affiliatedUnit) {
        this.affiliatedUnit = affiliatedUnit;
    }

    public RescueTeam getRescueTeam() {
        return rescueTeam;
    }

    public void setRescueTeam(RescueTeam rescueTeam) {
        this.rescueTeam = rescueTeam;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // ✅ Helper — แสดงชื่อจาก Detail
    public String getName() {
        return (detail != null && detail.getName() != null)
                ? detail.getName()
                : "Unknown";
    }

    // ✅ Helper — ใช้ตรวจว่าอยู่ในทีมไหม
    public boolean isInTeam() {
        return this.rescueTeam != null;
    }
}