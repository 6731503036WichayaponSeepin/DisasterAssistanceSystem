package th.mfu.controller.usercontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import th.mfu.model.user.District;
import th.mfu.model.user.PostalCode;
import th.mfu.model.user.Province;
import th.mfu.model.user.Subdistrict;
import th.mfu.repository.userrepository.DistrictRepository;
import th.mfu.repository.userrepository.PostalCodeRepository;
import th.mfu.repository.userrepository.ProvinceRepository;
import th.mfu.repository.userrepository.SubdistrictRepository;

@RestController
@RequestMapping("/api/location")
@CrossOrigin
public class LocationController {

    @Autowired private ProvinceRepository provinceRepo;
    @Autowired private DistrictRepository districtRepo;
    @Autowired private SubdistrictRepository subdistrictRepo;
    @Autowired private PostalCodeRepository postalRepo;

    // -------------------- 📍 PROVINCE --------------------

    // ✅ ดึงจังหวัดทั้งหมด
    @GetMapping("/provinces")
    public List<Province> getAllProvinces() {
        return provinceRepo.findAll();
    }

    // ✅ เพิ่มจังหวัดใหม่
    @PostMapping("/provinces")
    public Province createProvince(@RequestBody Province province) {
        return provinceRepo.save(province);
    }

    // -------------------- 🏙️ DISTRICT --------------------

    // ✅ ดึงอำเภอตามจังหวัด
    @GetMapping("/districts/{provinceId}")
    public List<District> getDistrictsByProvince(@PathVariable Long provinceId) {
        return districtRepo.findByProvinceId(provinceId);
    }

    // ✅ เพิ่มอำเภอใหม่ (ต้องมี province id)
    @PostMapping("/districts")
    public District createDistrict(@RequestBody District district) {
        return districtRepo.save(district);
    }

    // -------------------- 🏘️ SUBDISTRICT --------------------

    // ✅ ดึงตำบลตามอำเภอ
    @GetMapping("/subdistricts/{districtId}")
    public List<Subdistrict> getSubdistrictsByDistrict(@PathVariable Long districtId) {
        return subdistrictRepo.findByDistrictId(districtId);
    }

    // ✅ เพิ่มตำบลใหม่
    @PostMapping("/subdistricts")
    public Subdistrict createSubdistrict(@RequestBody Subdistrict subdistrict) {
        return subdistrictRepo.save(subdistrict);
    }

    // -------------------- 🏤 POSTAL CODE --------------------

    // ✅ ดึงรหัสไปรษณีย์ของตำบล
    @GetMapping("/postal/{subdistrictId}")
    public List<PostalCode> getPostalBySubdistrict(@PathVariable Long subdistrictId) {
        return postalRepo.findBySubdistrictId(subdistrictId);
    }

    // ✅ เพิ่มรหัสไปรษณีย์ใหม่
    @PostMapping("/postal")
    public PostalCode createPostalCode(@RequestBody PostalCode postalCode) {
        return postalRepo.save(postalCode);
    }
}