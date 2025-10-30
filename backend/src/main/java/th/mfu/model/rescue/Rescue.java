package th.mfu.model.rescue;

import jakarta.persistence.*;
import th.mfu.model.Detail;

@Entity
public class Rescue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 รหัสเฉพาะของกู้ภัย
    @Column(nullable = false, unique = true, length = 20)
    private String rescueId;

    // 🔹 Foreign Key ไปยัง Detail (ข้อมูลส่วนตัว)
    @ManyToOne
    @JoinColumn(name = "detail_id", nullable = false)
    private Detail detail;

    @ManyToOne
    @JoinColumn(name = "rescue_team_id")
    private RescueTeam rescueTeam;

    // 🔹 Foreign Key ไปยัง AffiliatedUnit (หน่วยสังกัด)
    @ManyToOne
    @JoinColumn(name = "affiliated_unit_id", nullable = false)
    private AffiliatedUnit affiliatedUnit;

    // 🔹 Constructors
    public Rescue() {}

    public Rescue(String rescueId, Detail detail, AffiliatedUnit affiliatedUnit) {
        this.rescueId = rescueId;
        this.detail = detail;
        this.affiliatedUnit = affiliatedUnit;
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

    public RescueTeam getRescueTeam() {
        return rescueTeam;
    }

    public void setRescueTeam(RescueTeam rescueTeam) {
        this.rescueTeam = rescueTeam;
    }

    public AffiliatedUnit getAffiliatedUnit() {
        return affiliatedUnit;
    }

    public void setAffiliatedUnit(AffiliatedUnit affiliatedUnit) {
        this.affiliatedUnit = affiliatedUnit;
    }
}