package th.mfu.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @OneToMany(mappedBy = "district", cascade = CascadeType.ALL)
    private List<Subdistrict> subdistricts;

    public District() {}
    public District(String name, Province province) {
        this.name = name;
        this.province = province;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Province getProvince() { return province; }
    public void setProvince(Province province) { this.province = province; }
}
