package th.mfu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import th.mfu.model.District;
import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findByProvinceId(Long provinceId);
}
