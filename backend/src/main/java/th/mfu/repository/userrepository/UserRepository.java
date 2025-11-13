package th.mfu.repository.userrepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import th.mfu.model.Detail;
import th.mfu.model.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 🔹 หา user จาก Detail โดยตรง (ใช้ตอน login)
    Optional<User> findByDetail(Detail detail);

    // 🔹 หา user จาก phoneNumber ที่อยู่ใน Detail
    Optional<User> findByDetail_PhoneNumber(String phoneNumber);

   
}
