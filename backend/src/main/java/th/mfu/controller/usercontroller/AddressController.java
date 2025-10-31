package th.mfu.controller.usercontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import th.mfu.model.user.Address;
import th.mfu.repository.userrepository.AddressRepository;
import th.mfu.repository.userrepository.SubdistrictRepository;

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