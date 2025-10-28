package th.mfu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import th.mfu.model.PostalCode;
import java.util.List;

public interface PostalCodeRepository extends JpaRepository<PostalCode, Long> {
    List<PostalCode> findBySubdistrictId(Long subdistrictId);
}
