package th.mfu.repository.rescuerepository;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.model.rescue.AffiliatedUnit;

public interface AffiliatedUnitRepository extends JpaRepository<AffiliatedUnit, Long> {

}