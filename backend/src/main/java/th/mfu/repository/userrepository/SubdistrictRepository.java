package th.mfu.repository.userrepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.model.user.Subdistrict;

public interface SubdistrictRepository extends JpaRepository<Subdistrict, Long> {
    List<Subdistrict> findByDistrictId(Long districtId);
}
