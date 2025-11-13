package th.mfu.controller.rescuecontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import th.mfu.model.Detail;
import th.mfu.model.rescue.*;
import th.mfu.repository.DetailRepository;
import th.mfu.repository.rescuerepository.*;
import th.mfu.security.JwtUtil;

import java.util.*;

@RestController
@RequestMapping("/api/rescues")
@CrossOrigin
public class RescueController {

    @Autowired private RescueRepository rescueRepo;
    @Autowired private DetailRepository detailRepo;
    @Autowired private AffiliatedUnitRepository unitRepo;
    @Autowired private RescueTeamRepository teamRepo;
    @Autowired private JwtUtil jwtUtil;

    // ⭐ Helper: สร้างรหัสกู้ภัยใหม่อัตโนมัติ เช่น RS-001, RS-002
    private String generateNewRescueId() {
        List<Rescue> rescues = rescueRepo.findAll();
        int max = 0;

        for (Rescue r : rescues) {
            String rid = r.getRescueId();
            if (rid != null && rid.startsWith("RS-")) {
                try {
                    int num = Integer.parseInt(rid.substring(3)); // ตัด "RS-"
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException ignored) {
                    // ถ้ารูปแบบไม่ตรง เช่น RS-ABC ก็ข้ามไป
                }
            }
        }

        int next = max + 1;
        return String.format("RS-%03d", next); // RS-001, RS-002, ...
    }

