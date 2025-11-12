package th.mfu.repository.userrepository;

import org.springframework.data.jpa.repository.JpaRepository;

import th.mfu.model.user.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {}
