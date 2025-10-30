package th.mfu.controller.usercontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import th.mfu.model.Detail;
import th.mfu.model.user.Address;
import th.mfu.model.user.User;
import th.mfu.repository.DetailRepository;
import th.mfu.repository.userrepository.AddressRepository;
import th.mfu.repository.userrepository.UserRepository;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired private UserRepository userRepo;
    @Autowired private AddressRepository addressRepo;
    @Autowired private DetailRepository detailRepo;

    // ✅ สมัครสมาชิก
@PostMapping("/register")
public ResponseEntity<?> registerUser(@RequestBody User user) {
    Map<String, Object> response = new HashMap<>();

    // 🔹 ตรวจสอบเบอร์ซ้ำ
    if (userRepo.findByPhoneNumber(user.getPhoneNumber()) != null) {
        response.put("status", "error");
        response.put("message", "This phone number is already registered.");
        return ResponseEntity.badRequest().body(response);
    }

    // 🔹 บันทึก Detail ใหม่
    Detail newDetail = user.getDetail();
    if (newDetail != null) {
        newDetail = detailRepo.save(newDetail);
        user.setDetail(newDetail);
    } else {
        throw new RuntimeException("Detail is required");
    }

    // 🔹 ตรวจสอบ Address (ถ้ามี)
    if (user.getAddress() != null) {
        Long addressId = user.getAddress().getId();
        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + addressId));
        user.setAddress(address);
    }

    // 🔹 บันทึก User สุดท้าย
    User saved = userRepo.save(user);

    response.put("status", "success");
    response.put("message", "User registered successfully");
    response.put("user", saved);
    return ResponseEntity.ok(response);
}

    // ✅ เข้าสู่ระบบด้วยชื่อ (จาก detail.name) และหมายเลขโทรศัพท์
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
        String name = loginRequest.get("name");
        String phoneNumber = loginRequest.get("phone_number");

        Map<String, Object> response = new HashMap<>();

        User user = userRepo.findByDetail_NameAndPhoneNumber(name, phoneNumber);

        if (user != null) {
            response.put("status", "success");
            response.put("message", "Login successful");
            response.put("user", user);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Invalid name or phone number");
            return ResponseEntity.status(401).body(response);
        }
    }

    // ✅ ดึงผู้ใช้ทั้งหมด
    @GetMapping
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // ✅ อัปเดต Address ของ User
    @PutMapping("/{phoneNumber}/address/{addressId}")
    public User updateUserAddress(@PathVariable String phoneNumber, @PathVariable Long addressId) {
        User user = userRepo.findByPhoneNumber(phoneNumber);
        if (user == null) throw new RuntimeException("User not found");

        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        user.setAddress(address);
        return userRepo.save(user);
    }

    // ✅ อัปเดต Detail ของ User
    @PutMapping("/{phoneNumber}/detail/{detailId}")
    public User updateUserDetail(@PathVariable String phoneNumber, @PathVariable Long detailId) {
        User user = userRepo.findByPhoneNumber(phoneNumber);
        if (user == null) throw new RuntimeException("User not found");

        Detail detail = detailRepo.findById(detailId)
                .orElseThrow(() -> new RuntimeException("Detail not found"));

        user.setDetail(detail);
        return userRepo.save(user);
    }

    // ✅ ดู Detail ของผู้ใช้
    @GetMapping("/{phoneNumber}/detail")
    public ResponseEntity<?> getUserDetail(@PathVariable String phoneNumber) {
        User user = userRepo.findByPhoneNumber(phoneNumber);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        if (user.getDetail() == null) {
            return ResponseEntity.ok("This user has no detail assigned yet");
        }

        return ResponseEntity.ok(user.getDetail());
    }

    // ✅ ดึงผู้ใช้ทั้งหมดใน Detail เดียวกัน
    @GetMapping("/detail/{detailId}")
    public List<User> getUsersByDetail(@PathVariable Long detailId) {
        Detail detail = detailRepo.findById(detailId)
                .orElseThrow(() -> new RuntimeException("Detail not found"));
        return userRepo.findByDetail(detail);
    }
}
