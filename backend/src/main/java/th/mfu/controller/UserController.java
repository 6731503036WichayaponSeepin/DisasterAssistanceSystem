package th.mfu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.mfu.model.*;
import th.mfu.repository.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired private UserRepository userRepo;
    @Autowired private AddressRepository addressRepo;

    // สมัครสมาชิก
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userRepo.save(user);
    }

    // เข้าสู่ระบบ
    @PostMapping("/login")
public ResponseEntity<Map<String, Object>> loginUser(@RequestBody User loginRequest) {
    User user = userRepo.findByNameAndNumber(loginRequest.getName(), loginRequest.getNumber());
    Map<String, Object> response = new HashMap<>();

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


    // ดึงผู้ใช้ทั้งหมด
    @GetMapping
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // อัปเดต Address ของ User
    @PutMapping("/{number}/address/{addressId}")
    public User updateUserAddress(@PathVariable String number, @PathVariable Long addressId) {
        User user = userRepo.findByNumber(number);
        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        user.setAddress(address);
        return userRepo.save(user);
    }
}
