package th.mfu.controller.usercontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.mfu.model.Detail;
import th.mfu.model.user.Address;
import th.mfu.model.user.User;
import th.mfu.repository.DetailRepository;
import th.mfu.repository.userrepository.AddressRepository;
import th.mfu.repository.userrepository.UserRepository;
import th.mfu.security.JwtUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired private UserRepository userRepo;
    @Autowired private AddressRepository addressRepo;
    @Autowired private DetailRepository detailRepo;
    @Autowired private JwtUtil jwtUtil; // ✅ ใช้สำหรับสร้าง JWT Token

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

        // 🔹 บันทึก Detail ใหม่ (ต้องมี)
        if (user.getDetail() == null) {
            throw new RuntimeException("Detail is required");
        }
        Detail savedDetail = detailRepo.save(user.getDetail());
        user.setDetail(savedDetail);

        // 🔹 ตรวจสอบ Address (ถ้ามี)
        if (user.getAddress() != null) {
            Address address = addressRepo.findById(user.getAddress().getId())
                    .orElseThrow(() -> new RuntimeException("Address not found"));
            user.setAddress(address);
        }

        // 🔹 ตั้งค่า role เริ่มต้น
        user.setRole("USER");

        // 🔹 บันทึก User
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

        // 🔍 ค้นหา User จากชื่อ + เบอร์โทร
        User user = userRepo.findByDetail_NameAndPhoneNumber(name, phoneNumber);

        if (user != null) {
            // ✅ สร้าง JWT Token
            String token = jwtUtil.generateToken(user.getPhoneNumber(), user.getRole());

            response.put("status", "success");
            response.put("message", "Login successful");
            response.put("role", user.getRole());
            response.put("userId", user.getId());
            response.put("name", user.getDetail().getName());
            response.put("phoneNumber", user.getPhoneNumber());
            response.put("token", token); // ✅ ส่ง token กลับให้ front-end
            response.put("navigateTo", "/mainPageUser");

            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Invalid name or phone number");
            return ResponseEntity.status(401).body(response);
        }
    }

    // ✅ หน้า main ของ User (เห็นเฉพาะตัวเอง)
    @GetMapping("/main/{id}")
    public ResponseEntity<?> getUserMain(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        // ✅ ตรวจสอบ JWT ก่อน
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        try {
            String phoneFromToken = jwtUtil.parseToken(token).getBody().getSubject(); // phoneNumber
            User user = userRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ ตรวจสอบว่าคนที่ยิง request ตรงกับเจ้าของบัญชีไหม
            if (!user.getPhoneNumber().equals(phoneFromToken)) {
                return ResponseEntity.status(403).body("You are not authorized to view this data");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("name", user.getDetail().getName());
            response.put("phoneNumber", user.getPhoneNumber());
            response.put("address", user.getAddress());
            response.put("role", user.getRole());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }

    // ✅ ดึงผู้ใช้ทั้งหมด (อาจใช้เฉพาะ admin)
    @GetMapping
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // ✅ อัปเดต Address ของ User
    @PutMapping("/{phoneNumber}/address/{addressId}")
    public ResponseEntity<?> updateUserAddress(@PathVariable String phoneNumber,
                                               @PathVariable Long addressId,
                                               @RequestHeader("Authorization") String authHeader) {
        // ตรวจสอบสิทธิ์จาก JWT
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid token");
        }
        String token = authHeader.substring(7);
        String phoneFromToken = jwtUtil.parseToken(token).getBody().getSubject();

        if (!phoneNumber.equals(phoneFromToken)) {
            return ResponseEntity.status(403).body("You are not authorized to update this data");
        }

        User user = userRepo.findByPhoneNumber(phoneNumber);
        if (user == null) throw new RuntimeException("User not found");

        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        user.setAddress(address);
        userRepo.save(user);

        return ResponseEntity.ok(Map.of("status", "success", "message", "Address updated"));
    }

    // ✅ อัปเดต Detail ของ User
    @PutMapping("/{phoneNumber}/detail/{detailId}")
    public ResponseEntity<?> updateUserDetail(@PathVariable String phoneNumber,
                                              @PathVariable Long detailId,
                                              @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid token");
        }

        String token = authHeader.substring(7);
        String phoneFromToken = jwtUtil.parseToken(token).getBody().getSubject();

        if (!phoneNumber.equals(phoneFromToken)) {
            return ResponseEntity.status(403).body("You are not authorized to update this data");
        }

        User user = userRepo.findByPhoneNumber(phoneNumber);
        if (user == null) throw new RuntimeException("User not found");

        Detail detail = detailRepo.findById(detailId)
                .orElseThrow(() -> new RuntimeException("Detail not found"));

        user.setDetail(detail);
        userRepo.save(user);

        return ResponseEntity.ok(Map.of("status", "success", "message", "Detail updated"));
    }

    // ✅ ดู Detail ของ User
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
            return ResponseEntity.ok("This user has no detail assigned yet");
        }

        return ResponseEntity.ok(user.getDetail());
    }
}