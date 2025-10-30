package th.mfu.model.user;

import jakarta.persistence.*;

@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String moreDetails; // รายละเอียดเพิ่มเติม

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
