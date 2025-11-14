package th.mfu.controller.rescuecontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import th.mfu.model.rescue.Rescue;
import th.mfu.model.rescue.RescueTeam;
import th.mfu.model.user.District;
import th.mfu.repository.rescuerepository.RescueRepository;
import th.mfu.repository.rescuerepository.RescueTeamRepository;
import th.mfu.repository.userrepository.DistrictRepository;

@RestController
@RequestMapping("/api/rescue-teams")
@CrossOrigin
public class RescueTeamController {

    @Autowired private RescueTeamRepository teamRepo;
    @Autowired private RescueRepository rescueRepo;
    @Autowired private DistrictRepository districtRepo;

    private boolean isLeader(Rescue rescue, RescueTeam team) {
        return team.getLeader().getId().equals(rescue.getId());
    }

    // ---------------------------------------------------------
    // 1️⃣ CREATE TEAM
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // 2️⃣ GET ALL TEAMS
    // ---------------------------------------------------------
    @GetMapping
    public List<RescueTeam> getAllTeams() {
        return teamRepo.findAll();
    }

    // ---------------------------------------------------------
    // 3️⃣ GET TEAM MEMBERS (with affiliatedUnit + leader info)
    // ---------------------------------------------------------
    @GetMapping("/{teamId}/members/{viewerRescueId}")
    public Map<String, Object> getTeamMembers(@PathVariable String teamId,
                                              @PathVariable String viewerRescueId) {

        RescueTeam team = teamRepo.findByTeamId(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found: " + teamId));

        Rescue viewer = rescueRepo.findByRescueId(viewerRescueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rescue not found: " + viewerRescueId));

        // ❌ ถ้า viewer ไม่ใช่สมาชิกทีมนี้
        if (viewer.getRescueTeam() == null || !viewer.getRescueTeam().getId().equals(team.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: not a member of this team.");
        }

        List<Rescue> members = rescueRepo.findAllByRescueTeam(team);
        List<Map<String, Object>> memberList = new ArrayList<>();

        for (Rescue r : members) {
            Map<String, Object> row = new HashMap<>();
            row.put("rescueId", r.getRescueId());
            row.put("name", r.getName());

            // ⭐ Add affiliatedUnit
            if (r.getAffiliatedUnit() != null) {
                row.put("affiliatedUnit", r.getAffiliatedUnit().getUnitName());
            } else {
                row.put("affiliatedUnit", "-");
            }

            // ⭐ Check leader
            row.put("isLeader", r.getId().equals(team.getLeader().getId()));

            memberList.add(row);
        }

        // ⭐ ส่ง leaderName + isViewerLeader กลับไปด้วย
        Map<String, Object> result = new HashMap<>();
        result.put("teamName", team.getName());
        result.put("teamId", team.getTeamId());
        result.put("district", team.getDistrict().getName());
        result.put("leaderName", team.getLeader().getName());
        result.put("isViewerLeader", viewer.getId().equals(team.getLeader().getId()));
        result.put("members", memberList);

        return result;
    }

    // ---------------------------------------------------------
    // 4️⃣ ADD MEMBER
    // ---------------------------------------------------------
    @PutMapping("/{teamId}/members/{rescueId}/add/{leaderRescueId}")
    public String addMember(
            @PathVariable String teamId,
            @PathVariable String rescueId,
            @PathVariable String leaderRescueId) {

        Rescue leader = rescueRepo.findByRescueId(leaderRescueId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leader not found"));

        RescueTeam team = teamRepo.findByTeamId(teamId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

        if (!isLeader(leader, team)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the team leader can add members.");
        }

        Rescue rescue = rescueRepo.findByRescueId(rescueId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rescue not found"));

        if (rescue.getRescueTeam() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Rescue " + rescue.getName() + " is already in another team.");
        }

        List<Rescue> members = rescueRepo.findAllByRescueTeam(team);

        if (members.size() >= 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Team already has 10 members (maximum).");
        }

        rescue.setRescueTeam(team);
        rescueRepo.save(rescue);

        return "Added " + rescue.getName() + " to team " + team.getName();
    }

    // ---------------------------------------------------------
    // 5️⃣ REMOVE MEMBER
    // ---------------------------------------------------------
    @DeleteMapping("/{teamId}/members/{rescueId}/remove/{leaderRescueId}")
    public String removeMember(@PathVariable String teamId,
                               @PathVariable String rescueId,
                               @PathVariable String leaderRescueId) {

        Rescue leader = rescueRepo.findByRescueId(leaderRescueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leader not found"));

        RescueTeam team = teamRepo.findByTeamId(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

        if (!isLeader(leader, team)) {
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

        return "Removed " + rescue.getName() + " from team " + team.getName();
    }

    // ---------------------------------------------------------
    // 6️⃣ DELETE TEAM
    // ---------------------------------------------------------
    @DeleteMapping("/{teamId}/delete/{leaderRescueId}")
    public String deleteTeam(@PathVariable String teamId,
                             @PathVariable String leaderRescueId) {

        Rescue leader = rescueRepo.findByRescueId(leaderRescueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leader not found"));

        RescueTeam team = teamRepo.findByTeamId(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

        if (!isLeader(leader, team)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the team leader can delete this team.");
        }

        List<Rescue> members = rescueRepo.findAllByRescueTeam(team);
        for (Rescue r : members) {
            r.setRescueTeam(null);
            rescueRepo.save(r);
        }

        teamRepo.delete(team);
        return "Team " + team.getName() + " deleted successfully. " + members.size() + " members detached.";
    }

    // ---------------------------------------------------------
    // 7️⃣ GET AVAILABLE RESCUES (affiliatedUnit included)
    // ---------------------------------------------------------
    @GetMapping("/available")
    public List<Map<String, Object>> getAvailableRescues() {

        List<Rescue> rescues = rescueRepo.findAllByRescueTeamIsNull();
        List<Map<String,Object>> list = new ArrayList<>();

        for (Rescue r : rescues) {
            Map<String, Object> obj = new HashMap<>();
            obj.put("rescueId", r.getRescueId());
            obj.put("name", r.getName());

            if (r.getAffiliatedUnit() != null)
                obj.put("affiliatedUnit", r.getAffiliatedUnit().getUnitName());
            else
                obj.put("affiliatedUnit", "-");

            list.add(obj);
        }

        return list;
    }
}
