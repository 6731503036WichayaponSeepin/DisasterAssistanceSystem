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
import th.mfu.model.user.Address;
import th.mfu.model.user.User;
import th.mfu.repository.locationdatarepository.LocationRepository;
import th.mfu.repository.userrepository.UserRepository;

@RestController
@RequestMapping("/api/user-location")   // 👈 ไม่ชนของเดิมแล้ว
@CrossOrigin
public class UserLocationController {

    @Autowired
    private LocationRepository locationRepo;

    @Autowired
    private UserRepository userRepo;

    // ------------------------------------------
    // 🔵 บันทึกตำแหน่งบ้านของ user
    // ------------------------------------------
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

            Address address = user.getAddress();
            if (address == null) {
                return ResponseEntity.badRequest()
                        .body("User has no address saved. Please add address first.");
            }

            // หา location เดิมจาก address นี้
            LocationData locationData = locationRepo.findByAddress_Id(address.getId())
                    .orElseGet(LocationData::new);

            locationData.setLatitude(req.getLatitude());
            locationData.setLongitude(req.getLongitude());
            locationData.setAddress(address);

            if (locationData.getId() == null) {
                locationData.setConfirmed(false);
                locationData.setFollowed(false);
            }

            LocationData saved = locationRepo.save(locationData);

            Map<String, Object> res = new HashMap<>();
            res.put("status", "success");
            res.put("message", "Home location saved successfully");
            res.put("locationId", saved.getId());

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error saving location: " + e.getMessage());
        }
    }

    // ------------------------------------------
    // 🔵 โหลดตำแหน่งบ้านของ user
    // ------------------------------------------
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

            Address address = user.getAddress();
            if (address == null) {
                return ResponseEntity.status(404).body("User has no address saved");
            }

            Optional<LocationData> optLoc = locationRepo.findByAddress_Id(address.getId());
            if (optLoc.isEmpty()) {
                return ResponseEntity.status(404).body("Location not found");
            }

            LocationData loc = optLoc.get();

            Map<String, Object> res = new HashMap<>();
            res.put("latitude", loc.getLatitude());
            res.put("longitude", loc.getLongitude());
            res.put("addressId", address.getId());

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error loading location: " + e.getMessage());
        }
    }
}
