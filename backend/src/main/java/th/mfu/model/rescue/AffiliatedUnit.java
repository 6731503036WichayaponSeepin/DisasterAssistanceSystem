package th.mfu.model.rescue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AffiliatedUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String unitName;

    public AffiliatedUnit() {}

    public AffiliatedUnit(String unitName) {
        this.unitName = unitName;
    }

    // Getters & Setters
    public Long getId() { return id; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
}