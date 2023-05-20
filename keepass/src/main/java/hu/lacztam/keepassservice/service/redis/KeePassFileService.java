package hu.lacztam.keepassservice.service.redis;

import de.slackspace.openkeepass.domain.KeePassFile;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import hu.lacztam.keepassservice.model.redis.KeePassFileSerialization;
import hu.lacztam.keepassservice.model.redis.KeePassFileSerializationBuilder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

@Service
@AllArgsConstructor
public class KeePassFileService {

    public KeePassFile getKeePassFile(byte[] keePassFileSerializationInBytes){
        try {
            KeePassFileSerialization keePassFileSerialization
                    = (KeePassFileSerialization) SerializationUtils.deserialize(keePassFileSerializationInBytes);;

            KeePassFile keePassFile = keePassFileSerialization;

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
        KeePassFileSerialization keePassFileSerialization = new KeePassFileSerializationBuilder(keePassFile).build();

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
