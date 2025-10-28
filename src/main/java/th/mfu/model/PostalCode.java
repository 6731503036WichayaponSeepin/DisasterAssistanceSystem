package th.mfu.model;

import jakarta.persistence.*;

@Entity
public class PostalCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @ManyToOne
    @JoinColumn(name = "subdistrict_id")
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
