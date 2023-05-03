package hu.lacztam.cryptoservice.jms;

import hu.lacztam.cryptoservice.model.PublicPrivateKeyPair;
import hu.lacztam.cryptoservice.service.KeyPairService;
import hu.lacztam.model_lib.keepass_s_crypto_s.UserMailAndKeePassPw;
import lombok.AllArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static hu.lacztam.cryptoservice.config.JmsConfig.LOGIN_DECRYPT_KEEPASS_FILE_PASSWORD;


@Component
@AllArgsConstructor
public class DecryptKeePassFilePasswordConsumer {

    private final KeyPairService keyPairService;
    private final JmsTemplate jmsTemplate;
    private final SimpleMessageConverter converter = new SimpleMessageConverter();

    @JmsListener(destination = LOGIN_DECRYPT_KEEPASS_FILE_PASSWORD)
    public void onDecryptPassword(final Message message){
        UserMailAndKeePassPw userMailAndKeePassPw = null;
        try {
            userMailAndKeePassPw = (UserMailAndKeePassPw) ((ObjectMessage)message).getObject();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        String email = (String) userMailAndKeePassPw.getEmail();
        PublicPrivateKeyPair applicationUserKeyPairs = keyPairService.findByUserEmail(email);

        String decryptedPassword = null;
        try {
            decryptedPassword
                    = keyPairService.getDecrypted(userMailAndKeePassPw.getPassword(), applicationUserKeyPairs.getPrivateKey());

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }

        if(decryptedPassword != null){
            try {
                final String finalDecryptedPassword = decryptedPassword;
                jmsTemplate.send(message.getJMSReplyTo(), new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        Message decrpytedPasswordMessage = session.createTextMessage(finalDecryptedPassword);
                        decrpytedPasswordMessage.setJMSCorrelationID(message.getJMSCorrelationID());
                        return decrpytedPasswordMessage;
                    }
                });
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
