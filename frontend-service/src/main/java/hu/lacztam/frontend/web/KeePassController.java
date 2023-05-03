package hu.lacztam.frontend.web;


import hu.lacztam.frontend.model.FrontEndKeePassModel;
import hu.lacztam.frontend.repository.KeePassRepository;
import hu.lacztam.frontend.service.KeePassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/frontend")
public class KeePassController {

    @Autowired
    KeePassService keePassService;
    @Autowired
    KeePassRepository keePassRepository;

    @PostMapping
    public void saveContent() {
        byte[] pgba = new byte[0];
        try {
            pgba = Files.readAllBytes(Path.of("/home/tamas/Documents/pg.jpg"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FrontEndKeePassModel frontEndKeePassModel = new FrontEndKeePassModel();
//        keePassModel.setId("1");
        frontEndKeePassModel.setKeePassFile(pgba);
        keePassService.saveKeePassModel(frontEndKeePassModel);
    }

    @GetMapping("/kp/{id}")
    public String getKdbxFileTopGroup(@PathVariable String id) {
        FrontEndKeePassModel frontEndKeePassModel = keePassService.getOneKeePassFile(id);

        return frontEndKeePassModel.getKeePassFile(frontEndKeePassModel.getPassword()).getEntries().toString();
    }

    @GetMapping("/{id}")
    public void writeFromRedis(@PathVariable String id) {
//        KeePassModel keePassModel = redisDao.getOneKeePassFile(id);
        Iterable<FrontEndKeePassModel> all = keePassRepository.findAll();
        FrontEndKeePassModel frontEndKeePassModel = all.iterator().next();
        byte[] picture = frontEndKeePassModel.getKeePassFile();
        try {
            Files.write(Path.of("/home/tamas/Documents/from_redis_demo2.jpg"), picture);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
