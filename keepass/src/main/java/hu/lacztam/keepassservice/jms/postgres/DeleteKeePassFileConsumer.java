package hu.lacztam.keepassservice.jms.postgres;

import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.service.postgres.KeePassService;
import lombok.AllArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import static hu.lacztam.keepassservice.config.JmsConfig.DELETE_KEEPASS_FILE;

@Component
@AllArgsConstructor
public class DeleteKeePassFileConsumer {

    private final KeePassService keePassService;
    private final JmsTemplate jmsTemplate;

    //TO-DO: delete both main and shared KeePassModel!
    @JmsListener(destination = DELETE_KEEPASS_FILE)
    public void onDeleteUserKeePassFile(Message message){

        String email= null;
        try {
            email = (String)((ObjectMessage)message).getObject();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\nonDeleteUserKeePassFile email: " + email);

        KeePassModel keePassModel = keePassService.findMainKeePassByUserEmail(email);
        keePassService.delete(keePassModel);

        try {
            jmsTemplate.send(message.getJMSReplyTo(), new MessageCreator() {

                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(true);
                }
            });
        } catch (JMSException e) {
            // jmsTemplate send back: false ?
            throw new RuntimeException(e);
        }
        System.out.println("msg received and send");
    }
}
