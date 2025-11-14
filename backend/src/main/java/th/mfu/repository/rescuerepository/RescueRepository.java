package th.mfu.repository.rescuerepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import th.mfu.model.Detail;
import th.mfu.model.rescue.Rescue;
import th.mfu.model.rescue.RescueTeam;

@Repository
public interface RescueRepository extends JpaRepository<Rescue, Long> {

    // ✅ ค้นหากู้ภัยจากรหัสเฉพาะ (ใช้ตอน login หรือค้นหาสมาชิก)
    Optional<Rescue> findByRescueId(String rescueId);
    
    Optional<Rescue> findByDetail(Detail detail);
    // ✅ ดึงรายชื่อกู้ภัยทั้งหมดตามหน่วยสังกัด
    List<Rescue> findByAffiliatedUnit_Id(Long unitId);

    // ✅ ดึงกู้ภัยทั้งหมดในทีมที่ระบุ
    List<Rescue> findAllByRescueTeam(RescueTeam team);

    // ✅ ดึงกู้ภัยที่ยังไม่มีทีม (ใช้ตอนเพิ่มสมาชิกเข้าใหม่)
    List<Rescue> findAllByRescueTeamIsNull();

    // ✅ ตรวจสอบว่ามี RescueId นี้อยู่ในระบบหรือไม่
    boolean existsByRescueId(String rescueId);
}
