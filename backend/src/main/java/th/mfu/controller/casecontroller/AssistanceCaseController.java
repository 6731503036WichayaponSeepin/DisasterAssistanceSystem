package th.mfu.controller.casecontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import th.mfu.dto.CreateCaseRequest;
import th.mfu.model.caseentity.AssistanceCase;
import th.mfu.model.caseentity.CaseSeverity;
import th.mfu.model.caseentity.CaseStatus;
import th.mfu.model.user.User;
import th.mfu.model.rescue.Rescue;
import th.mfu.repository.caserepo.AssistanceCaseRepository;
import th.mfu.repository.userrepository.UserRepository;
import th.mfu.repository.rescuerepository.RescueRepository;

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

    @Autowired
    private RescueRepository rescueRepo;   // ✅ เพิ่มอันนี้

    // ใช้เทสต์ว่า controller โดนโหลด
    @GetMapping("/ping")
    public String ping() {
        return "cases-ok";
    }

    // ผู้ใช้กดปุ่มแจ้งเคส (SOS / SUSTENANCE)
    @PostMapping("/report")
    public ResponseEntity<?> reportCase(@RequestBody CreateCaseRequest req) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String phone = auth.getName();

        User reporter = userRepo.findByPhoneNumber(phone);
        if (reporter == null) {
            return ResponseEntity.badRequest().body("User not found for phone: " + phone);
        }

        if (req.getLatitude() == null || req.getLongitude() == null) {
            return ResponseEntity.badRequest().body("Latitude/Longitude is required");
        }

        AssistanceCase ac = new AssistanceCase();
        ac.setReporterUserId(reporter.getId());
        ac.setReporterAddressId(req.getReporterAddressId());
        ac.setLatitude(req.getLatitude());
        ac.setLongitude(req.getLongitude());
        ac.setCaseType(req.getCaseType());

        int nearbyCount = countNearbyCases(req.getLatitude(), req.getLongitude(), 0.5);
        CaseSeverity severity = calculateSeverity(nearbyCount);
        ac.setSeverity(severity);

        ac.setStatus(CaseStatus.NEW);

        AssistanceCase saved = caseRepo.save(ac);

        // อัปเดตเคสเก่าในรัศมีเดียวกัน
        updateNearbyCasesSeverity(req.getLatitude(), req.getLongitude(), 0.5);

        return ResponseEntity.ok(saved);
    }

    // ดึงเคสทั้งหมด
    @GetMapping
    public ResponseEntity<?> getAllCases() {
        return ResponseEntity.ok(caseRepo.findAll());
    }

    // ดึงเคสตามสถานะ
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable("status") CaseStatus status) {
        return ResponseEntity.ok(caseRepo.findByStatus(status));
    }

    // ========== helper methods ==========

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

    private CaseSeverity calculateSeverity(int nearbyCount) {
        if (nearbyCount > 5) {
            return CaseSeverity.HIGH;
        } else if (nearbyCount >= 3) {
            return CaseSeverity.MEDIUM;
        } else {
            return CaseSeverity.LOW;
        }
    }

    private void updateNearbyCasesSeverity(Double lat, Double lon, double radiusKm) {
        List<AssistanceCase> all = caseRepo.findAll();
        boolean changed = false;
        for (AssistanceCase c : all) {
            if (c.getLatitude() != null && c.getLongitude() != null) {
                double dist = distanceKm(lat, lon, c.getLatitude(), c.getLongitude());
                if (dist <= radiusKm) {
                    int nearby = countNearbyCases(c.getLatitude(), c.getLongitude(), radiusKm);
                    CaseSeverity newSev = calculateSeverity(nearby);
                    if (c.getSeverity() != newSev) {
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

    // ================== กู้ภัยกด "Follow Case" ==================
    @PostMapping("/{id}/follow")
    public ResponseEntity<?> followCase(@PathVariable Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rescueCode = auth.getName();  // เช่น RS-001

        // หาจากตาราง rescue เท่านั้น
        Rescue rescue = rescueRepo.findByRescueId(rescueCode).orElse(null);
        if (rescue == null) {
            return ResponseEntity.badRequest().body("Rescue not found: " + rescueCode);
        }

        AssistanceCase c = caseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found: " + id));

        // ถ้ามีคนรับแล้ว ไม่ให้รับซ้ำ
        if (c.getAssignedRescueId() != null) {
            return ResponseEntity.badRequest().body("This case is already assigned.");
        }

        c.setAssignedRescueId(rescue.getId());
        c.setStatus(CaseStatus.ASSIGNED);

        caseRepo.save(c);
        return ResponseEntity.ok(c);
    }

    // ================== กู้ภัยกด "Confirm" ==================
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmCase(@PathVariable Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rescueCode = auth.getName();

        Rescue rescue = rescueRepo.findByRescueId(rescueCode).orElse(null);
        if (rescue == null) {
            return ResponseEntity.badRequest().body("Rescue not found: " + rescueCode);
        }

        AssistanceCase c = caseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found: " + id));

        // ถ้ายังไม่ได้ assign ใครเลย ให้คนที่กด confirm เป็นคนปิด
        if (c.getAssignedRescueId() == null) {
            c.setAssignedRescueId(rescue.getId());
        }

        c.setStatus(CaseStatus.DONE);

        caseRepo.save(c);
        return ResponseEntity.ok(c);
    }
}
