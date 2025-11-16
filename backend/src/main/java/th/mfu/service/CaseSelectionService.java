package th.mfu.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import th.mfu.dto.CaseSelectionResponse;
import th.mfu.model.caseentity.AssistanceCase;
import th.mfu.model.caseentity.CaseStatus;
import th.mfu.model.locationdata.LocationData;
import th.mfu.model.user.User;
import th.mfu.repository.caserepo.AssistanceCaseRepository;
import th.mfu.repository.locationdatarepository.LocationRepository;
import th.mfu.repository.userrepository.UserRepository;

@Service
public class CaseSelectionService {

    private AssistanceCaseRepository caseRepo;
    private UserRepository userRepo;
    private LocationRepository locationRepo;

    @Autowired
    public CaseSelectionService(AssistanceCaseRepository caseRepo,
                                UserRepository userRepo,
                                LocationRepository locationRepo) {

        this.caseRepo = caseRepo;
        this.userRepo = userRepo;
        this.locationRepo = locationRepo;
    }

    /**
     * ดึงเคสทั้งหมดที่ยังไม่มีทีมรับ (สถานะ NEW)
     */
    public List<CaseSelectionResponse> getAvailableCases() {

        List<AssistanceCase> cases =
                caseRepo.findByAssignedRescueTeamIdIsNullAndStatus(CaseStatus.NEW);

        return convertToResponseList(cases);
    }

    /**
     * ดึงเคสตามประเภท: SOS / SUSTENANCE
     */
    public List<CaseSelectionResponse> getAvailableCasesByType(String caseType) {

        List<AssistanceCase> cases =
                caseRepo.findByAssignedRescueTeamIdIsNullAndStatus(CaseStatus.NEW);

        List<AssistanceCase> filtered = new ArrayList<>();

        for (AssistanceCase c : cases) {
            if (c.getCaseType().name().equalsIgnoreCase(caseType)) {
                filtered.add(c);
            }
        }

        return convertToResponseList(filtered);
    }

    /**
     * แปลง List<AssistanceCase> → List<CaseSelectionResponse>
     */
    private List<CaseSelectionResponse> convertToResponseList(List<AssistanceCase> cases) {

        List<CaseSelectionResponse> result = new ArrayList<>();

        for (AssistanceCase c : cases) {

            // ดึงข้อมูลผู้แจ้ง
            User reporter = userRepo.findById(c.getReporterUserId()).orElse(null);
            if (reporter == null) continue;

            // ดึงข้อมูลตำแหน่ง
            LocationData loc = c.getLocationId();
            if (loc == null) continue;

            String address = loc.getRoad() + ", " +
                             loc.getSubdistrict() + ", " +
                             loc.getDistrict() + ", " +
                             loc.getProvince() + " " +
                             loc.getPostcode();

            // เพิ่มลงผลลัพธ์
            result.add(new CaseSelectionResponse(
                    c.getId(),
                    reporter.getDetail().getName(),
                    reporter.getDetail().getPhoneNumber(),
                    c.getSeverity().name(),
                    address,
                    loc.getLatitude(),
                    loc.getLongitude()
            ));
        }

        return result;
    }
}