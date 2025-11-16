package th.mfu.controller.casecontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import th.mfu.dto.CreateCaseRequest;
import th.mfu.model.caseentity.AssistanceCase;
import th.mfu.model.caseentity.CaseSeverity;
import th.mfu.model.caseentity.CaseStatus;
import th.mfu.model.locationdata.LocationData;
import th.mfu.model.rescue.Rescue;
import th.mfu.model.rescue.RescueTeam;
import th.mfu.model.user.User;
import th.mfu.repository.caserepo.AssistanceCaseRepository;
import th.mfu.repository.locationdatarepository.LocationRepository;
import th.mfu.repository.rescuerepository.RescueRepository;
import th.mfu.repository.userrepository.UserRepository;

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

    @Autowired
    private LocationRepository locationRepo;


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
private void updateSeverityWithin1Km(AssistanceCase newCase) {

    LocationData newLoc = newCase.getLocationId();
    double lat1 = newLoc.getLatitude();
    double lon1 = newLoc.getLongitude();

    List<AssistanceCase> allCases = caseRepo.findAll();
    List<AssistanceCase> nearby = new ArrayList<>();

    for (AssistanceCase c : allCases) {
        LocationData loc = c.getLocationId();
        if (loc == null) continue;

        double dist = distanceKm(lat1, lon1, loc.getLatitude(), loc.getLongitude());
        if (dist <= 1.0) {
            nearby.add(c);
        }
    }

    int count = nearby.size();

    CaseSeverity severity;
    if (count >= 5) severity = CaseSeverity.HIGH;
    else if (count >= 3) severity = CaseSeverity.MEDIUM;
    else severity = CaseSeverity.LOW;

    for (AssistanceCase c : nearby) {
        c.setSeverity(severity);
        caseRepo.save(c);
    }
}

    // ใช้เทสต์ว่า controller โดนโหลด
    @GetMapping("/ping")
    public String ping() {
        return "cases-ok";
    }

    /**
     * ============================
     *  ผู้ใช้กดปุ่ม "แจ้งเคส"
     *  ใช้ locationId จาก location_data
     * ============================
     */
    @PostMapping("/report")
public ResponseEntity<?> reportCase(@RequestBody CreateCaseRequest req) {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String phone = auth.getName(); // เบอร์โทรจาก Token

    // หา user จากเบอร์โทร
    Optional<User> optReporter = userRepo.findByDetail_PhoneNumber(phone);
    if (optReporter.isEmpty()) {
        return ResponseEntity.badRequest().body("User not found for phone: " + phone);
    }
    User reporter = optReporter.get();

    // ต้องมี locationId เสมอ
    if (req.getLocationId() == null) {
        return ResponseEntity.badRequest()
                .body("locationId is required. Please pin location first.");
    }
// ตรวจสอบว่าผู้ใช้มีเคสที่ยัง active อยู่หรือไม่
List<CaseStatus> active = List.of(
        CaseStatus.NEW,
        CaseStatus.ASSIGNED,
        CaseStatus.COMING
);

boolean hasActiveCase = caseRepo
        .existsByReporterUserIdAndStatusIn(reporter.getId(), active);

if (hasActiveCase) {
    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body("You already have an active request.");
}

    AssistanceCase ac = new AssistanceCase();
    ac.setReporterUserId(reporter.getId());
LocationData loc = locationRepo.findById(req.getLocationId())
        .orElseThrow(() -> new RuntimeException("Location not found: " + req.getLocationId()));

ac.setLocationId(loc);

    ac.setCaseType(req.getCaseType());
    ac.setStatus(CaseStatus.NEW);
    ac.setSeverity(CaseSeverity.LOW);

    AssistanceCase saved = caseRepo.save(ac);
    updateSeverityWithin1Km(saved);
    return ResponseEntity.ok(saved);
}


    /** ดึงเคสทั้งหมด */
    @GetMapping
    public ResponseEntity<?> getAllCases() {
        return ResponseEntity.ok(caseRepo.findAll());
    }

    /** ดึงเคสตามสถานะ */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable("status") CaseStatus status) {
        return ResponseEntity.ok(caseRepo.findByStatus(status));
    }

    /** ============================
     *  กด "รับเคส" (ASSIGNED)
     * ============================ */
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

        // ต้องเป็นหัวหน้าทีมเท่านั้น
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

        return ResponseEntity.ok(caseRepo.save(c));
    }

    /** ============================
     *  ทีมกู้ภัยกด "กำลังไปช่วย" (COMING)
     * ============================ */
    @Transactional
    @PostMapping("/{id}/coming")
    public ResponseEntity<?> ComingCase(@PathVariable Long id) {

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

        // ต้องเป็นหัวหน้าทีมเท่านั้น
        if (!team.getLeader().getId().equals(rescue.getId())) {
            return ResponseEntity.status(403).body("Only the team leader can set COMING.");
        }

        AssistanceCase c = caseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found: " + id));

        // ❗ ต้อง assigned แล้ว
        if (c.getAssignedRescueTeamId() == null) {
            return ResponseEntity.badRequest().body("This case is not assigned to any team.");
        }

        // ❗ Assigned team ต้องเป็นทีมของ rescue นี้
        if (!c.getAssignedRescueTeamId().equals(team.getId())) {
            return ResponseEntity.status(403).body("Only the assigned team can set COMING.");
        }

        // ❗ ต้องอยู่สถานะ ASSIGNED เท่านั้น
        if (c.getStatus() != CaseStatus.ASSIGNED) {
            return ResponseEntity.status(409).body("Invalid status. Must be ASSIGNED first.");
        }

        // เปลี่ยนเป็น COMING
        c.setStatus(CaseStatus.COMING);

        return ResponseEntity.ok(caseRepo.save(c));
    }


    /** ============================
     *  ดูเคสของทีมฉัน
     * ============================ */
    @GetMapping("/my")
    public ResponseEntity<?> getMyTeamCases(@RequestParam(required = false) CaseStatus status) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rescueCode = auth.getName();

        Rescue rescue = rescueRepo.findByRescueId(rescueCode).orElse(null);
        if (rescue == null)
            return ResponseEntity.badRequest().body("Rescue not found: " + rescueCode);

        if (rescue.getRescueTeam() == null)
            return ResponseEntity.badRequest().body("Please join a team first.");

        Long teamId = rescue.getRescueTeam().getId();

        if (status != null)
            return ResponseEntity.ok(caseRepo.findByAssignedRescueTeamIdAndStatus(teamId, status));

        return ResponseEntity.ok(caseRepo.findByAssignedRescueTeamId(teamId));
    }

    /** ============================
     *  ดึงเคสที่ยังไม่มีทีมรับ
     * ============================ */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableCases(@RequestParam(required = false) CaseStatus status) {

        if (status != null)
            return ResponseEntity.ok(caseRepo.findByAssignedRescueTeamIdIsNullAndStatus(status));

        return ResponseEntity.ok(caseRepo.findByAssignedRescueTeamIdIsNull());
    }

    // ===== helper =====
    private boolean isTeamLeader(Rescue rescue) {
        return rescue.getRescueTeam() != null
                && rescue.getRescueTeam().getLeader() != null
                && rescue.getId().equals(rescue.getRescueTeam().getLeader().getId());
    }
    @GetMapping("/{id}")
