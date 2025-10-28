package th.mfu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import th.mfu.model.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {}
