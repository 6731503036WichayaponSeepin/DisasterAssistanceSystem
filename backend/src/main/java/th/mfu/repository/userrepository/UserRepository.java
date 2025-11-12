package th.mfu.repository.userrepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import th.mfu.model.Detail;
import th.mfu.model.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ ตรวจสอบว่ามีผู้ใช้ที่ใช้เบอร์นี้อยู่หรือไม่ (ป้องกันซ้ำ)
    User findByPhoneNumber(String phoneNumber);

    // ✅ ใช้สำหรับ login โดยตรวจชื่อ (จาก Detail.name) และเบอร์โทร (จาก User.phoneNumber)
    User findByDetail_NameAndPhoneNumber(String name, String phoneNumber);

    // ✅ ดึงผู้ใช้ทั้งหมดที่เชื่อมกับ Detail เดียวกัน (เช่น ในบางกรณี user หลายคนแชร์ข้อมูล detail)
    List<User> findByDetail(Detail detail);
}