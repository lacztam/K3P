package hu.lacztam.keepassservice.test;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.KeePassFile;
import hu.lacztam.keepassservice.dto.KeePassModelDto;
import hu.lacztam.keepassservice.mapper.InMemoryKeePassModelMapper;
import hu.lacztam.keepassservice.mapper.KeePassModelMapper;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import hu.lacztam.keepassservice.model.redis.KeePassFileSerialization;
import hu.lacztam.keepassservice.model.redis.KeePassFileSerializationBuilder;
import hu.lacztam.keepassservice.service.MakeKdbxByteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired MakeKdbxByteService makeKdbxByteService;
    @Autowired KeePassModelMapper keePassModelMapper;
    @Autowired InMemoryKeePassModelMapper inMemoryKeePassModelMapper;

    @GetMapping
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
        System.err.println("TestController / keePassFile.getGroups().get(0).getName(): " + keePassFile.getGroups().get(0).getName());

        KeePassFileSerialization kfs = new KeePassFileSerializationBuilder(keePassFile).build();

        System.err.println("TestController /  kfs: " + kfs.getGroups().get(0).getEntries().get(0).getTitle() );

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
        System.err.println("TestController / inMemoryKeePassModel.getEmail():" + inMemoryKeePassModel.getEmail());
    }

}
