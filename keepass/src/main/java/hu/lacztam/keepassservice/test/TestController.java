package hu.lacztam.keepassservice.test;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.KeePassFile;
import hu.lacztam.keepassservice.dto.KeePassModelDto;
import hu.lacztam.keepassservice.mapper.InMemoryKeePassModelMapper;
import hu.lacztam.keepassservice.mapper.KeePassModelMapper;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import hu.lacztam.keepassservice.model.redis.KeePassFileSerialization;
import hu.lacztam.keepassservice.service.MakeKdbxByteService;
import hu.lacztam.keepassservice.service.redis.KeePassFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired MakeKdbxByteService makeKdbxByteService;
    @Autowired KeePassModelMapper keePassModelMapper;
    @Autowired InMemoryKeePassModelMapper inMemoryKeePassModelMapper;
    @Autowired KeePassFileService keePassFileService;


    @GetMapping("/2")
    public void test2() {
        String dbName = "t1.kdbx";
        String keePassFilePw = "1";
        //InputStream keePassStream = getClass().getClassLoader().getResourceAsStream(dbName);

        Resource resource = new ClassPathResource(dbName);

        InputStream keePassStream = null;
        try {
//            keePassStream = new ByteArrayInputStream(Files.readAllBytes(Path.of(dbName)));
            keePassStream = resource.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        KeePassFile keePassFile = KeePassDatabase.getInstance(keePassStream).openDatabase(keePassFilePw);
        System.err.println
                ("TestController / keePassFile.getGroups().get(0).getName(): "
                        + keePassFile.getGroups().get(0).getName()
                );

        KeePassFileSerialization keePassFileSerialization = new KeePassFileSerialization(keePassFile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        byte[] kfsInBytes;
        try {
            oos = new ObjectOutputStream( baos );
            oos.writeObject( keePassFileSerialization );
            oos.flush();
            kfsInBytes = baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(kfsInBytes);
        ObjectInput in = null;
        Object o = null;
        try {
            in = new ObjectInputStream(bis);
            o = in.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }

        keePassFileSerialization = (KeePassFileSerialization) o;

        System.err.println("after deserialization: "
                + keePassFileSerialization.getKeePassFile().getGroups().get(0).getEntries().get(0).getTitle());

//        String string = objectToString(keePassFileSerialization);
//
//        Object o = fromString(string);
//        keePassFileSerialization = (KeePassFileSerialization) o;
//        String groupName = keePassFileSerialization.getKeePassFile().getGroups().get(0).getName();

//        System.err.println("groupName: " + groupName);
    }

//    private String objectToString(Serializable o) throws IOException {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ObjectOutputStream oos = new ObjectOutputStream( baos );
//        oos.writeObject( o );
//        oos.close();
//        return Base64.getEncoder().encodeToString(baos.toByteArray());
//    }
//
//    private Object fromString( String s ) throws IOException ,
//            ClassNotFoundException {
//        byte [] data = Base64.getDecoder().decode( s );
//        ObjectInputStream ois = new ObjectInputStream(
//                new ByteArrayInputStream(  data ) );
//        Object o  = ois.readObject();
//        ois.close();
//        return o;

    @GetMapping("/1")
    public void test(){
        String dbName = "t1.kdbx";
        String keePassFilePw = "1";
        //InputStream keePassStream = getClass().getClassLoader().getResourceAsStream(dbName);

        Resource resource = new ClassPathResource(dbName);

        InputStream keePassStream = null;
        try {
//            keePassStream = new ByteArrayInputStream(Files.readAllBytes(Path.of(dbName)));
            keePassStream = resource.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        KeePassFile keePassFile = KeePassDatabase.getInstance(keePassStream).openDatabase(keePassFilePw);
        System.err.println
                ("TestController / keePassFile.getGroups().get(0).getName(): "
                + keePassFile.getGroups().get(0).getName()
                );

//        KeePassFileSerialization kfs = (KeePassFileSerialization) new KeePassFileSerializationBuilder(keePassFile).build();
//        byte[] kfsInBytes = SerializationUtils.serialize(kfs);
        byte[] serialize;
//        try {
//            serialize = serialize(kfs);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        Object deserialize = null;
//        try {
//            deserialize = deserialize(serialize);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        KeePassFileSerialization deserializedKFS = (KeePassFileSerialization) deserialize;

//        System.err.println("\nDESERIALIZED.getGroups().get(0).getName(): " +
//                deserializedKFS.getGroups().get(0).getName());

//        System.err.println("TestController /  kfs: " + kfs.getGroups().get(0).getEntries().get(0).getTitle() );

        KeePassModel keePassModel = new KeePassModel();
        byte[] kdbxFile = makeKdbxByteService.makeKdbx(keePassFile, keePassFilePw);
        keePassModel.setKdbxFile(kdbxFile);
        keePassModel.setId(1L);
        keePassModel.setEmail("testEmail");
        keePassModel.setRedisId("redisId");
        keePassModel.setEncryptedPassword(keePassFilePw);
        keePassModel.setCreated(LocalDateTime.now());

        KeePassModelDto keePassModelDto = keePassModelMapper.keePassModelToDtoWithKdbxFile(keePassModel);
        keePassModelDto.setDecryptedPasswordDto(keePassModel.getEncryptedPassword());
        System.err.println("TestController / KeePassModelDTO getEmail: " + keePassModelDto.getEmailDto());

        InMemoryKeePassModel inMemoryKeePassModel = inMemoryKeePassModelMapper.dtoToInMemoryKeePassModel(keePassModelDto);

        byte[] keePassFileSerializationInBytes = inMemoryKeePassModel.getKeePassFileSerializationInBytes();
        KeePassFile keePassFile1 = keePassFileService.getKeePassFile(keePassFileSerializationInBytes);
        String name = keePassFile1.getGroups().get(0).getName();
        System.err.println("name: " + name);

        System.err.println("TestController / inMemoryKeePassModel.getEmail():" + inMemoryKeePassModel.getEmail());
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

}
