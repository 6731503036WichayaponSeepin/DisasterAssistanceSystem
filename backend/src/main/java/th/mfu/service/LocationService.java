package th.mfu.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import th.mfu.model.locationdata.LocationData;
import th.mfu.model.user.User;
import th.mfu.repository.locationdatarepository.LocationRepository;
import th.mfu.repository.userrepository.UserRepository;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepo;

    @Autowired
    private UserRepository userRepo;

    /**
     * สร้าง LocationData ใหม่แบบ manual (ไม่ใช้ Address entity)
     */
    public LocationData saveLocationForUser(
            Long userId,
            double lat,
            double lng,
            String road,
            String subdistrict,
            String district,
            String province,
            String postcode
    ) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        LocationData loc = new LocationData();
        loc.setUser(user);
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        loc.setRoad(road);
        loc.setSubdistrict(subdistrict);
        loc.setDistrict(district);
        loc.setProvince(province);
        loc.setPostcode(postcode);

        return locationRepo.save(loc);
    }

    /**
     * Reverse Geocode → แปลง lat/lng เป็น address (ใช้ Nominatim)
     */
    public String reverseGeocode(double lat, double lng) throws IOException {
        String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat="
                + lat + "&lon=" + lng;

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", "mfu-disaster-assist/1.0");
        conn.setRequestMethod("GET");

        StringBuilder json = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = br.readLine()) != null) {
                json.append(line);
            }
        }

        // extract only display_name
        Pattern p = Pattern.compile("\"display_name\":\"(.*?)\"");
        Matcher m = p.matcher(json.toString());

        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    /**
     * ดึง LocationData ทั้งหมด
     */
    public List<LocationData> getAllLocations() {
        return locationRepo.findAll();
    }
}
