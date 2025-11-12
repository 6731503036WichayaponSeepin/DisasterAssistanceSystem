package th.mfu.repository.rescuerepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import th.mfu.model.rescue.Rescue;
import th.mfu.model.rescue.RescueTeam;

@Repository
public interface RescueTeamRepository extends JpaRepository<RescueTeam, Long> {

    // ✅ ค้นหาทีมจาก teamId (ใช้ตอนเพิ่ม/ลบสมาชิก หรือลบทีม)
    Optional<RescueTeam> findByTeamId(String teamId);

    // ✅ ค้นหาทีมที่หัวหน้าทีมเป็น Rescue คนนี้
    Optional<RescueTeam> findByLeader(Rescue leader);

    // ✅ ดึงรายชื่อทีมทั้งหมดในอำเภอที่ระบุ (District)
    List<RescueTeam> findByDistrict_Id(Long districtId);

    // ✅ ตรวจสอบว่ารหัส teamId นี้มีอยู่ในระบบแล้วหรือไม่
    boolean existsByTeamId(String teamId);

    // ✅ ดึงทีมที่ Rescue คนนี้สังกัดอยู่ (จะได้แค่ 1 ทีมเท่านั้น)
    Optional<RescueTeam> findByMembersContaining(Rescue rescue);
}