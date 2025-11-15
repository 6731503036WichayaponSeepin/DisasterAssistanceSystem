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
import th.mfu.model.locationdata.LocationData;
import th.mfu.model.user.User;
import th.mfu.repository.DetailRepository;
import th.mfu.repository.locationdatarepository.LocationRepository;
import th.mfu.repository.userrepository.UserRepository;
import th.mfu.security.JwtUtil;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired private UserRepository userRepo;
    @Autowired private DetailRepository detailRepo;
    @Autowired private LocationRepository locationRepo;
    @Autowired private JwtUtil jwtUtil;

    // ===========================
    //  REGISTER
    // ===========================
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {

        if (user.getDetail() == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Detail is required"));

        Detail detail = user.getDetail();

        if (detail.getPhoneNumber() == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Phone required"));

        if (detailRepo.findByPhoneNumber(detail.getPhoneNumber()).isPresent())
            return ResponseEntity.badRequest().body(Map.of("error", "Phone already used"));

        Detail savedDetail = detailRepo.save(detail);
        user.setDetail(savedDetail);

        // ⭐ ผู้ใช้ใหม่ยังไม่มีบ้าน (location = null)
        user.setLocationId(null);
        user.setRole("USER");

        User saved = userRepo.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "User registered",
                "user", saved
        ));
    }

    // ===========================
    //  LOGIN
    // ===========================
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginReq) {

        String name = loginReq.get("name");
        String phone = loginReq.get("phone_number");

        Optional<Detail> optDetail = detailRepo.findByNameAndPhoneNumber(name, phone);
        if (optDetail.isEmpty())
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));

        Detail detail = optDetail.get();

        Optional<User> optUser = userRepo.findByDetail(detail);
        if (optUser.isEmpty())
            return ResponseEntity.status(401).body(Map.of("error", "User not found"));

        User user = optUser.get();

        String token = jwtUtil.generateToken(detail.getPhoneNumber(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "message", "Login success",
                "token", token,
                "role", user.getRole(),
                "name", detail.getName(),
                "phoneNumber", detail.getPhoneNumber()
        ));
    }

    // ===========================
    //  PROFILE
    // ===========================
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication auth) {

        if (auth == null)
            return ResponseEntity.status(401).body("Unauthorized");

        String phone = auth.getName();

        Optional<User> optUser = userRepo.findByDetail_PhoneNumber(phone);
        if (optUser.isEmpty())
            return ResponseEntity.status(404).body("User not found");

        User user = optUser.get();
        Detail detail = user.getDetail();

        Map<String, Object> result = new HashMap<>();
        result.put("name", detail.getName());
        result.put("phoneNumber", detail.getPhoneNumber());
        result.put("role", user.getRole());

        // ⭐ ใช้ locationData แทน address
        LocationData loc = user.getLocationId();
        if (loc != null) {
            result.put("location", Map.of(
                    "id", loc.getId(),
                    "latitude", loc.getLatitude(),
                    "longitude", loc.getLongitude(),
                    "road", loc.getRoad(),
                    "subdistrict", loc.getSubdistrict(),
                    "district", loc.getDistrict(),
                    "province", loc.getProvince(),
                    "postcode", loc.getPostcode()
            ));
        } else {
            result.put("location", null);
        }

        return ResponseEntity.ok(result);
    }
}
