package th.mfu.repository.rescuerepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.model.rescue.Rescue;

public interface RescueRepository extends JpaRepository<Rescue, Long> {
    Optional<Rescue> findByRescueId(String rescueId);

    // 🔹 ดึงรายชื่อกู้ภัยทั้งหมดตามหน่วยสังกัด
    List<Rescue> findByAffiliatedUnit_Id(Long unitId);
}
