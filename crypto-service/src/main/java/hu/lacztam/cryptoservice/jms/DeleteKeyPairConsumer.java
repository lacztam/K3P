package hu.lacztam.cryptoservice.jms;

import hu.lacztam.cryptoservice.model.PublicPrivateKeyPair;
import hu.lacztam.cryptoservice.service.KeyPairService;
import lombok.AllArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import static hu.lacztam.cryptoservice.config.JmsConfig.DELETE_KEYPAIR;

@Component
@AllArgsConstructor
public class DeleteKeyPairConsumer {

    private final KeyPairService keyPairService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = DELETE_KEYPAIR)
    public void onDeleteUserKeyPair(Message message){

        String email= null;
        try {
            email = (String)((ObjectMessage)message).getObject();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        PublicPrivateKeyPair keyPair = keyPairService.findByUserEmail(email);
        keyPairService.delete(keyPair);

        try {
            jmsTemplate.send(message.getJMSReplyTo(), new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(true);
                }
            });
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
