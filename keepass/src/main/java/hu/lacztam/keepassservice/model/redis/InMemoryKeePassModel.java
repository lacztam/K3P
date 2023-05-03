package hu.lacztam.keepassservice.model.redis;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.exception.KeePassDatabaseUnreadableException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@RedisHash("KeePassFile")
public class InMemoryKeePassModel implements Serializable {

    private static final long serialVersionUID = -7817224776021728682L;
    @Id
    private String id;
    private long postgresId;
    private byte [] kdbxFile;
    private String password;
    //    @org.springframework.data.redis.core.index.Indexed
    private String email;

    public InputStream returnKDBXAsInputStream(){
        if(this.kdbxFile == null)
            throw new NullPointerException("\n The kdbx file is null.");
        return new ByteArrayInputStream(this.kdbxFile);
    }

    public KeePassFile getKeePassFile(String password){
        try {
            return KeePassDatabase.getInstance(returnKDBXAsInputStream()).openDatabase(password);
        }catch (KeePassDatabaseUnreadableException e){
            e.printStackTrace();
        }

        throw new SecurityException("Can not authenticate KeePassFile.");
    }

    public Group getRootGroup(String password) {
        try {
            KeePassFile keePassFile = KeePassDatabase.getInstance(returnKDBXAsInputStream()).openDatabase(password);
            return keePassFile.getRoot().getGroups().get(0);
        }catch (KeePassDatabaseUnreadableException e){
            e.printStackTrace();
        }
        throw new SecurityException("Can not authenticate KeePassFile.");
    }
}
