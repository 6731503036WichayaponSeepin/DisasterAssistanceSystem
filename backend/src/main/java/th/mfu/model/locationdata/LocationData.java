package th.mfu.model.locationdata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import th.mfu.model.user.User;

@Entity
@Table(name = "location_data")
public class LocationData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ผูกตำแหน่งกับ user โดยตรง
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    // (ถ้าจะลบ address ออกทีหลัง แจ้งได้)
   
    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = true)
private String road;

@Column(nullable = true)
private String subdistrict;

@Column(nullable = true)
private String district;

@Column(nullable = true)
private String province;

@Column(nullable = true)
private String postcode;

    // ---------- Constructors ----------
    public LocationData() {}

    public LocationData(User user, double latitude, double longitude) {
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // ---------- Getter & Setter ----------
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    

  

    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getRoad() { return road; }
public void setRoad(String road) { this.road = road; }

public String getSubdistrict() { return subdistrict; }
public void setSubdistrict(String subdistrict) { this.subdistrict = subdistrict; }

public String getDistrict() { return district; }
public void setDistrict(String district) { this.district = district; }

public String getProvince() { return province; }
public void setProvince(String province) { this.province = province; }

public String getPostcode() { return postcode; }
public void setPostcode(String postcode) { this.postcode = postcode; }

@Transient
public String getFullAddress() {
    StringBuilder sb = new StringBuilder();

    if (road != null) sb.append(road).append(" ");
    if (subdistrict != null) sb.append(subdistrict).append(" ");
    if (district != null) sb.append(district).append(" ");
    if (province != null) sb.append(province).append(" ");
    if (postcode != null) sb.append(postcode);

    return sb.toString().trim();
}

}