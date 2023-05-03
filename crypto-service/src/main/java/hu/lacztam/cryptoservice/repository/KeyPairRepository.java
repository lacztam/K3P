package hu.lacztam.cryptoservice.repository;

import hu.lacztam.cryptoservice.model.PublicPrivateKeyPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface KeyPairRepository extends JpaRepository<PublicPrivateKeyPair, Long> {

    @Query("SELECT kp FROM PublicPrivateKeyPair kp " +
            "WHERE kp.email = :email")
    Optional<PublicPrivateKeyPair> findByUserEmail(String email);

}
