package hu.lacztam.keepassservice.dto;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.exception.KeePassDatabaseUnreadableException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeePassModelDto implements Serializable {

    private long kdbxFileIdDto;
    private String redisIdDto;
    private byte[] kdbxFileDto;
    private String decryptedPasswordDto;
    private LocalDateTime createdDto;
    private String emailDto;

    public InputStream returnKDBXAsInputStream(){
        if(this.kdbxFileDto == null)
            throw new NullPointerException("\n The kdbx file is null.");

        return new ByteArrayInputStream(this.kdbxFileDto);
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
