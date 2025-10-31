package th.mfu.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PostalCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String code;

    @ManyToOne
    @JoinColumn(name = "subdistrict_id", nullable = false)
    private Subdistrict subdistrict;

    public PostalCode() {}
    public PostalCode(String code, Subdistrict subdistrict) {
        this.code = code;
        this.subdistrict = subdistrict;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Subdistrict getSubdistrict() { return subdistrict; }
    public void setSubdistrict(Subdistrict subdistrict) { this.subdistrict = subdistrict; }
}