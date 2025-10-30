package th.mfu.repository.userrepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.model.user.PostalCode;

public interface PostalCodeRepository extends JpaRepository<PostalCode, Long> {
    List<PostalCode> findBySubdistrictId(Long subdistrictId);
}
