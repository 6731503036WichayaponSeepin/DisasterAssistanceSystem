package th.mfu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import th.mfu.model.Subdistrict;
import java.util.List;

public interface SubdistrictRepository extends JpaRepository<Subdistrict, Long> {
    List<Subdistrict> findByDistrictId(Long districtId);
}
