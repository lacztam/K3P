package hu.lacztam.userservice.repository;

import hu.lacztam.userservice.model.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {
    @Query("SELECT a FROM " +
            "ApplicationUser a " +
            "WHERE a.email = :email")
    Optional<ApplicationUser> findByEmailOptional(String email);

    @Query("SELECT a FROM " +
            "ApplicationUser a " +
            "JOIN FETCH a.roles " +
            "WHERE a.email = :email")
    Optional<ApplicationUser> findByEmailOptionalWithRoles(String email);

}
