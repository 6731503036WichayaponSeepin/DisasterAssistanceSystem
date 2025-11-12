package th.mfu.controller.rescuecontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import th.mfu.model.rescue.Rescue;
import th.mfu.model.rescue.RescueTeam;
import th.mfu.model.user.District;

import th.mfu.repository.rescuerepository.RescueRepository;
import th.mfu.repository.rescuerepository.RescueTeamRepository;
import th.mfu.repository.userrepository.DistrictRepository;

import java.util.*;

@RestController
@RequestMapping("/api/rescue-teams")
@CrossOrigin
public class RescueTeamController {

    @Autowired private RescueTeamRepository teamRepo;
    @Autowired private RescueRepository rescueRepo;
    @Autowired private DistrictRepository districtRepo;

    // ✅ 1️⃣ สร้างทีมใหม่ (เฉพาะ Rescue ที่ยังไม่ได้เป็นหัวหน้าทีม)
    @PostMapping("/create/{leaderRescueId}")
    public RescueTeam createTeam(@PathVariable String leaderRescueId, @RequestBody Map<String, Object> payload) {

        Rescue leader = rescueRepo.findByRescueId(leaderRescueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leader not found: " + leaderRescueId));

        if (teamRepo.findByLeader(leader).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This rescue is already a team leader.");
        }

        if (leader.getRescueTeam() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This rescue is already in another team.");
        }

        String name = (String) payload.get("name");
        String teamId = (String) payload.get("teamId");
        Long districtId = Long.valueOf(payload.get("districtId").toString());

        if (teamRepo.existsByTeamId(teamId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Team ID already exists: " + teamId);
        }

        District district = districtRepo.findById(districtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "District not found: " + districtId));

        RescueTeam team = new RescueTeam();
        team.setName(name);
        team.setTeamId(teamId);
        team.setDistrict(district);
        team.setLeader(leader);

        RescueTeam savedTeam = teamRepo.save(team);

        leader.setRescueTeam(savedTeam);
        rescueRepo.save(leader);

        return savedTeam;
    }

    // ✅ 2️⃣ ดึงรายชื่อทีมทั้งหมด
    @GetMapping
    public List<RescueTeam> getAllTeams() {
        return teamRepo.findAll();
    }

    // ✅ 3️⃣ ดูรายชื่อสมาชิกในทีม (เฉพาะคนในทีมเท่านั้น)
    @GetMapping("/{teamId}/members/{viewerRescueId}")
    public List<Map<String, Object>> getTeamMembers(@PathVariable String teamId,
                                                    @PathVariable String viewerRescueId) {

        RescueTeam team = teamRepo.findByTeamId(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found: " + teamId));

        Rescue viewer = rescueRepo.findByRescueId(viewerRescueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rescue not found: " + viewerRescueId));

        // ✅ ตรวจว่า viewer อยู่ในทีมนี้ไหม
        if (viewer.getRescueTeam() == null || !viewer.getRescueTeam().getId().equals(team.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: not a member of this team.");
        }

        // ✅ ถ้าเป็นคนในทีมจริง ให้แสดงรายชื่อสมาชิกได้
        List<Rescue> members = rescueRepo.findAllByRescueTeam(team);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Rescue r : members) {
            Map<String, Object> row = new HashMap<>();
            row.put("rescueId", r.getRescueId());
            row.put("name", r.getName());
            result.add(row);
        }

        return result;
    }

    // ✅ 4️⃣ เพิ่มสมาชิกเข้าทีม (เฉพาะหัวหน้าทีมเท่านั้น)
    @PutMapping("/{teamId}/members/{rescueId}/add/{leaderRescueId}")
    public String addMember(@PathVariable String teamId,
                            @PathVariable String rescueId,
                            @PathVariable String leaderRescueId) {

        Rescue leader = rescueRepo.findByRescueId(leaderRescueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leader not found"));

        RescueTeam team = teamRepo.findByTeamId(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

        if (!team.getLeader().getId().equals(leader.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the team leader can add members.");
        }

        Rescue rescue = rescueRepo.findByRescueId(rescueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rescue not found: " + rescueId));

        if (rescue.getRescueTeam() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Rescue " + rescue.getName() + " is already in another team.");
        }

        rescue.setRescueTeam(team);
        rescueRepo.save(rescue);

        return "✅ Added " + rescue.getName() + " to team " + team.getName();
    }

    // ✅ 5️⃣ ลบสมาชิกออกจากทีม (เฉพาะหัวหน้าทีมเท่านั้น)
    @DeleteMapping("/{teamId}/members/{rescueId}/remove/{leaderRescueId}")
    public String removeMember(@PathVariable String teamId,
                               @PathVariable String rescueId,
                               @PathVariable String leaderRescueId) {

        Rescue leader = rescueRepo.findByRescueId(leaderRescueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leader not found"));

        RescueTeam team = teamRepo.findByTeamId(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

        if (!team.getLeader().getId().equals(leader.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the team leader can remove members.");
        }

        Rescue rescue = rescueRepo.findByRescueId(rescueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rescue not found: " + rescueId));

        if (rescue.getRescueTeam() == null || !rescue.getRescueTeam().getId().equals(team.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Rescue " + rescue.getName() + " is not a member of this team.");
        }

        rescue.setRescueTeam(null);
        rescueRepo.save(rescue);

        return "🚫 Removed " + rescue.getName() + " from team " + team.getName();
    }

    // ✅ 6️⃣ ลบทีม (เฉพาะหัวหน้าทีมเท่านั้น)
    @DeleteMapping("/{teamId}/delete/{leaderRescueId}")
    public String deleteTeam(@PathVariable String teamId,
                             @PathVariable String leaderRescueId) {

        Rescue leader = rescueRepo.findByRescueId(leaderRescueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leader not found"));

        RescueTeam team = teamRepo.findByTeamId(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

        if (!team.getLeader().getId().equals(leader.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the team leader can delete this team.");
        }

        List<Rescue> members = rescueRepo.findAllByRescueTeam(team);
        for (Rescue r : members) {
            r.setRescueTeam(null);
            rescueRepo.save(r);
        }

        teamRepo.delete(team);
        return "🗑️ Team " + team.getName() + " deleted successfully. " + members.size() + " members detached.";
    }
}