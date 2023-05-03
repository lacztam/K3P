package hu.lacztam.userservice.repository;

import hu.lacztam.userservice.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    Optional<UserRole> findByRolename(String rolename);

}
