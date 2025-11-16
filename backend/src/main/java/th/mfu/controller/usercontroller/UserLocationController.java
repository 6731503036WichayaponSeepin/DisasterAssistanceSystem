package th.mfu.controller.usercontroller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import th.mfu.dto.SaveLocationRequest;
import th.mfu.model.locationdata.LocationData;
import th.mfu.model.user.User;
import th.mfu.repository.locationdatarepository.LocationRepository;
import th.mfu.repository.userrepository.UserRepository;

@RestController
@RequestMapping("/api/user-location")
@CrossOrigin
public class UserLocationController {

    @Autowired
    private LocationRepository locationRepo;

    @Autowired
    private UserRepository userRepo;

    // -------------------------------------------------------
    // 🔵 บันทึกตำแหน่งบ้านของ user (ใช้ user_id)
    // -------------------------------------------------------
    @PostMapping("/home")
    public ResponseEntity<?> saveMyHomeLocation(
            @RequestBody SaveLocationRequest req,
            Authentication authentication) {

        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            // subject = phoneNumber
            String phoneNumber = authentication.getName();

            Optional<User> optUser = userRepo.findByDetail_PhoneNumber(phoneNumber);
            if (optUser.isEmpty()) {
                return ResponseEntity.status(404).body("User not found");
            }

            User user = optUser.get();

            // หา location เดิมตาม user_id (มีแค่ตำแหน่งเดียว)
            LocationData location = locationRepo.findFirstByUserOrderByIdDesc(user)
                    .orElse(new LocationData());

            // บันทึกข้อมูลตำแหน่ง
            location.setUser(user);
            location.setLatitude(req.getLatitude());
            location.setLongitude(req.getLongitude());

            // 🔵 บันทึก field ที่อยู่แบบแยก (ถ้ามี)
            location.setRoad(req.getRoad());
            location.setSubdistrict(req.getSubdistrict());
            location.setDistrict(req.getDistrict());
            location.setProvince(req.getProvince());
            location.setPostcode(req.getPostcode());

           LocationData saved = locationRepo.save(location);

// ⭐ อัปเดต foreign key ในตาราง user
user.setLocationId(saved);
userRepo.save(user);

Map<String, Object> res = new HashMap<>();
res.put("status", "success");
res.put("locationId", saved.getId());

return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                    "Error saving location: " + e.getMessage()
            );
        }
    }

    // -------------------------------------------------------
    // 🔵 โหลดตำแหน่งบ้านของ user ( lat / lng + address )
    // -------------------------------------------------------
   @GetMapping("/home")
public ResponseEntity<?> getMyHomeLocation(Authentication authentication) {

    try {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String phoneNumber = authentication.getName();

        Optional<User> optUser = userRepo.findByDetail_PhoneNumber(phoneNumber);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = optUser.get();

        Optional<LocationData> optLoc = locationRepo.findFirstByUserOrderByIdDesc(user);
        if (optLoc.isEmpty()) {
            return ResponseEntity.status(404).body("Location not found");
        }

        LocationData loc = optLoc.get();

        Map<String, Object> res = new HashMap<>();
        res.put("id", loc.getId());                // ⭐⭐⭐⭐ ต้องเพิ่ม
        res.put("latitude", loc.getLatitude());
        res.put("longitude", loc.getLongitude());
        res.put("road", loc.getRoad());
        res.put("subdistrict", loc.getSubdistrict());
        res.put("district", loc.getDistrict());
        res.put("province", loc.getProvince());
        res.put("postcode", loc.getPostcode());

        return ResponseEntity.ok(res);

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().body(
                "Error loading location: " + e.getMessage()
        );
    }
}

}
