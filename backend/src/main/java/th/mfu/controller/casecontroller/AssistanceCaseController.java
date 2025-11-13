package th.mfu.controller.casecontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import th.mfu.dto.CreateCaseRequest;
import th.mfu.model.caseentity.AssistanceCase;
import th.mfu.model.caseentity.CaseSeverity;
import th.mfu.model.caseentity.CaseStatus;
import th.mfu.model.user.User;
import th.mfu.model.rescue.Rescue;
import th.mfu.model.rescue.RescueTeam;
import th.mfu.repository.caserepo.AssistanceCaseRepository;
import th.mfu.repository.userrepository.UserRepository;
import th.mfu.repository.rescuerepository.RescueRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin
public class AssistanceCaseController {

    @Autowired
    private AssistanceCaseRepository caseRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RescueRepository rescueRepo;

    // ใช้เทสต์ว่า controller โดนโหลด
    @GetMapping("/ping")
    public String ping() {
        return "cases-ok";
    }

    // ผู้ใช้กดปุ่มแจ้งเคส (SOS / SUSTENANCE)
    @PostMapping("/report")
    public ResponseEntity<?> reportCase(@RequestBody CreateCaseRequest req) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String phone = auth.getName(); // subject = เบอร์โทรจาก Detail

        // 🔹 หา user จาก detail.phoneNumber
        Optional<User> optReporter = userRepo.findByDetail_PhoneNumber(phone);
        if (optReporter.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("User not found for phone: " + phone);
        }
        User reporter = optReporter.get();

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

    private boolean isTeamLeader(Rescue rescue) {
        return rescue.getRescueTeam() != null
            && rescue.getRescueTeam().getLeader() != null
            && rescue.getId().equals(rescue.getRescueTeam().getLeader().getId());
    }

    @Transactional
    @PostMapping("/{id}/follow")
    public ResponseEntity<?> followCase(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rescueCode = auth.getName();

        Rescue rescue = rescueRepo.findByRescueId(rescueCode).orElse(null);
        if (rescue == null) {
            return ResponseEntity.badRequest().body("Rescue not found");
        }

        if (rescue.getRescueTeam() == null) {
            return ResponseEntity.badRequest().body("Please join a team before accepting a case.");
        }

        RescueTeam team = rescue.getRescueTeam();
        if (!team.getLeader().getId().equals(rescue.getId())) {
            return ResponseEntity.status(403).body("Only the team leader can accept a case.");
        }

        AssistanceCase c = caseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found: " + id));

        if (c.getAssignedRescueTeamId() != null) {
            return ResponseEntity.badRequest().body("This case is already assigned to a team.");
        }

        c.setAssignedRescueTeamId(team.getId());
        c.setStatus(CaseStatus.ASSIGNED);

        caseRepo.save(c);
        return ResponseEntity.ok(c);
    }

    @Transactional
    @PostMapping("/{id}/coming")
    public ResponseEntity<?> comingToHelp(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rescueCode = auth.getName();

        Rescue rescue = rescueRepo.findByRescueId(rescueCode).orElse(null);
        if (rescue == null) return ResponseEntity.badRequest().body("Rescue not found: " + rescueCode);
        if (rescue.getRescueTeam() == null)
            return ResponseEntity.badRequest().body("Please join a team first.");
        if (!isTeamLeader(rescue))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only team leader can set COMING.");

        AssistanceCase c = caseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found: " + id));

        Long teamId = rescue.getRescueTeam().getId();
        if (c.getAssignedRescueTeamId() == null)
            return ResponseEntity.badRequest().body("This case is not assigned to any team.");
        if (!teamId.equals(c.getAssignedRescueTeamId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the assigned team can set COMING.");
        if (c.getStatus() != CaseStatus.ASSIGNED)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Invalid state transition to COMING.");

        c.setStatus(CaseStatus.COMING);
        return ResponseEntity.ok(caseRepo.save(c));
    }

    @Transactional
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmCase(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rescueCode = auth.getName();

        Rescue rescue = rescueRepo.findByRescueId(rescueCode).orElse(null);
        if (rescue == null) return ResponseEntity.badRequest().body("Rescue not found: " + rescueCode);
        if (rescue.getRescueTeam() == null)
            return ResponseEntity.badRequest().body("Please join a team first.");
        if (!isTeamLeader(rescue))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only team leader can confirm DONE.");

        AssistanceCase c = caseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found: " + id));

        Long teamId = rescue.getRescueTeam().getId();
        if (c.getAssignedRescueTeamId() == null)
            return ResponseEntity.badRequest().body("This case is not assigned to any team.");
        if (!teamId.equals(c.getAssignedRescueTeamId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the assigned team can confirm DONE.");
        if (c.getStatus() != CaseStatus.COMING)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Invalid state transition to DONE.");

        c.setStatus(CaseStatus.DONE);
        return ResponseEntity.ok(caseRepo.save(c));
    }

    /** ดึงเคสของ “ทีมฉัน” (ใช้กับ Selected/Coming/Completed) */
    @GetMapping("/my")
    public ResponseEntity<?> getMyTeamCases(@RequestParam(required = false) CaseStatus status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rescueCode = auth.getName();

        Rescue rescue = rescueRepo.findByRescueId(rescueCode).orElse(null);
        if (rescue == null) return ResponseEntity.badRequest().body("Rescue not found: " + rescueCode);
        if (rescue.getRescueTeam() == null)
            return ResponseEntity.badRequest().body("Please join a team first.");

        Long myTeamId = rescue.getRescueTeam().getId();

        if (status != null) {
            return ResponseEntity.ok(caseRepo.findByAssignedRescueTeamIdAndStatus(myTeamId, status));
        } else {
            return ResponseEntity.ok(caseRepo.findByAssignedRescueTeamId(myTeamId));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableCases(@RequestParam(required = false) CaseStatus status) {
        if (status != null) {
            return ResponseEntity.ok(caseRepo.findByAssignedRescueTeamIdIsNullAndStatus(status));
        }
        return ResponseEntity.ok(caseRepo.findByAssignedRescueTeamIdIsNull());
    }
}
