package th.mfu.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import th.mfu.model.locationdata.LocationData;
import th.mfu.model.user.Address;
import th.mfu.model.user.PostalCode;
import th.mfu.repository.locationdatarepository.LocationRepository;
import th.mfu.repository.userrepository.AddressRepository;
import th.mfu.repository.userrepository.PostalCodeRepository;

@Service
public class LocationService {

    @Autowired
    private AddressRepository addressRepo;

    @Autowired
    private LocationRepository locationRepo;

    @Autowired
    private PostalCodeRepository postalCodeRepo;

    // ✅ สร้างข้อมูลพิกัด (latitude, longitude) จาก Address ที่มีในฐานข้อมูล
    public LocationData createLocationFromAddress(Long addressId) throws IOException {
        // ดึงข้อมูล Address จากฐานข้อมูล
        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));

        // รวมข้อความที่อยู่ทั้งหมด
        String house = address.getMoreDetails();
        String subdistrict = address.getSubdistrict().getName();
        String district = address.getSubdistrict().getDistrict().getName();
        String province = address.getSubdistrict().getDistrict().getProvince().getName();

        // ✅ ดึงรหัสไปรษณีย์จาก Repository
        Optional<PostalCode> codeOpt = postalCodeRepo.findFirstBySubdistrict(address.getSubdistrict());
        String postalCode = codeOpt.map(PostalCode::getCode).orElse("");

        // รวมข้อความที่อยู่แบบสมบูรณ์
        String fullAddress = String.join(", ",
                house, subdistrict, district, province, postalCode
        );

        // เรียก API เพื่อแปลงเป็นพิกัด
        double[] coords = getLatLngFromAddress(fullAddress);

        // บันทึกลงฐานข้อมูล
        LocationData loc = new LocationData();
        loc.setAddress(address);
        loc.setLatitude(coords[0]);
        loc.setLongitude(coords[1]);

        return locationRepo.save(loc);
    }

    // ✅ ดึงข้อมูลพิกัดทั้งหมดจากฐานข้อมูล
    public List<LocationData> getAllLocations() {
        return locationRepo.findAll();
    }

    // ✅ ใช้ OpenStreetMap (Nominatim API) ฟรี เพื่อค้นหาพิกัดจากข้อความที่อยู่
    private double[] getLatLngFromAddress(String addressText) throws IOException {
        String encoded = URLEncoder.encode(addressText, StandardCharsets.UTF_8);
        String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encoded;

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", "mfu-project-disaster-assistance/1.0 (contact: example@mfu.ac.th)");
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        String json = response.toString();

        // ตรวจสอบว่ามีข้อมูลหรือไม่
        if (json == null || json.trim().equals("[]")) {
            throw new IOException("Address not found or invalid: " + addressText);
        }

        // ✅ ใช้ regex ดึง lat/lon จาก JSON
        Pattern latPattern = Pattern.compile("\"lat\":\"(.*?)\"");
        Pattern lonPattern = Pattern.compile("\"lon\":\"(.*?)\"");

        Matcher latMatcher = latPattern.matcher(json);
        Matcher lonMatcher = lonPattern.matcher(json);

        if (latMatcher.find() && lonMatcher.find()) {
            double lat = Double.parseDouble(latMatcher.group(1));
            double lon = Double.parseDouble(lonMatcher.group(1));
            return new double[]{lat, lon};
        } else {
            throw new IOException("Unable to parse coordinates for address: " + addressText);
        }
    }
}
