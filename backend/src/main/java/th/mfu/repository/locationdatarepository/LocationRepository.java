package th.mfu.repository.locationdatarepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import th.mfu.model.locationdata.LocationData;

@Repository
public interface LocationRepository extends JpaRepository<LocationData, Long> {
    
}
