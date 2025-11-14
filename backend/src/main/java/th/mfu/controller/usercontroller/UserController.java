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

    // ===========================
    //   REGISTER USER
    // ===========================
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        if (user.getDetail() == null) {
            response.put("status", "error");
            response.put("message", "Detail is required");
            return ResponseEntity.badRequest().body(response);
        }

        Detail detail = user.getDetail();

        if (detail.getPhoneNumber() == null || detail.getPhoneNumber().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Phone number is required"
            ));
        }

        if (detailRepo.findByPhoneNumber(detail.getPhoneNumber()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "This phone number is already registered"
            ));
        }

        Detail savedDetail = detailRepo.save(detail);
        user.setDetail(savedDetail);

        user.setRole("USER");

        User savedUser = userRepo.save(user);

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "User registered successfully",
            "user", savedUser
        ));
    }

    // ===========================
    //   LOGIN USER
    // ===========================
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

        Optional<Detail> optDetail = detailRepo.findByNameAndPhoneNumber(name, phoneNumber);
        if (optDetail.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of(
                "status", "error",
                "message", "Invalid name or phone number"
            ));
        }

        Detail detail = optDetail.get();

        Optional<User> optUser = userRepo.findByDetail(detail);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of(
                "status", "error",
                "message", "User not found for this detail"
            ));
        }

        User user = optUser.get();

        // Generate JWT
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

    // ===========================
    //   GET ALL USERS
    // ===========================
    @GetMapping
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // ===========================
    //   GET PROFILE OF LOGGED USER
    // ===========================
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized"
                ));
            }

            String phoneNumber = authentication.getName();

            Optional<User> optUser = userRepo.findByDetail_PhoneNumber(phoneNumber);
            if (optUser.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "User not found for phone: " + phoneNumber
                ));
            }

            User user = optUser.get();
            Detail detail = user.getDetail();

            Map<String, Object> response = new HashMap<>();
            response.put("name", detail != null ? detail.getName() : "-");
            response.put("phoneNumber", detail != null ? detail.getPhoneNumber() : "-");
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

    // ===========================
    //   GET DETAIL OF SPECIFIC USER
    // ===========================
    @GetMapping("/{phoneNumber}/detail")
    public ResponseEntity<?> getUserDetail(
            @PathVariable String phoneNumber,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid token");
        }

        String token = authHeader.substring(7);
        String phoneFromToken = jwtUtil.parseToken(token).getBody().getSubject();

        if (!phoneNumber.equals(phoneFromToken)) {
            return ResponseEntity.status(403).body("You are not authorized to view this data");
        }

        Optional<User> optUser = userRepo.findByDetail_PhoneNumber(phoneNumber);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = optUser.get();

        if (user.getDetail() == null) {
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", "User has no detail data"
            ));
        }

        return ResponseEntity.ok(user.getDetail());
    }

    // ===========================
    //   UPDATE USER DETAIL
    // ===========================
    @PutMapping("/detail")
    public ResponseEntity<?> updateMyDetail(
            @RequestBody Detail newDetail,
            Authentication authentication) {

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
        detailRepo.save(detail);

        user.setDetail(detail);
        userRepo.save(user);

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Detail updated successfully",
            "detail", detail
        ));
    }

    // ===========================
    //   UPDATE ADDRESS
    // ===========================
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
