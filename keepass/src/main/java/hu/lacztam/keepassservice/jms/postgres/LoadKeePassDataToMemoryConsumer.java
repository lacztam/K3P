package hu.lacztam.keepassservice.jms.postgres;

import hu.lacztam.keepassservice.dto.KeePassModelDto;
import hu.lacztam.keepassservice.mapper.InMemoryKeePassModelMapper;
import hu.lacztam.keepassservice.mapper.KeePassModelMapper;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import hu.lacztam.keepassservice.service.jms.LoadKeePassDataToMemoryConsumerService;
import hu.lacztam.keepassservice.service.postgres.KeePassService;
import hu.lacztam.keepassservice.service.redis.InMemoryKeePassService;
import hu.lacztam.model_lib.keepass_s_crypto_s.UserMailAndKeePassPw;
import lombok.AllArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import static hu.lacztam.keepassservice.config.JmsConfig.LOGIN_CONVERT_KEEPASS_DATA;

@Component
@AllArgsConstructor
public class LoadKeePassDataToMemoryConsumer {

    private final KeePassService keePassService;
    private final KeePassModelMapper keePassModelMapper;
    private final InMemoryKeePassModelMapper inMemoryKeePassModelMapper;
    private final InMemoryKeePassService inMemoryKeePassService;
    private final LoadKeePassDataToMemoryConsumerService loadKeePassDataToMemoryConsumerService;

    @JmsListener(destination = LOGIN_CONVERT_KEEPASS_DATA)
    public void onLoadKeePassDataToMemoryMessage(String email){
        if(email == null)
            throw new NullPointerException("Error, e-mail is null.");

        KeePassModel keePassModel = keePassService.findMainKeePassByUserEmail(email);
        String encryptedPassword = keePassModel.getEncryptedPassword();

        String decryptedPassword
                = loadKeePassDataToMemoryConsumerService.decryptPassword(new UserMailAndKeePassPw(email, encryptedPassword));

        KeePassModelDto keePassModelDto = keePassModelMapper.keePassModelToDtoWithKdbxFile(keePassModel);

        if(keePassModelDto != null)
            keePassModelDto.setDecryptedPasswordDto(decryptedPassword);

        InMemoryKeePassModel inMemoryKeePassModel = inMemoryKeePassModelMapper.dtoToInMemoryKeePassModel(keePassModelDto);

        inMemoryKeePassService.save(inMemoryKeePassModel);
    }

}
