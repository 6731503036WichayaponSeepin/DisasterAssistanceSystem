package th.mfu.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.model.Detail;

public interface DetailRepository extends JpaRepository<Detail, Long> { 


     // 🔍 ใช้ตอนเช็กเบอร์ซ้ำ
    Optional<Detail> findByPhoneNumber(String phoneNumber);

    // 🔍 ใช้ตอน login จาก name + phone ก็ได้ในอนาคต
    Optional<Detail> findByNameAndPhoneNumber(String name, String phoneNumber);
    
};
