package hu.lacztam.keepassservice.service.jms;

import hu.lacztam.model_lib.keepass_s_crypto_s.UserMailAndKeePassPw;
import lombok.AllArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import static hu.lacztam.keepassservice.config.JmsConfig.LOGIN_DECRYPT_KEEPASS_FILE_PASSWORD;

@Service
@AllArgsConstructor
public class LoadKeePassDataToMemoryConsumerService {

    private final JmsTemplate jmsTemplate;
    private final SimpleMessageConverter converter = new SimpleMessageConverter();

    public String decryptPassword(UserMailAndKeePassPw userMailAndKeePassPw){
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
