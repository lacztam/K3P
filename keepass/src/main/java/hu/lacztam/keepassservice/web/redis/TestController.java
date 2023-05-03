package hu.lacztam.keepassservice.web.redis;


import de.slackspace.openkeepass.domain.Group;
import hu.lacztam.keepassservice.config.ModelType;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import hu.lacztam.keepassservice.repository.redis.FrontendKeePassRepository;
import hu.lacztam.keepassservice.service.redis.InMemoryKeePassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/frontend")
public class TestController {

    @Autowired
    InMemoryKeePassService inMemoryKeePassService;
    @Autowired
    FrontendKeePassRepository frontendKeePassRepository;

    @PostMapping
    public void saveContent() {
        byte[] pgba = new byte[0];
        try {
            pgba = Files.readAllBytes(Path.of("/home/tamas/Documents/pg.jpg"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        InMemoryKeePassModel inMemoryKeePassModel = new InMemoryKeePassModel();
//        keePassModel.setId("1");
        inMemoryKeePassModel.setKdbxFile(pgba);
        inMemoryKeePassService.save(inMemoryKeePassModel);
    }

    @GetMapping("/main")
    public Group getMainKdbxFileTopGroup(HttpServletRequest request) {
        InMemoryKeePassModel inMemoryKeePassModel
                = inMemoryKeePassService.getKeePassFile(request, ModelType.MAIN_KEEPASS);

        return inMemoryKeePassModel.getKeePassFile(inMemoryKeePassModel.getPassword()).getGroups().get(0);
    }

    @GetMapping("/{id}")
    public void writeFromRedis(@PathVariable String id) {
//        KeePassModel keePassModel = redisDao.getOneKeePassFile(id);
        Iterable<InMemoryKeePassModel> all = frontendKeePassRepository.findAll();
        InMemoryKeePassModel inMemoryKeePassModel = all.iterator().next();
        byte[] picture = inMemoryKeePassModel.getKdbxFile();
        try {
            Files.write(Path.of("/home/tamas/Documents/from_redis_demo2.jpg"), picture);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
