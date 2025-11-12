package th.mfu.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.model.Detail;

public interface DetailRepository extends JpaRepository<Detail, Long> { };
