package hu.lacztam.keepassservice.mapper;

import de.slackspace.openkeepass.domain.KeePassFile;
import hu.lacztam.keepassservice.dto.KeePassModelDto;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import hu.lacztam.keepassservice.model.redis.KeePassFileSerialization;
import hu.lacztam.keepassservice.service.MakeKdbxByteService;
import hu.lacztam.keepassservice.service.jms.LoadKeePassDataToMemoryConsumerService;
import hu.lacztam.keepassservice.service.postgres.KeePassService;
import hu.lacztam.keepassservice.service.redis.KeePassFileService;
import hu.lacztam.model_lib.keepass_s_crypto_s.UserMailAndKeePassPw;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

@Component
@AllArgsConstructor
public class InMemoryKeePassModelMapper {

    private final MakeKdbxByteService makeKdbxByteService;
    private final KeePassService keePassService;
    private final KeePassModelMapper keePassModelMapper;
    private final KeePassFileService  keePassFileService;
    private final LoadKeePassDataToMemoryConsumerService loadKeePassDataToMemoryConsumerService;

    public InMemoryKeePassModel dtoToInMemoryKeePassModel(KeePassModelDto keePassModelDto) {
         InMemoryKeePassModel inMemoryKeePassModel =  new InMemoryKeePassModel()
                .builder()
                .id(keePassModelDto.getRedisIdDto())
                .postgresId(keePassModelDto.getKdbxFileIdDto())
                .email(keePassModelDto.getEmailDto())
                .build();

        KeePassFileSerialization keePassFileSerialization = new KeePassFileSerialization(
                keePassModelDto.getKeePassFile(keePassModelDto.getDecryptedPasswordDto())
            );

        byte[] keePassFileSerializationBytesArray
                = keePassFileService.serializeKeePassFileIntoByteArray(keePassFileSerialization);

        inMemoryKeePassModel.setKeePassFileSerializationInBytes(keePassFileSerializationBytesArray);

        return inMemoryKeePassModel;
    }

    public KeePassModel inMemoryModelToKeePass(InMemoryKeePassModel inMemoryKeePassModel) {
        KeePassModelDto dto = inMemoryModelToDto(inMemoryKeePassModel);

        return KeePassModel
                .builder()
                .id(dto.getKdbxFileIdDto())
                .redisId(dto.getRedisIdDto())
                .kdbxFile(dto.getKdbxFileDto())
                .email(dto.getEmailDto())
                .encryptedPassword(dto.getDecryptedPasswordDto())
                .created(dto.getCreatedDto())
                .build();
    }

    private KeePassModelDto inMemoryModelToDto(InMemoryKeePassModel inMemoryKeePassModel) {
        KeePassModel keePassModel = keePassService.findMainKeePassByUserEmail(inMemoryKeePassModel.getEmail());

        KeePassModelDto keePassModelDto = keePassModelMapper.keePassModelToDto(keePassModel);

        UserMailAndKeePassPw userMailAndKeePassPw
                = new UserMailAndKeePassPw(inMemoryKeePassModel.getEmail(), keePassModel.getEncryptedPassword());

        String decryptedPassword = loadKeePassDataToMemoryConsumerService.decryptPassword(userMailAndKeePassPw);

        byte[] keePassFileSerializationInBytes = inMemoryKeePassModel.getKeePassFileSerializationInBytes();
        KeePassFile keePassFile = (KeePassFile) SerializationUtils.deserialize(keePassFileSerializationInBytes);

        byte[] kdbxByteFromInMemoryModel
                = makeKdbxByteService.makeKdbx(keePassFile, decryptedPassword);

        return keePassModelDto
                .builder()
                .kdbxFileDto(kdbxByteFromInMemoryModel)
                .decryptedPasswordDto(keePassModel.getEncryptedPassword())
                .build();
    }

}
