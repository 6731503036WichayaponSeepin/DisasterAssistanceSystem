package th.mfu.controller.rescuecontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import th.mfu.model.Detail;
import th.mfu.model.rescue.AffiliatedUnit;
import th.mfu.model.rescue.Rescue;
import th.mfu.repository.DetailRepository;
import th.mfu.repository.rescuerepository.AffiliatedUnitRepository;
import th.mfu.repository.rescuerepository.RescueRepository;

@RestController
@RequestMapping("/api/rescues")
@CrossOrigin
public class RescueController {

    @Autowired private RescueRepository rescueRepo;
    @Autowired private DetailRepository detailRepo;
    @Autowired private AffiliatedUnitRepository unitRepo;

    // ✅ สมัครกู้ภัยใหม่ + สร้าง Detail อัตโนมัติ
    @PostMapping("/register")
    public Rescue registerRescue(@RequestBody Rescue rescue) {

        // 1️⃣ บันทึก Detail ใหม่
        Detail newDetail = rescue.getDetail();
        if (newDetail != null) {
            newDetail = detailRepo.save(newDetail);
            rescue.setDetail(newDetail);
        } else {
            throw new RuntimeException("Detail is required");
        }

        // 2️⃣ ตรวจสอบหน่วยสังกัด
        if (rescue.getAffiliatedUnit() != null) {
            Long unitId = rescue.getAffiliatedUnit().getId();
            AffiliatedUnit unit = unitRepo.findById(unitId)
                    .orElseThrow(() -> new RuntimeException("Affiliated Unit not found with ID: " + unitId));
            rescue.setAffiliatedUnit(unit);
        }

        // 3️⃣ บันทึกข้อมูล Rescue
        return rescueRepo.save(rescue);
    }

    // ✅ ดึงรายชื่อกู้ภัยทั้งหมด
    @GetMapping
    public List<Rescue> getAllRescues() {
        return rescueRepo.findAll();
    }

    // ✅ ดึงรายชื่อกู้ภัยตามหน่วย
    @GetMapping("/byUnit/{unitId}")
    public List<Rescue> getRescuesByUnit(@PathVariable Long unitId) {
        return rescueRepo.findByAffiliatedUnit_Id(unitId);
    }

    // ✅ 🔐 Login ด้วย name + rescueId
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginRescue(@RequestBody Map<String, String> loginData) {
        String name = loginData.get("name");
        String rescueId = loginData.get("rescueId");

        Map<String, Object> response = new HashMap<>();

        if (name == null || rescueId == null) {
            response.put("status", "error");
            response.put("message", "Missing name or rescueId");
            return ResponseEntity.badRequest().body(response);
        }

        // ค้นหากู้ภัยจาก rescueId
        Optional<Rescue> optRescue = rescueRepo.findByRescueId(rescueId);
        if (optRescue.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Rescue ID not found");
            return ResponseEntity.status(404).body(response);
        }

        Rescue rescue = optRescue.get();

        // ตรวจสอบชื่อจาก Detail
        if (rescue.getDetail() != null && name.equals(rescue.getDetail().getName())) {
            response.put("status", "success");
            response.put("message", "Login successful");
            response.put("rescue", rescue);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Invalid name or rescueId");
            return ResponseEntity.status(401).body(response);
        }
    }
}