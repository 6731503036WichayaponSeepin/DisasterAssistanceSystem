package th.mfu.repository.userrepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.model.user.District;

public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findByProvinceId(Long provinceId);
}
