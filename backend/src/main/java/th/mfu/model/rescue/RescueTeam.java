package th.mfu.model.rescue;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import th.mfu.model.user.District;

@Entity
public class RescueTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 ชื่อทีม
    @Column(nullable = false)
    private String name;

    // 🔹 รหัสทีม (ไม่ซ้ำ)
    @Column(nullable = false, unique = true)
    private String teamId;

    // 🔹 ทีมนี้อยู่ในอำเภอใด
    @ManyToOne
    @JoinColumn(name = "district_id")
    private District district;

    // 🔹 หัวหน้าทีม (RescueLeader 1 คนต่อทีม)
    @ManyToOne
    @JoinColumn(name = "leader_id", nullable = false)
    private Rescue leader;

    // 🔹 สมาชิกในทีม (รวมทั้งหัวหน้าเองด้วย)
    @OneToMany(mappedBy = "rescueTeam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Rescue> members;

    // 🔹 Constructors
    public RescueTeam() {}

    public RescueTeam(String name, String teamId, District district, Rescue leader) {
        this.name = name;
        this.teamId = teamId;
        this.district = district;
        this.leader = leader;
    }

    // 🔹 Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public Rescue getLeader() {
        return leader;
    }

    public void setLeader(Rescue leader) {
        this.leader = leader;
    }

    public List<Rescue> getMembers() {
        return members;
    }

    public void setMembers(List<Rescue> members) {
        this.members = members;
    }

    // ✅ Helper Method — ใช้ดึงชื่อหัวหน้าทีมได้สะดวก
    public String getLeaderName() {
        if (leader != null && leader.getDetail() != null) {
            return leader.getDetail().getName();
        }
        return "Unknown Leader";
    }

    // ✅ Helper Method — ใช้ดึงจำนวนสมาชิกในทีม
    public int getMemberCount() {
        return (members != null) ? members.size() : 0;
    }

    // ✅ Helper Method — ใช้ตรวจว่า Rescue คนนี้เป็นหัวหน้าทีมนี้ไหม
    public boolean isLeader(Rescue rescue) {
        return (leader != null && rescue != null && leader.getId().equals(rescue.getId()));
    }
}
