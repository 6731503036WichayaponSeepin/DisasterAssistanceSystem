package th.mfu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import th.mfu.model.*;
import th.mfu.repository.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@CrossOrigin
public class AddressController {

    @Autowired private AddressRepository addressRepo;
    @Autowired private SubdistrictRepository subdistrictRepo;

    // เพิ่มที่อยู่ (บ้านเลขที่ + ข้อความเพิ่มเติม)
    @PostMapping
    public Address createAddress(@RequestBody Address address) {
        return addressRepo.save(address);
    }

    // ดึง address ทั้งหมด
    @GetMapping
    public List<Address> getAllAddresses() {
        return addressRepo.findAll();
    }
}
