package hu.lacztam.userservice.repository;

import hu.lacztam.userservice.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    @Query("SELECT t FROM " +
            "VerificationToken t " +
            "JOIN FETCH t.applicationUser " +
            "WHERE t.token = :token")
    VerificationToken findByToken(String token);

    @Query("SELECT t FROM " +
            "VerificationToken t " +
            "JOIN FETCH t.applicationUser " +
            "WHERE t.applicationUser.email = :email ")
    VerificationToken findByEmail(String email);
}
