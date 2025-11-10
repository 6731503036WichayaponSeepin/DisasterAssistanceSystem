package th.mfu.controller.casecontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import th.mfu.dto.CreateCaseRequest;
import th.mfu.model.caseentity.AssistanceCase;
import th.mfu.model.caseentity.CaseSeverity;
import th.mfu.model.caseentity.CaseStatus;
import th.mfu.model.user.User;
import th.mfu.repository.caserepo.AssistanceCaseRepository;
import th.mfu.repository.userrepository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin
public class AssistanceCaseController {

    @Autowired
    private AssistanceCaseRepository caseRepo;

    @Autowired
    private UserRepository userRepo;

    // ใช้เทสต์ว่า controller โดนโหลด
    @GetMapping("/ping")
    public String ping() {
        return "cases-ok";
    }

    // ผู้ใช้กดปุ่มแจ้งเคส (SOS / SUSTENANCE)
    @PostMapping("/report")
    public ResponseEntity<?> reportCase(@RequestBody CreateCaseRequest req) {

        // 1) เอา user จาก token (sub = เบอร์โทร)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String phone = auth.getName();

        User reporter = userRepo.findByPhoneNumber(phone);
        if (reporter == null) {
            return ResponseEntity.badRequest().body("User not found for phone: " + phone);
        }

        // 2) ต้องมีพิกัด
        if (req.getLatitude() == null || req.getLongitude() == null) {
            return ResponseEntity.badRequest().body("Latitude/Longitude is required");
        }

        // 3) สร้างเคสใหม่
        AssistanceCase ac = new AssistanceCase();
        ac.setReporterUserId(reporter.getId());
        ac.setReporterAddressId(req.getReporterAddressId());
        ac.setLatitude(req.getLatitude());
        ac.setLongitude(req.getLongitude());
        ac.setCaseType(req.getCaseType()); // SOS หรือ SUSTENANCE

        // 4) ประเมิน severity ของเคสนี้ก่อน
        int nearbyCount = countNearbyCases(req.getLatitude(), req.getLongitude(), 0.5);
        CaseSeverity severity = calculateSeverity(nearbyCount);
        ac.setSeverity(severity);

        // 5) สถานะเริ่มต้น
        ac.setStatus(CaseStatus.NEW);

        AssistanceCase saved = caseRepo.save(ac);

        // 6) อัปเดตเคสเก่าในรัศมีเดียวกันให้เป็นระดับล่าสุดด้วย (อัตโนมัติ)
        updateNearbyCasesSeverity(req.getLatitude(), req.getLongitude(), 0.5);

        return ResponseEntity.ok(saved);
    }

    // ดึงเคสทั้งหมด (เอาไปโชว์แผนที่ / list)
    @GetMapping
    public ResponseEntity<?> getAllCases() {
        return ResponseEntity.ok(caseRepo.findAll());
    }

    // ดึงเคสตามสถานะ (NEW / ASSIGNED / DONE)
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable("status") CaseStatus status) {
        return ResponseEntity.ok(caseRepo.findByStatus(status));
    }

    // ========== helper methods ==========

    // นับว่ามีเคสกี่อันอยู่ในรัศมีที่กำหนด
    private int countNearbyCases(Double lat, Double lon, double radiusKm) {
        List<AssistanceCase> all = caseRepo.findAll();
        int count = 0;
        for (AssistanceCase c : all) {
            if (c.getLatitude() != null && c.getLongitude() != null) {
                double dist = distanceKm(lat, lon, c.getLatitude(), c.getLongitude());
                if (dist <= radiusKm) {
                    count++;
                }
            }
        }
        return count;
    }

    // คำนวณระยะทางระหว่าง 2 จุดบนโลก (km)
    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // เกณฑ์ severity ปัจจุบัน
    private CaseSeverity calculateSeverity(int nearbyCount) {
        if (nearbyCount > 5) {
            return CaseSeverity.HIGH;
        } else if (nearbyCount >=3) {
            return CaseSeverity.MEDIUM;
        } else {
            return CaseSeverity.LOW;
        }
    }

    /**
     * อัปเดตทุกเคสที่อยู่ในรัศมีเดียวกันให้มี severity ตามจำนวนเคสล่าสุด
     * ใช้ตอนมีเคสใหม่เข้า เพื่อให้เคสเก่าในจุดเดียวกันเปลี่ยนสีตามไปด้วย
     */
    private void updateNearbyCasesSeverity(Double lat, Double lon, double radiusKm) {
        List<AssistanceCase> all = caseRepo.findAll();
        boolean changed = false;
        for (AssistanceCase c : all) {
            if (c.getLatitude() != null && c.getLongitude() != null) {
                double dist = distanceKm(lat, lon, c.getLatitude(), c.getLongitude());
                if (dist <= radiusKm) {
                    int nearby = countNearbyCases(c.getLatitude(), c.getLongitude(), radiusKm);
                    CaseSeverity newSev = calculateSeverity(nearby);
                    if (c.getSeverity() != newSev) {   // เซฟเฉพาะอันที่เปลี่ยนจริง
                        c.setSeverity(newSev);
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            caseRepo.saveAll(all);
        }
    }
}
