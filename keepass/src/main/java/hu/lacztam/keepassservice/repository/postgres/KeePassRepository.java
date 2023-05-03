package hu.lacztam.keepassservice.repository.postgres;

import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface KeePassRepository extends JpaRepository<KeePassModel, Long> {

    @Query("SELECT k FROM KeePassModel k " +
            "WHERE k.redisId = :keePassFileName ")
    Optional<KeePassModel> findbyName(String keePassFileName);

    @Query("SELECT k FROM KeePassModel k " +
            "WHERE k.id = :kdbxFileId " +
            "AND " +
            "k.email = :email ")
    Optional<KeePassModel> findByIDWithAttachmentsWithUserAuth(long kdbxFileId, String email);

    @Query("SELECT k.id FROM KeePassModel k")
    List<Long> getAllIds();

    @Query("SELECT k FROM KeePassModel k " +
            "WHERE k.id = :kdbxFileId " +
            "AND " +
            "k.email = :email ")
    Optional<KeePassModel> findByIdNoAttachmentWithUserAuth(long kdbxFileId, String email);

    @Query("SELECT k FROM KeePassModel k " +
            "WHERE k.id = :kdbxFileId AND " +
            "k.email = :email ")
    Optional<KeePassModel> findByIDWithUserCheck(long kdbxFileId, String email);

    @Query("SELECT k FROM KeePassModel k " +
            "WHERE k.redisId = :redisId ")
    Optional<KeePassModel> findMainByRedisId(String redisId);

    @Query("SELECT k FROM KeePassModel k " +
            "WHERE k.redisId = :redisId ")
    Optional<KeePassModel> findSharedByRedisId(String redisId);
}