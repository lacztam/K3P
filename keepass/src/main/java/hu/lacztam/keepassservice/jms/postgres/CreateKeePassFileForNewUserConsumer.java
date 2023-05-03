package hu.lacztam.keepassservice.jms.postgres;

import de.slackspace.openkeepass.domain.*;
import hu.lacztam.keepassservice.config.ModelType;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.service.MakeKdbxByteService;
import hu.lacztam.keepassservice.service.PasswordGenerator;
import hu.lacztam.keepassservice.service.postgres.KeePassService;
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
import javax.persistence.EntityExistsException;
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

        System.err.println("creating new keepass model..");
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
        Group rootA = new GroupBuilder("A")
                    .addEntry(new EntryBuilder()
                    .title("A-SampleEntry")
                    .username("Example username")
                    .password("pw")
                    .url("https://keepass.info/")
                    .notes("Sample note")
                    .build())
                    .addGroup(
                        new GroupBuilder("B")
                                .addEntry(new EntryBuilder()
                                        .title("B-SampleEntry")
                                        .username("Example username")
                                        .password("pw")
                                        .url("https://keepass.info/")
                                        .notes("Sample note")
                                        .build())
                                .addGroup(
                                        new GroupBuilder("C")
                                                .addEntry(new EntryBuilder()
                                                        .title("C-SampleEntry")
                                                        .username("Example username")
                                                        .password("pw")
                                                        .url("https://keepass.info/")
                                                        .notes("Sample note")
                                                        .build())
                                                .addGroup(
                                                        new GroupBuilder("D")
                                                                .addEntry(new EntryBuilder()
                                                                        .title("D-SampleEntry")
                                                                        .username("Example username")
                                                                        .password("pw")
                                                                        .url("https://keepass.info/")
                                                                        .notes("Sample note")
                                                                        .build())
                                                                .build())
                                                .addGroup(new GroupBuilder("E")
                                                        .addEntry(new EntryBuilder()
                                                                .title("E-SampleEntry")
                                                                .username("Example username")
                                                                .password("pw")
                                                                .url("https://keepass.info/")
                                                                .notes("Sample note")
                                                                .build())
                                                        .build()).build())
                                .addGroup(new GroupBuilder("F")
                                        .addEntry(new EntryBuilder()
                                                .title("F-SampleEntry")
                                                .username("Example username")
                                                .password("pw")
                                                .url("https://keepass.info/")
                                                .notes("Sample note")
                                                .build())
                                        .build())
                                .addGroup(
                                        new GroupBuilder("G")
                                                .addEntry(new EntryBuilder()
                                                        .title("G-SampleEntry")
                                                        .username("Example username")
                                                        .password("pw")
                                                        .url("https://keepass.info/")
                                                        .notes("Sample note")
                                                        .build())
                                                .addGroup(new GroupBuilder("H")
                                                        .addEntry(new EntryBuilder()
                                                                .title("H-SampleEntry")
                                                                .username("Example username")
                                                                .password("pw")
                                                                .url("https://keepass.info/")
                                                                .notes("Sample note")
                                                                .build())
                                                        .addGroup(new GroupBuilder("I")
                                                                .addEntry(new EntryBuilder()
                                                                        .title("H-SampleEntry")
                                                                        .username("Example username")
                                                                        .password("pw")
                                                                        .url("https://keepass.info/")
                                                                        .notes("Sample note")
                                                                        .build())
                                                                .addGroup(new GroupBuilder("J")
                                                                        .addEntry(new EntryBuilder()
                                                                                .title("J-SampleEntry")
                                                                                .username("Example username")
                                                                                .password("pw")
                                                                                .url("https://keepass.info/")
                                                                                .notes("Sample note")
                                                                                .build())
                                                                        .build()).build())
                                                        .addGroup(
                                                                new GroupBuilder("K")
                                                                        .addEntry(new EntryBuilder()
                                                                                .title("K-SampleEntry")
                                                                                .username("Example username")
                                                                                .password("pw")
                                                                                .url("https://keepass.info/")
                                                                                .notes("Sample note")
                                                                                .build())
                                                                        .build())
                                                        .build())
                                                .addGroup(new GroupBuilder("L")
                                                        .addEntry(new EntryBuilder()
                                                                .title("L-SampleEntry")
                                                                .username("Example username")
                                                                .password("pw")
                                                                .url("https://keepass.info/")
                                                                .notes("Sample note")
                                                                .build())
                                                        .build()).build())
                                .build())
                .addGroup(new GroupBuilder("M")
                        .addEntry(new EntryBuilder()
                                .title("M-SampleEntry")
                                .username("Example username")
                                .password("pw")
                                .url("https://keepass.info/")
                                .notes("Sample note")
                                .build())
                        .build())
                .addGroup(new GroupBuilder("N")
                        .addEntry(new EntryBuilder()
                                .title("N-SampleEntry")
                                .username("Example username")
                                .password("pw")
                                .url("https://keepass.info/")
                                .notes("Sample note")
                                .build())
                        .addGroup(new GroupBuilder("O").addGroup(new GroupBuilder("P").addGroup(new GroupBuilder("Q").build()).build()).build())
                        .addGroup(new GroupBuilder("R").build()).addGroup(new GroupBuilder("S").addGroup(new GroupBuilder("T").build()).build()).build())
                .build();

        KeePassFile db = new KeePassFileBuilder(topGroupName).addTopGroups(rootA).build();
        return db;
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