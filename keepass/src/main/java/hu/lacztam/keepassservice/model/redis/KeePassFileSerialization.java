package hu.lacztam.keepassservice.model.redis;

import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.domain.KeePassFileContract;

import java.io.Serializable;

public class KeePassFileSerialization extends KeePassFile implements Serializable {
    private static final long serialVersionUID = 12345L;

    KeePassFileSerialization(){
        super(null);
    }

    public KeePassFileSerialization(KeePassFileContract keePassFileContract){
        super(keePassFileContract);
    }
}
