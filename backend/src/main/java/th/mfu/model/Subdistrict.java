package th.mfu.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Subdistrict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "district_id")
    private District district;

    @OneToMany(mappedBy = "subdistrict", cascade = CascadeType.ALL)
    private List<PostalCode> postalCodes;

    public Subdistrict() {}
    public Subdistrict(String name, District district) {
        this.name = name;
        this.district = district;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public District getDistrict() { return district; }
    public void setDistrict(District district) { this.district = district; }
}
