package th.mfu.repository.locationdatarepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import th.mfu.model.locationdata.LocationData;
import th.mfu.model.user.User;

@Repository
public interface LocationRepository extends JpaRepository<LocationData, Long> {

    Optional<LocationData> findFirstByUserOrderByIdDesc(User user);

}
