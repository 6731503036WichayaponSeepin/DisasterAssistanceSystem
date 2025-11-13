package th.mfu.controller.usercontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

        // 🔹 ต้องมีข้อมูล Detail
        if (user.getDetail() == null) {
            response.put("status", "error");
            response.put("message", "Detail is required");
            return ResponseEntity.badRequest().body(response);
        }

        Detail detail = user.getDetail();

        // 🔹 เช็กว่ามีเบอร์โทรไหม
        if (detail.getPhoneNumber() == null || detail.getPhoneNumber().isBlank()) {
            response.put("status", "error");
            response.put("message", "Phone number is required.");
            return ResponseEntity.badRequest().body(response);
        }

        // 🔹 ตรวจสอบเบอร์ซ้ำจากตาราง detail (ไม่ให้เบอร์ซ้ำกับใครทั้ง user/rescue)
        if (detailRepo.findByPhoneNumber(detail.getPhoneNumber()).isPresent()) {
            response.put("status", "error");
            response.put("message", "This phone number is already registered.");
            return ResponseEntity.badRequest().body(response);
        }

        // 🔹 บันทึก detail ก่อน
        Detail savedDetail = detailRepo.save(detail);
        user.setDetail(savedDetail);

        // 🔹 ตั้ง role เป็น USER
        user.setRole("USER");

        // 🔹 บันทึก user
        User saved = userRepo.save(user);

        response.put("status", "success");
        response.put("message", "User registered successfully");
        response.put("user", saved);

        return ResponseEntity.ok(response);
    }

    // ✅ เข้าสู่ระบบ (Login) ด้วย name + phone (จาก Detail)
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
        String name = loginRequest.get("name");
        String phoneNumber = loginRequest.get("phone_number");

        if (name == null || phoneNumber == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Name and phone number are required"
            ));
        }

        // 1) หา Detail จาก name + phoneNumber
        Optional<Detail> optDetail = detailRepo.findByNameAndPhoneNumber(name, phoneNumber);
        if (optDetail.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of(
                "status", "error",
                "message", "Invalid name or phone number"
            ));
        }
        Detail detail = optDetail.get();

        // 2) หา User จาก Detail
        Optional<User> optUser = userRepo.findByDetail(detail);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of(
                "status", "error",
                "message", "User not found for this detail"
            ));
        }

        User user = optUser.get();

        // ✅ สร้าง JWT Token (subject = phoneNumber จาก Detail)
        String token = jwtUtil.generateToken(detail.getPhoneNumber(), user.getRole());

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Login successful",
            "role", user.getRole(),
            "name", detail.getName(),
            "phoneNumber", detail.getPhoneNumber(),
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

            // ✅ ดึง phoneNumber จาก Authentication ที่ JwtAuthFilter เซ็ตไว้ (subject)
            String phoneNumber = authentication.getName();
            if (phoneNumber == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Invalid token: no phone number"
                ));
            }

            // ✅ หา user จากเบอร์ที่อยู่ใน detail
            Optional<User> optUser = userRepo.findByDetail_PhoneNumber(phoneNumber);
            if (optUser.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "User not found for " + phoneNumber
                ));
            }
            User user = optUser.get();

            Detail detail = user.getDetail();

            // ✅ สร้างข้อมูลตอบกลับแบบอ่านง่าย
            Map<String, Object> response = new HashMap<>();
            response.put("name", (detail != null) ? detail.getName() : "-");
            response.put("phoneNumber", (detail != null) ? detail.getPhoneNumber() : "-");
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

        // หา user จากเบอร์ใน detail
        Optional<User> optUser = userRepo.findByDetail_PhoneNumber(phoneNumber);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = optUser.get();

        if (user.getDetail() == null) {
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", "User has no detail assigned yet"
            ));
        }

        return ResponseEntity.ok(user.getDetail());
    }

    // ✅ อัปเดต Detail ของผู้ใช้ (แก้ไขชื่อ ฯลฯ)
    @PutMapping("/detail")
    public ResponseEntity<?> updateMyDetail(@RequestBody Detail newDetail, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String phoneNumber = authentication.getName();

        Optional<User> optUser = userRepo.findByDetail_PhoneNumber(phoneNumber);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = optUser.get();
        Detail detail = user.getDetail();
        if (detail == null) {
            detail = new Detail();
        }

        detail.setName(newDetail.getName());
        // ถ้าอยากให้แก้เบอร์ได้ด้วย เติมบรรทัดนี้ (แล้วเช็คซ้ำเอง):
        // detail.setPhoneNumber(newDetail.getPhoneNumber());

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

        Optional<User> optUser = userRepo.findByDetail_PhoneNumber(phoneNumber);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = optUser.get();

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
