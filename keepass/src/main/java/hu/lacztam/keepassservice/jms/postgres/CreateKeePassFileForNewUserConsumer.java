package hu.lacztam.keepassservice.jms.postgres;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.EntryBuilder;
import de.slackspace.openkeepass.domain.GroupBuilder;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.domain.KeePassFileBuilder;
import hu.lacztam.keepassservice.config.ModelType;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.service.MakeKdbxByteService;
import hu.lacztam.keepassservice.service.PasswordGenerator;
import hu.lacztam.keepassservice.service.postgres.KeePassService;
import hu.lacztam.model_lib.keepass_s_crypto_s.UserMailAndKeePassPw;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.persistence.EntityExistsException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import static hu.lacztam.keepassservice.config.JmsConfig.NEW_ACCOUNT_CREATE_KEEPASS_FILE;
import static hu.lacztam.keepassservice.config.JmsConfig.NEW_ACCOUNT_CREATE_KEYPAIR_ENCRYPT_KEEPASS_PASSWORD;

@Component
@AllArgsConstructor
public class CreateKeePassFileForNewUserConsumer {

    private final MakeKdbxByteService makeKdbxByteService;
    private final KeePassService keePassService;
    private final JmsTemplate jmsTemplate;
    private final SimpleMessageConverter converter = new SimpleMessageConverter();

    //TO-DO: create shared keepass model
    @JmsListener(destination = NEW_ACCOUNT_CREATE_KEEPASS_FILE)
    public void onCreateKeePassFileForNewUserMessage(String email){
        checkModelsExist(email);

        KeePassFile keePassFile = createTestKeePassFile("Main");
        String password = generatePassword(100);
        byte[] kdbxFileInBytes = makeKdbxByteService.makeKdbx(keePassFile, password);

        String encryptedPassword = encryptPassword(email, password);

        KeePassModel mainKeePassModel = createKeePassModel(email, kdbxFileInBytes, encryptedPassword, ModelType.MAIN_KEEPASS);
//        KeePassModel sharedKeePassModel = createKeePassModel(email, kdbxFileInBytes, encryptedPassword, ModelType.SHARED_KEEPASS);

        keePassService.save(mainKeePassModel);
//        keePassService.save(sharedKeePassModel);
    }

    private void checkModelsExist(String email){
        boolean isMainExists = keePassService.isMainKeePassAlreadyExists(email);
        if(isMainExists)
            throw new EntityExistsException("Main KeePass model already exists with email: " + email);

        boolean isSharedExists = keePassService.isSharedKeePassAlreadyExists(email);
        if(isSharedExists)
            throw new EntityExistsException("Shared KeePass model already exists with email: " + email);
    }

    private String generatePassword(int length){
        PasswordGenerator pwg = new PasswordGenerator(length,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true);

        return pwg.generatePassword();
    }

    private KeePassFile createDefaultKeePassFile(String topGroupName){
        KeePassFile keePassFile
                = new KeePassFileBuilder(topGroupName)
                .addTopGroups(
                        new GroupBuilder(topGroupName)
                                .addEntry(new EntryBuilder()
                                        .title("SampleEntry 2")
                                        .username("Example username1")
                                        .password("pass")
                                        .url("https://keepass.info/")
                                        .notes("Sample note")
                                        .build())
                                .addEntry(new EntryBuilder()
                                        .title("SampleEntry 2")
                                        .username("Example username2")
                                        .password("pw")
                                        .url("https://keepass.info/")
                                        .notes("Sample note")
                                        .build())
                                .build())
                .build();

        return keePassFile;
    }

    private KeePassFile createTestKeePassFile(String topGroupName){
            String dbName = "t1.kdbx";
            String keePassFilePw = "1";
            Resource resource = new ClassPathResource(dbName);

            InputStream keePassStream = null;
            try {
                keePassStream = resource.getInputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            KeePassFile keePassFile = KeePassDatabase.getInstance(keePassStream).openDatabase(keePassFilePw);

            return keePassFile;
    }

    private String encryptPassword(String email, String password) {
        Object encryptedPasswordObject
                = this.jmsTemplate.sendAndReceive(
                NEW_ACCOUNT_CREATE_KEYPAIR_ENCRYPT_KEEPASS_PASSWORD,
                new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        UserMailAndKeePassPw userMailAndKeePassPw = new UserMailAndKeePassPw(email, password);
                        return session.createObjectMessage(userMailAndKeePassPw);
                    }
                });

        String encryptedPassword = null;
        try {
            Object cipherPassword = this.converter.fromMessage((Message) encryptedPasswordObject);

            if(cipherPassword == null)
                throw new NullPointerException("Password encryption failed.");

            encryptedPassword = String.valueOf(cipherPassword);

        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        if(encryptedPassword == null)
            throw new NullPointerException("Password encryption failed.");

        return encryptedPassword;
    }

    private KeePassModel createKeePassModel(String email, byte[] kdbxFileInBytes, String encryptedPassword, String type){
        KeePassModel newKeePassModel = new KeePassModel().builder()
                .redisId(email + type)
                .email(email)
                .kdbxFile(kdbxFileInBytes)
                .kdbxFilePassword(encryptedPassword)
                .created(LocalDateTime.now())
                .build();

        System.err.println("create keepass file consumer / createKeePassModel / newKeePassModel.toString():\n"
        + newKeePassModel.getRedisId());

        if(newKeePassModel == null)
            throw new NullPointerException("KeePass model can not be null.");

        return newKeePassModel;
    }

}