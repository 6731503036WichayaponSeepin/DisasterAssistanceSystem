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

    // เอาไว้เทสต์ว่า controller โดนโหลดจริงมั้ย
    @GetMapping("/ping")
    public String ping() {
        return "cases-ok";
    }

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
        // คำนวณความหนาแน่นเคส
        int nearbyCount = countNearbyCases(req.getLatitude(), req.getLongitude(), 1.0);
        CaseSeverity severity = calculateSeverity(nearbyCount);
        ac.setSeverity(severity);
        ac.setStatus(CaseStatus.NEW);

        AssistanceCase saved = caseRepo.save(ac);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<?> getAllCases() {
        return ResponseEntity.ok(caseRepo.findAll());
    }

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
        if (nearbyCount >= 3) {
            return CaseSeverity.HIGH;
        } else if (nearbyCount >= 1) {
            return CaseSeverity.MEDIUM;
        } else {
            return CaseSeverity.LOW;
        }
    }
}
