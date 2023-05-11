package hu.lacztam.keepassservice.mapper;

import hu.lacztam.keepassservice.dto.KeePassModelDto;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import hu.lacztam.keepassservice.service.MakeKdbxByteService;
import hu.lacztam.keepassservice.service.jms.LoadKeePassDataToMemoryConsumerService;
import hu.lacztam.keepassservice.service.postgres.KeePassService;
import hu.lacztam.model_lib.keepass_s_crypto_s.UserMailAndKeePassPw;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InMemoryKeePassModelMapper {

    private final MakeKdbxByteService makeKdbxByteService;
    private final KeePassService keePassService;
    private final KeePassModelMapper keePassModelMapper;
    private final LoadKeePassDataToMemoryConsumerService loadKeePassDataToMemoryConsumerService;

    public InMemoryKeePassModel dtoToInMemoryKeePassModel(KeePassModelDto keePassModelDto) {
        return new InMemoryKeePassModel()
                .builder()
                .id(keePassModelDto.getRedisIdDto())
                .postgresId(keePassModelDto.getKdbxFileIdDto())
                .keePassFile(keePassModelDto.getKeePassFile( keePassModelDto.getKdbxFilePasswordDto() ))
                .email(keePassModelDto.getEmailDto())
                .build();
    }

    public KeePassModelDto inMemoryModelToDto(InMemoryKeePassModel inMemoryKeePassModel) {
        KeePassModel keePassModel = keePassService.findMainKeePassByUserEmail(inMemoryKeePassModel.getEmail());

        KeePassModelDto keePassModelDto = keePassModelMapper.keePassModelToDto(keePassModel);

        UserMailAndKeePassPw userMailAndKeePassPw
                = new UserMailAndKeePassPw(inMemoryKeePassModel.getEmail(), keePassModel.getKdbxFilePassword());

        String decryptedPassword = loadKeePassDataToMemoryConsumerService.decryptPassword(userMailAndKeePassPw);

        byte[] kdbxByteFromInMemoryModel
                = makeKdbxByteService.makeKdbx(inMemoryKeePassModel.getKeePassFile(), decryptedPassword);

        return keePassModelDto
                .builder()
                .kdbxFileDto(kdbxByteFromInMemoryModel)
                .build();
    }

    public KeePassModel inMemoryModelToKeePass(InMemoryKeePassModel inMemoryKeePassModel) {
        KeePassModelDto dto = inMemoryModelToDto(inMemoryKeePassModel);

        return KeePassModel
                .builder()
                .id(dto.getKdbxFileIdDto())
                .redisId(dto.getRedisIdDto())
                .kdbxFile(dto.getKdbxFileDto())
                .email(dto.getEmailDto())
                .kdbxFilePassword(dto.getKdbxFilePasswordDto())
                .created(dto.getCreatedDto())
                .build();
    }

}
