package th.mfu.controller.locationdatacontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import th.mfu.model.locationdata.LocationData;
import th.mfu.model.user.Address;
import th.mfu.model.user.User;
import th.mfu.repository.locationdatarepository.LocationRepository;
import th.mfu.repository.userrepository.AddressRepository;
import th.mfu.repository.userrepository.UserRepository;

@RestController
@RequestMapping("/api/location")
@CrossOrigin(origins = "*")
public class ApiMapController {

    @Autowired
    private LocationRepository locationRepo;

    @Autowired
    private AddressRepository addressRepo;

    @Autowired
    private UserRepository userRepo;

    // ✅ 1️⃣ บันทึกพิกัดใหม่จาก React Map
    @PostMapping
    public LocationData saveLocation(@RequestBody LocationRequest request, Authentication authentication) {
        // ดึงชื่อผู้ใช้จาก JWT token (subject)
        String username = authentication.getName();

        // ดึงข้อมูล User จาก DB
        User user = userRepo.findByPhoneNumber(username);
        if (user == null) {
            throw new RuntimeException("User not found for token: " + username);
        }

        // ตรวจสอบว่า address ที่ส่งมาเป็นของ user นี้หรือไม่
        Address address = addressRepo.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + request.getAddressId()));

        if (!address.equals(user.getAddress())) {
            throw new RuntimeException("Forbidden: You cannot modify another user's location");
        }

        // ✅ สร้าง LocationData และบันทึกลงฐานข้อมูล
        LocationData loc = new LocationData();
        loc.setAddress(address);
        loc.setLatitude(request.getLatitude());
        loc.setLongitude(request.getLongitude());

        return locationRepo.save(loc);
    }

    // ✅ 2️⃣ ดึงพิกัดของผู้ใช้ที่ล็อกอินอยู่
    @GetMapping
    public List<LocationData> getUserLocations(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepo.findByPhoneNumber(username);
        if (user == null) {
            throw new RuntimeException("User not found for token: " + username);
        }

        // คืนเฉพาะพิกัดของ user นี้
        return locationRepo.findAll().stream()
                .filter(l -> l.getAddress().equals(user.getAddress()))
                .toList();
    }

    // 🔹 DTO สำหรับรับข้อมูล JSON จาก React
    public static class LocationRequest {
        private Long addressId;
        private double latitude;
        private double longitude;

        public Long getAddressId() {
            return addressId;
        }
        public void setAddressId(Long addressId) {
            this.addressId = addressId;
        }
        public double getLatitude() {
            return latitude;
        }
        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }
        public double getLongitude() {
            return longitude;
        }
        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}