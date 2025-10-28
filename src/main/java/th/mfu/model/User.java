package th.mfu.model;

import jakarta.persistence.*;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String number;

    // ผู้ใช้จะมีที่อยู่ (foreign key ไปยัง Address)
    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    public User() {}

    public User(String name, String number, Address address) {
        this.name = name;
        this.number = number;
        this.address = address;
    }

    // Getters & Setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
}
