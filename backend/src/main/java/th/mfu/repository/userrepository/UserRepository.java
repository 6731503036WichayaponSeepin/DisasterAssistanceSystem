package th.mfu.repository.userrepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.model.Detail;
import th.mfu.model.user.Address;
import th.mfu.model.user.User;


public interface UserRepository extends JpaRepository<User, Long> {

    User findByPhoneNumber(String phoneNumber);

    List<User> findByDetail(Detail detail);
    List<User> findByAddress(Address address);

    User findByDetail_NameAndPhoneNumber(String name, String phoneNumber);
    
}
