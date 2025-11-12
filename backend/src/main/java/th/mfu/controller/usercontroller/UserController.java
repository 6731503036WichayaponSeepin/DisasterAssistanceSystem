package th.mfu.controller.usercontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import th.mfu.model.Detail;
import th.mfu.model.user.Address;
import th.mfu.model.user.User;
import th.mfu.repository.DetailRepository;
import th.mfu.repository.userrepository.AddressRepository;
import th.mfu.repository.userrepository.UserRepository;
import th.mfu.security.JwtUtil;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired private UserRepository userRepo;
    @Autowired private AddressRepository addressRepo;
    @Autowired private DetailRepository detailRepo;
    @Autowired private JwtUtil jwtUtil;

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

        // 🔹 ต้องมีข้อมูลส่วนตัว (Detail)
        if (user.getDetail() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Detail is required"));
        }

        Detail savedDetail = detailRepo.save(user.getDetail());
        user.setDetail(savedDetail);
        user.setRole("USER");

        // 🔹 บันทึก user
        User saved = userRepo.save(user);
        response.put("status", "success");
        response.put("message", "User registered successfully");
        response.put("user", saved);
        return ResponseEntity.ok(response);
    }

    // ✅ เข้าสู่ระบบ (Login)
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
        String name = loginRequest.get("name");
        String phoneNumber = loginRequest.get("phone_number");

        User user = userRepo.findByDetail_NameAndPhoneNumber(name, phoneNumber);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Invalid name or phone number"));
        }

        // ✅ สร้าง JWT Token
        String token = jwtUtil.generateToken(user.getPhoneNumber(), user.getRole());

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Login successful",
            "role", user.getRole(),
            "name", user.getDetail().getName(),
            "phoneNumber", user.getPhoneNumber(),
            "token", token
        ));
    }

    // ✅ ดึงผู้ใช้ทั้งหมด (เฉพาะ admin)
    @GetMapping
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // ✅ ดึงข้อมูลโปรไฟล์ของผู้ใช้ที่ login อยู่
@GetMapping("/me")
public ResponseEntity<?> getMyProfile(Authentication authentication) {
    try {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unauthorized - Missing authentication"
            ));
        }

        // ✅ ดึง phoneNumber จาก Authentication ที่ JwtAuthFilter เซ็ตไว้
        String phoneNumber = authentication.getName();
        if (phoneNumber == null) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Invalid token: no phone number"
            ));
        }

        // ✅ ค้นหา user จาก phoneNumber
        User user = userRepo.findByPhoneNumber(phoneNumber);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of(
                "error", "User not found for " + phoneNumber
            ));
        }

        // ✅ สร้างข้อมูลตอบกลับแบบอ่านง่าย
        Map<String, Object> response = new HashMap<>();
        response.put("name", user.getDetail() != null ? user.getDetail().getName() : "-");
        response.put("phoneNumber", user.getPhoneNumber());
        response.put("role", user.getRole());
        response.put("address", user.getAddress());

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().body(Map.of(
            "error", "Internal Server Error",
            "message", e.getMessage()
        ));
    }
}


    // ✅ ดึง Detail ของผู้ใช้ (เฉพาะเจ้าของเท่านั้น)
    @GetMapping("/{phoneNumber}/detail")
    public ResponseEntity<?> getUserDetail(@PathVariable String phoneNumber,
                                           @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid token");
        }

        String token = authHeader.substring(7);
        String phoneFromToken = jwtUtil.parseToken(token).getBody().getSubject();

        if (!phoneNumber.equals(phoneFromToken)) {
            return ResponseEntity.status(403).body("You are not authorized to view this data");
        }

        User user = userRepo.findByPhoneNumber(phoneNumber);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        if (user.getDetail() == null) {
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", "User has no detail assigned yet"
            ));
        }

        return ResponseEntity.ok(user.getDetail());
    }

    // ✅ อัปเดต Detail ของผู้ใช้ (แก้ไขชื่อ, นามสกุล, ฯลฯ)
    @PutMapping("/detail")
    public ResponseEntity<?> updateMyDetail(@RequestBody Detail newDetail, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String phoneNumber = authentication.getName();
        User user = userRepo.findByPhoneNumber(phoneNumber);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        Detail detail = user.getDetail();
        if (detail == null) {
            detail = new Detail();
        }

        detail.setName(newDetail.getName());
        detailRepo.save(detail);

        user.setDetail(detail);
        userRepo.save(user);

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Detail updated successfully",
            "detail", detail
        ));
    }

    // ✅ อัปเดต Address ของผู้ใช้
    @PutMapping("/address")
    public ResponseEntity<?> updateMyAddress(@RequestBody Address newAddress, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String phoneNumber = authentication.getName();
        User user = userRepo.findByPhoneNumber(phoneNumber);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        Address address = user.getAddress();
        if (address == null) {
            address = new Address();
        }

        address.setHouseNumber(newAddress.getHouseNumber());
        address.setMoreDetails(newAddress.getMoreDetails());
        address.setSubdistrict(newAddress.getSubdistrict());
        addressRepo.save(address);

        user.setAddress(address);
        userRepo.save(user);

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Address updated successfully",
            "address", address
        ));
    }
}