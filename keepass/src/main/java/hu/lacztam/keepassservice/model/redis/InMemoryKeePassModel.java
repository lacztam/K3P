package hu.lacztam.keepassservice.model.redis;

import de.slackspace.openkeepass.domain.KeePassFile;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@RedisHash("KeePassFile")
@Builder
public class InMemoryKeePassModel implements Serializable {

    private static final long serialVersionUID = -7817224776021728682L;
    @Id
    private String id;
    private long postgresId;
    private KeePassFile keePassFile;
    private String email;

}
