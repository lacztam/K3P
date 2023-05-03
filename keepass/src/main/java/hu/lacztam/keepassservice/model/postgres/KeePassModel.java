package hu.lacztam.keepassservice.model.postgres;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.exception.KeePassDatabaseUnreadableException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "keepass_model")
public class KeePassModel {

    @Id
    @GeneratedValue
    private long id;
    @Column(unique = true)
    private String redisId;
    private byte[] kdbxFile;
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String kdbxFilePassword;
    private LocalDateTime created;
    @Column(unique = true)
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

    public boolean authenticateKeePassFile(String password) {
        if(getKeePassFile(password) != null) return true;
        throw new SecurityException("\nCan not authenticate KeePassFile.");
    }

}