public ResponseEntity<?> getCaseDetails(@PathVariable Long id) {

    AssistanceCase c = caseRepo.findById(id)
            .orElse(null);

    if (c == null) {
        return ResponseEntity.notFound().build();
    }

    User reporter = userRepo.findById(c.getReporterUserId()).orElse(null);
    LocationData loc = c.getLocationId();

    String address = "";
    if (loc != null) {
        if (loc.getRoad() != null) address += loc.getRoad() + ", ";
        if (loc.getSubdistrict() != null) address += loc.getSubdistrict() + ", ";
        if (loc.getDistrict() != null) address += loc.getDistrict() + ", ";
        if (loc.getProvince() != null) address += loc.getProvince() + " ";
        if (loc.getPostcode() != null) address += loc.getPostcode();
    }

    Map<String, Object> res = new HashMap<>();
    res.put("id", c.getId());
    res.put("status", c.getStatus().name());   // ⭐⭐⭐ ต้องมีบรรทัดนี้ ⭐⭐⭐
    res.put("severity", c.getSeverity().name());
    res.put("name", reporter != null ? reporter.getDetail().getName() : "Unknown");
    res.put("phone", reporter != null ? reporter.getDetail().getPhoneNumber() : "-");
    res.put("address", address);

    return ResponseEntity.ok(res);
}




@GetMapping("/my-active")
public ResponseEntity<?> getMyActiveCase() {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String phone = auth.getName();

    User user = userRepo.findByDetail_PhoneNumber(phone)
            .orElse(null);

    if (user == null) {
        return ResponseEntity.status(404).body("User not found");
    }

    // สถานะที่ถือว่ายัง active อยู่
    List<CaseStatus> active = List.of(
            CaseStatus.NEW,
            CaseStatus.ASSIGNED,
            CaseStatus.COMING
    );

    Optional<AssistanceCase> opt = caseRepo
            .findFirstByReporterUserIdAndStatusInOrderByCreatedAtDesc(
                    user.getId(), active
            );

    if (opt.isEmpty()) {
        return ResponseEntity.noContent().build(); // ไม่มีเคส
    }

    return ResponseEntity.ok(opt.get()); // ส่งเคสกลับไป
}


}