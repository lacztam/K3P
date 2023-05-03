package hu.lacztam.keepassservice.repository.redis;

import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FrontendKeePassRepository extends CrudRepository<InMemoryKeePassModel, String> {


}