package th.mfu.controller.usercontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import th.mfu.model.user.Address;
import th.mfu.model.user.Subdistrict;
import th.mfu.model.user.User;
import th.mfu.repository.userrepository.AddressRepository;
import th.mfu.repository.userrepository.SubdistrictRepository;
import th.mfu.repository.userrepository.UserRepository;

@RestController
@RequestMapping("/api/address")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AddressController {

    @Autowired private AddressRepository addressRepo;
    @Autowired private SubdistrictRepository subdistrictRepo;
    @Autowired private UserRepository userRepo;

    // ✅ เพิ่มหรืออัปเดต (ครั้งแรก)
    @PostMapping
    public ResponseEntity<?> createOrUpdateAddress(@RequestBody Address address, Authentication authentication) {
        try {
            if (authentication == null) return ResponseEntity.status(401).body("Unauthorized");
            String phoneNumber = authentication.getName();
            User user = userRepo.findByPhoneNumber(phoneNumber);
            if (user == null) return ResponseEntity.status(404).body("User not found");

            if (address.getSubdistrict() == null || address.getSubdistrict().getId() == null)
                return ResponseEntity.badRequest().body("Subdistrict is required");

            Subdistrict subdistrict = subdistrictRepo.findById(address.getSubdistrict().getId())
                    .orElseThrow(() -> new RuntimeException("Subdistrict not found"));

            Address existingAddress = user.getAddress();
            Address savedAddress;

            if (existingAddress != null) {
                existingAddress.setHouseNumber(address.getHouseNumber());
                existingAddress.setMoreDetails(address.getMoreDetails());
                existingAddress.setSubdistrict(subdistrict);
                savedAddress = addressRepo.save(existingAddress);
            } else {
                address.setSubdistrict(subdistrict);
                savedAddress = addressRepo.save(address);
                user.setAddress(savedAddress);
                userRepo.save(user);
            }

            return ResponseEntity.ok(savedAddress);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error saving address: " + e.getMessage());
        }
    }

    // ✅ ดึง address ทั้งหมด (เฉพาะ admin)
    @GetMapping
    public List<Address> getAllAddresses() {
        return addressRepo.findAll();
    }

    // ✅ ดึงที่อยู่ของผู้ใช้ที่ล็อกอิน
    @GetMapping("/my")
    public ResponseEntity<?> getMyAddress(Authentication authentication) {
        try {
            if (authentication == null) return ResponseEntity.status(401).body("Unauthorized");
            String phoneNumber = authentication.getName();
            User user = userRepo.findByPhoneNumber(phoneNumber);
            if (user == null) return ResponseEntity.status(404).body("User not found");

            Address address = user.getAddress();
            if (address == null) return ResponseEntity.status(404).body("Address not found");

            return ResponseEntity.ok(address);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error loading address: " + e.getMessage());
        }
    }

    // ✅ อัปเดตที่อยู่ของตัวเอง
    @PutMapping("/update")
    public ResponseEntity<?> updateMyAddress(@RequestBody Address updatedAddress, Authentication authentication) {
        try {
            if (authentication == null) return ResponseEntity.status(401).body("Unauthorized");
            String phoneNumber = authentication.getName();

            User user = userRepo.findByPhoneNumber(phoneNumber);
            if (user == null) return ResponseEntity.status(404).body("User not found");

            Address currentAddress = user.getAddress();
            if (currentAddress == null) return ResponseEntity.status(404).body("Address not found");

            // ตรวจสอบ Subdistrict
            if (updatedAddress.getSubdistrict() == null || updatedAddress.getSubdistrict().getId() == null)
                return ResponseEntity.badRequest().body("Subdistrict is required");

            Subdistrict subdistrict = subdistrictRepo.findById(updatedAddress.getSubdistrict().getId())
                    .orElseThrow(() -> new RuntimeException("Subdistrict not found"));

            // ✅ อัปเดตค่าใหม่
            currentAddress.setHouseNumber(updatedAddress.getHouseNumber());
            currentAddress.setMoreDetails(updatedAddress.getMoreDetails());
            currentAddress.setSubdistrict(subdistrict);

            Address saved = addressRepo.save(currentAddress);

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error updating address: " + e.getMessage());
        }
    }
}
