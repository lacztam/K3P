package hu.lacztam.keepassservice.service.redis;

import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.domain.KeePassFileBuilder;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import hu.lacztam.keepassservice.model.redis.KeePassFileSerialization;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

@Service
@AllArgsConstructor
public class KeePassFileService {

    public KeePassFile getKeePassFile(byte[] keePassFileSerializationInBytes){
        KeePassFileSerialization keePassFileSerialization = null;
        try {
            Object deserialize = SerializationUtils.deserialize(keePassFileSerializationInBytes);

            keePassFileSerialization = (KeePassFileSerialization) deserialize;

            KeePassFile keePassFile = new KeePassFileBuilder(keePassFileSerialization.getKeePassFile()).build();

            return keePassFile;
        }catch (Exception e){
            e.printStackTrace();
        }

        throw new RuntimeException("Can not deserialize KeePassFile object.");
    }

    public KeePassFile getKeePassFile(InMemoryKeePassModel inMemoryKeePassModel){
        if(inMemoryKeePassModel == null)
            throw new NullPointerException("InMemoryKeePassModel can not be null.");

        KeePassFile keePassFile = getKeePassFile(inMemoryKeePassModel.getKeePassFileSerializationInBytes());

        return keePassFile;
    }

    public byte[] serializeKeePassFileIntoByteArray(KeePassFile keePassFile){
        KeePassFileSerialization keePassFileSerialization = new KeePassFileSerialization(keePassFile);

        byte[] keePassFileInBytes = new byte[0];
        try{
            keePassFileInBytes = SerializationUtils.serialize(keePassFileSerialization);
        }catch (Exception e){
            e.printStackTrace();
        }

        return keePassFileInBytes;
    }

    public byte[] serializeKeePassFileIntoByteArray(KeePassFileSerialization keePassFileSerialization){

        byte[] keePassFileInBytes = new byte[0];
        try{
            keePassFileInBytes = SerializationUtils.serialize(keePassFileSerialization);
        }catch (Exception e){
            e.printStackTrace();
        }

        return keePassFileInBytes;
    }

}