    // ✅ 1️⃣ สมัครเป็นกู้ภัย (ชื่อ, เบอร์, หน่วย) — rescueId ระบบออกเอง
    @PostMapping("/register")
    public ResponseEntity<?> registerRescue(@RequestBody Rescue rescue) {
        // ✅ ตรวจสอบข้อมูลส่วนตัว (ชื่อ / เบอร์ อยู่ใน Detail)
        if (rescue.getDetail() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Detail is required");
        }

        // 👉 ตรงนี้สมมติว่า Detail มี name และ (อาจจะ) phoneNumber
        // ถ้ายังไม่มี phoneNumber ใน entity/detail ให้ไปเพิ่มใน model + ตาราง DB ด้วยนะ
        Detail detail = rescue.getDetail();
        Detail savedDetail = detailRepo.save(detail);
        rescue.setDetail(savedDetail);

        // ✅ ตรวจสอบหน่วยสังกัด
        if (rescue.getAffiliatedUnit() == null || rescue.getAffiliatedUnit().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Affiliated Unit is required");
        }
        AffiliatedUnit unit = unitRepo.findById(rescue.getAffiliatedUnit().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Affiliated Unit not found"));
        rescue.setAffiliatedUnit(unit);

        // ✅ ตรวจสอบทีม (ถ้ามีการแนบมา)
        if (rescue.getRescueTeam() != null && rescue.getRescueTeam().getId() != null) {
            RescueTeam team = teamRepo.findById(rescue.getRescueTeam().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rescue team not found"));
            rescue.setRescueTeam(team);
        }

        // ⭐ ไม่ใช้ rescueId จาก client แล้ว — ให้ระบบออกเอง
        String newRescueId = generateNewRescueId();
        rescue.setRescueId(newRescueId);

        // (จะเช็คซ้ำอีกรอบก็ได้ เผื่อวันหน้ามีการแก้ logic generate)
        if (rescueRepo.findByRescueId(newRescueId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Generated Rescue ID already exists: " + newRescueId);
        }

        // ✅ ตั้งค่า role เป็น RESCUE
        rescue.setRole("RESCUE");

        Rescue saved = rescueRepo.save(rescue);

        // ✅ สร้าง response
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Rescue registered successfully");
        response.put("rescueId", saved.getRescueId()); // 👉 ส่งรหัสที่ระบบออกให้กลับไป
        response.put("name", saved.getName());
        response.put("role", saved.getRole());
        response.put("navigateTo", "/loginRescue");

        return ResponseEntity.ok(response);
    }

    // ✅ 2️⃣ ดึงรายชื่อกู้ภัยทั้งหมด
    @GetMapping
    public List<Rescue> getAllRescues() {
        return rescueRepo.findAll();
    }

    // ✅ 3️⃣ ดึงรายชื่อกู้ภัยตามหน่วย
    @GetMapping("/byUnit/{unitId}")
    public List<Rescue> getRescuesByUnit(@PathVariable Long unitId) {
        return rescueRepo.findByAffiliatedUnit_Id(unitId);
    }

    // ✅ 4️⃣ ดึงรายชื่อกู้ภัยที่ยังไม่มีทีม
    @GetMapping("/available")
    public List<Map<String, Object>> getAvailableRescues() {
        List<Rescue> rescues = rescueRepo.findByRescueTeamIsNull();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Rescue r : rescues) {
            Map<String, Object> item = new HashMap<>();
            item.put("rescueId", r.getRescueId());
            item.put("name", r.getName());
            item.put("unit", r.getAffiliatedUnit().getUnitName());
            response.add(item);
        }
        return response;
    }

   // ต้องมี @Autowired DetailRepository detailRepo; อยู่บนคลาสนี้ด้วยนะ

@PostMapping("/login")
public ResponseEntity<Map<String, Object>> loginRescue(@RequestBody Map<String, String> loginData) {
    String name        = loginData.get("name");
    String phoneNumber = loginData.get("phone_number"); // ให้ frontend ส่ง key ชื่อนี้มา

    Map<String, Object> response = new HashMap<>();

    if (name == null || phoneNumber == null) {
        response.put("status", "error");
        response.put("message", "Missing name or phone number");
        return ResponseEntity.badRequest().body(response);
    }

    // 1) หา Detail จาก name + phoneNumber
    Optional<Detail> optDetail = detailRepo.findByNameAndPhoneNumber(name, phoneNumber);
    if (optDetail.isEmpty()) {
        response.put("status", "error");
        response.put("message", "Invalid name or phone number");
        return ResponseEntity.status(401).body(response);
    }
    Detail detail = optDetail.get();

    // 2) หา Rescue ที่ใช้ Detail นี้
    Optional<Rescue> optRescue = rescueRepo.findByDetail(detail);
    if (optRescue.isEmpty()) {
        response.put("status", "error");
        response.put("message", "Rescue account not found for this detail");
        return ResponseEntity.status(404).body(response);
    }

    Rescue rescue = optRescue.get();

    // 3) สร้าง JWT Token
    //    👉 ยังใช้ rescueId เป็น subject ได้เหมือนเดิม
    String token = jwtUtil.generateToken(rescue.getRescueId(), "RESCUE");

    response.put("status", "success");
    response.put("message", "Login successful");
    response.put("token", token);
    response.put("role", "RESCUE");
    response.put("rescueDbId", rescue.getId());
    response.put("rescueId", rescue.getRescueId());
    response.put("name", detail.getName());
    response.put("unit", rescue.getAffiliatedUnit() != null
            ? rescue.getAffiliatedUnit().getUnitName()
            : "-");
    response.put("navigateTo", "/mainPageRescue");

    return ResponseEntity.ok(response);
}


    // ✅ 6️⃣ หน้า Main ของ Rescue (แสดงข้อมูลตนเอง)
    @GetMapping("/main/{id}")
    public ResponseEntity<?> getRescueMain(@PathVariable Long id) {
        Rescue rescue = rescueRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rescue not found"));

        // ✅ ตรวจสอบสิทธิ์ role
        if (!"RESCUE".equalsIgnoreCase(rescue.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only rescue role can access this page");
        }

        Map<String, Object> response = new HashMap<>();

        // 🔹 ข้อมูลส่วนตัว
        Map<String, Object> personalInfo = new HashMap<>();
        if (rescue.getDetail() != null) {
            personalInfo.put("detailId", rescue.getDetail().getId());
            personalInfo.put("name", rescue.getDetail().getName());
        }

        // 🔹 หน่วยสังกัด
        Map<String, Object> unitInfo = new HashMap<>();
        if (rescue.getAffiliatedUnit() != null) {
            unitInfo.put("unitId", rescue.getAffiliatedUnit().getId());
            unitInfo.put("unitName", rescue.getAffiliatedUnit().getUnitName());
        }

        // 🔹 ทีมกู้ภัย (ถ้ามี)
        Map<String, Object> teamInfo = new HashMap<>();
        if (rescue.getRescueTeam() != null) {
            RescueTeam team = rescue.getRescueTeam();
            teamInfo.put("teamId", team.getTeamId());
            teamInfo.put("teamName", team.getName());
            teamInfo.put("memberCount", team.getMembers() != null ? team.getMembers().size() : 0);
            teamInfo.put("leader", team.getLeader().getName());
        }

        response.put("status", "success");
        response.put("role", rescue.getRole());
        response.put("rescueDbId", rescue.getId());
        response.put("rescueId", rescue.getRescueId());
        response.put("personalInfo", personalInfo);
        response.put("unitInfo", unitInfo);
        response.put("teamInfo", teamInfo);

        return ResponseEntity.ok(response);
    }
}