package th.mfu.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String moreDetails;

    @ManyToOne
    @JoinColumn(name = "subdistrict_id", nullable = false)
    private Subdistrict subdistrict;

    public Address() {}

    public Address(String moreDetails, Subdistrict subdistrict) {
        this.moreDetails = moreDetails;
        this.subdistrict = subdistrict;
    }

    // Getters & Setters
    public Long getId() { return id; }

    public String getMoreDetails() { return moreDetails; }
    public void setMoreDetails(String moreDetails) { this.moreDetails = moreDetails; }

    public Subdistrict getSubdistrict() { return subdistrict; }
    public void setSubdistrict(Subdistrict subdistrict) { this.subdistrict = subdistrict; }
}