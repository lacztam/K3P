package hu.lacztam.keepassservice.jms.postgres;

import hu.lacztam.keepassservice.dto.KeePassModelDto;
import hu.lacztam.keepassservice.mapper.KeePassModelMapper;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import hu.lacztam.keepassservice.service.postgres.KeePassService;
import hu.lacztam.keepassservice.service.redis.InMemoryKeePassService;
import hu.lacztam.model_lib.keepass_s_crypto_s.UserMailAndKeePassPw;
import lombok.AllArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import static hu.lacztam.keepassservice.config.JmsConfig.LOGIN_CONVERT_KEEPASS_DATA;
import static hu.lacztam.keepassservice.config.JmsConfig.LOGIN_DECRYPT_KEEPASS_FILE_PASSWORD;

@Component
@AllArgsConstructor
public class LoadKeePassDataToFrontendConsumer {

    private KeePassService keePassService;
    private JmsTemplate jmsTemplate;
    private final SimpleMessageConverter converter = new SimpleMessageConverter();
    private KeePassModelMapper keePassModelMapper;
    private final InMemoryKeePassService inMemoryKeePassService;

    @JmsListener(destination = LOGIN_CONVERT_KEEPASS_DATA)
    public void onLoadKeePassDataToFrontendMessage(String email){
        if(email == null)
            throw new NullPointerException("Error, e-mail is null.");

        KeePassModel keePassModel = keePassService.findMainKeePassByUserEmail(email);
        String encryptedPassword = keePassModel.getKdbxFilePassword();

        UserMailAndKeePassPw userMailAndKeePassPw = new UserMailAndKeePassPw(email, encryptedPassword);
        String decryptedPassword = decryptPassword(userMailAndKeePassPw);

        KeePassModelDto keePassModelDto = keePassModelMapper.keePassModelToDtoWithKdbxFile(keePassModel);
        if(keePassModelDto != null)
            keePassModelDto.setKdbxFilePasswordDto(decryptedPassword);

        InMemoryKeePassModel inMemoryKeePassModel = keePassModelMapper.dtoToInMemoryKeePassModel(keePassModelDto);

        inMemoryKeePassService.save(inMemoryKeePassModel);
    }

    private String decryptPassword(UserMailAndKeePassPw userMailAndKeePassPw){
        Object receivedObject
                = this.jmsTemplate.sendAndReceive(
                LOGIN_DECRYPT_KEEPASS_FILE_PASSWORD,
                new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createObjectMessage(userMailAndKeePassPw);
                    }
                });

        Object decryptedPasswordObj = null;
        try {
            decryptedPasswordObj = this.converter.fromMessage((Message) receivedObject);
        } catch (JMSException e) {
            throw new RuntimeException(e.getMessage());
        }

        String decryptedPassword = null;
        if(decryptedPasswordObj != null)
            decryptedPassword = (String) decryptedPasswordObj;
        else{
            throw new NullPointerException("Decrypted password can not be null.");
        }

        return decryptedPassword;
    }

}
