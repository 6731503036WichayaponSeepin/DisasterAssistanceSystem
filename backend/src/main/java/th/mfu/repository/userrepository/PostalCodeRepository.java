package th.mfu.repository.userrepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.model.user.PostalCode;
import th.mfu.model.user.Subdistrict;

public interface PostalCodeRepository extends JpaRepository<PostalCode, Long> {
    List<PostalCode> findBySubdistrictId(Long subdistrictId);

    Optional<PostalCode> findFirstBySubdistrict(Subdistrict subdistrict);
}
