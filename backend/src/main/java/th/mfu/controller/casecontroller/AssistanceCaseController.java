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

        AssistanceCase ac = new AssistanceCase();
        ac.setReporterUserId(reporter.getId());
        ac.setLocationId(req.getLocationId());
        ac.setCaseType(req.getCaseType());
        ac.setStatus(CaseStatus.NEW);
        ac.setSeverity(CaseSeverity.LOW); // เริ่มต้นเป็น LOW

        AssistanceCase saved = caseRepo.save(ac);

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
    public ResponseEntity<?> comingToHelp(@PathVariable Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rescueCode = auth.getName();

        Rescue rescue = rescueRepo.findByRescueId(rescueCode).orElse(null);
        if (rescue == null) {
            return ResponseEntity.badRequest().body("Rescue not found: " + rescueCode);
        }

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

    /** ============================
     *  ทีมกู้ภัยกด "ช่วยสำเร็จ" (DONE)
     * ============================ */
    @Transactional
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmCase(@PathVariable Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rescueCode = auth.getName();

        Rescue rescue = rescueRepo.findByRescueId(rescueCode).orElse(null);
        if (rescue == null) {
            return ResponseEntity.badRequest().body("Rescue not found: " + rescueCode);
        }

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
public ResponseEntity<?> getCaseById(@PathVariable Long id) {
    return caseRepo.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}

}
