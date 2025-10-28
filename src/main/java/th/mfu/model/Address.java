package th.mfu.model;

import jakarta.persistence.*;

@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String houseNumber;
    private String moreDetails; // รายละเอียดเพิ่มเติม

    @ManyToOne
    @JoinColumn(name = "subdistrict_id")
    private Subdistrict subdistrict;

    public Address() {}

    public Address(String houseNumber, String moreDetails, Subdistrict subdistrict) {
        this.houseNumber = houseNumber;
        this.moreDetails = moreDetails;
        this.subdistrict = subdistrict;
    }

    // Getters & Setters
    public Long getId() { return id; }

    public String getHouseNumber() { return houseNumber; }
    public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }

    public String getMoreDetails() { return moreDetails; }
    public void setMoreDetails(String moreDetails) { this.moreDetails = moreDetails; }

    public Subdistrict getSubdistrict() { return subdistrict; }
    public void setSubdistrict(Subdistrict subdistrict) { this.subdistrict = subdistrict; }
}
