package th.mfu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import th.mfu.model.*;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByNumber(String number);

    User findByNameAndNumber(String name, String number);
}
