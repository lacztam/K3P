package hu.lacztam.cryptoservice.jms;

import hu.lacztam.cryptoservice.model.PublicPrivateKeyPair;
import hu.lacztam.cryptoservice.service.KeyPairService;
import hu.lacztam.model_lib.keepass_s_crypto_s.UserMailAndKeePassPw;
import lombok.AllArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.management.openmbean.KeyAlreadyExistsException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static hu.lacztam.cryptoservice.config.JmsConfig.NEW_ACCOUNT_CREATE_KEYPAIR_ENCRYPT_KEEPASS_PASSWORD;

@Component
@AllArgsConstructor
public class NewAccountCreateKeyPairConsumer {

    private final KeyPairService keyPairService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = NEW_ACCOUNT_CREATE_KEYPAIR_ENCRYPT_KEEPASS_PASSWORD)
    public void onCreateKeyPairAndEncryptPassword(final Message message){

        UserMailAndKeePassPw mailAndPw = null;
        try {
            mailAndPw =  (UserMailAndKeePassPw) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
        if(mailAndPw == null)
            throw new NullPointerException("Object can not be null.");

        boolean keyPairExistForUser = keyPairService.isKeyPairExist(mailAndPw.getEmail());
        if(keyPairExistForUser)
            throw new KeyAlreadyExistsException("Key pair already exists for user: " + mailAndPw.getEmail());

        PublicPrivateKeyPair keyPair = keyPairService.generateKeyPairAndSave();
        keyPair.setEmail(mailAndPw.getEmail());

        String encryptedPassword = null;
        try {
            encryptedPassword = keyPairService.getEncrypted(mailAndPw.getPassword(), keyPair.getPublicKey());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }

        if(encryptedPassword == null)
            throw new NullPointerException("Encrypted password can not be null.");

        keyPairService.save(keyPair);

        try {
            String finalEncryptedPassword = encryptedPassword;
            jmsTemplate.send(message.getJMSReplyTo(), new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    Message responseMsg = session.createTextMessage(finalEncryptedPassword);
                    responseMsg.setJMSCorrelationID(message.getJMSCorrelationID());
                    return responseMsg;
                }
            });
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

}
