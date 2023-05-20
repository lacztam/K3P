package hu.lacztam.keepassservice.model.redis;

import de.slackspace.openkeepass.domain.KeePassFile;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@Data
@Builder
public class KeePassFileSerialization implements Serializable {

    private static final long serialVersionUID = 1L;
    private transient KeePassFile keePassFile;

    public KeePassFileSerialization() {
    }

    public KeePassFileSerialization(KeePassFile keePassFile) {
        this.keePassFile = keePassFile;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.keePassFile);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.keePassFile = (KeePassFile) in.readObject();
    }

}
