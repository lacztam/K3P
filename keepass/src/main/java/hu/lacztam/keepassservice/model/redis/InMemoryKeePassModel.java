package hu.lacztam.keepassservice.model.redis;

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
    private byte[] keePassFileSerializationInBytes;
    private String email;

}
