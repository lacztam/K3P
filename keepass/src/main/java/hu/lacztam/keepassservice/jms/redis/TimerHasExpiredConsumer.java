package hu.lacztam.keepassservice.jms.redis;

import hu.lacztam.keepassservice.mapper.KeePassModelMapper;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import hu.lacztam.keepassservice.service.postgres.KeePassService;
import hu.lacztam.keepassservice.service.redis.InMemoryKeePassService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import static hu.lacztam.keepassservice.config.JmsConfig.IN_MEMORY_KEEPASS_TIMER_HAS_EXPIRED;

@Component
@AllArgsConstructor
public class TimerHasExpiredConsumer {

    private final InMemoryKeePassService inMemoryKeePassService;
    private final KeePassService keePassService;
    private final KeePassModelMapper keePassModelMapper;

    @JmsListener(destination = IN_MEMORY_KEEPASS_TIMER_HAS_EXPIRED)
    public void inMemoryKeePassTimerHasExpired(String email){
        System.err.println("in memory keepass timer has expired: " + email);

        InMemoryKeePassModel inMemoryMainKeePassModel = inMemoryKeePassService.getKeePassFile(email);
        KeePassModel originalMainKeePass = keePassService.findMainKeePassByUserEmail(email);

        if(inMemoryMainKeePassModel != null){
            boolean isMainKeePassFileTheSame = compareHash(inMemoryMainKeePassModel, originalMainKeePass);
            if(isMainKeePassFileTheSame)
                inMemoryKeePassService.delete(inMemoryMainKeePassModel.getId());
            else
                saveMainChanges(inMemoryMainKeePassModel, originalMainKeePass);
        }

        //        InMemoryKeePassModel inMemorySharedKeePassModel = inMemoryKeePassService.getSharedKeePassFile(email);

//        if(inMemoryMainKeePassModel != null && inMemorySharedKeePassModel != null){
//            KeePassModel originalMainKeePass = keePassService.findMainKeePassByUserEmail(email);
//            KeePassModel originalSharedKeePass = keePassService.findSharedKeePassByUserEmail(email);
//
//            boolean isMainKeePassFileTheSame = compareHash(inMemoryMainKeePassModel, originalMainKeePass);
//            boolean isSharedKeePassFileTheSame = compareHash(inMemorySharedKeePassModel, originalSharedKeePass);
//
//            if(isMainKeePassFileTheSame && isSharedKeePassFileTheSame){
//                inMemoryKeePassService.delete(inMemoryMainKeePassModel.getId());
//                inMemoryKeePassService.delete(inMemorySharedKeePassModel.getId());
//            }
//
//            if(!isMainKeePassFileTheSame)
//                saveMainChanges(inMemoryMainKeePassModel, originalMainKeePass);
//
//            if(!isSharedKeePassFileTheSame)
//                saveSharedChanges(inMemorySharedKeePassModel, originalSharedKeePass);
//        }

    }

    private int createHashFromKeePassFile(byte[] kdbxFile) {
        return new HashCodeBuilder(17, 31)
                .append(kdbxFile)
                .toHashCode();
    }

    private boolean compareHash(InMemoryKeePassModel inMemoryModel, KeePassModel keePassModel){
        int hash1 = createHashFromKeePassFile(inMemoryModel.getKdbxFile());
        int hash2 = createHashFromKeePassFile(keePassModel.getKdbxFile());

        return hash1 == hash2 ? true : false;
    }


    private void saveMainChanges(InMemoryKeePassModel inMemoryMainModel, KeePassModel originalModel){
        KeePassModel main = keePassModelMapper.inMemoryModelToKeePass(inMemoryMainModel);
        main.setKdbxFilePassword(originalModel.getKdbxFilePassword());
        main.setCreated(originalModel.getCreated());
        keePassService.save(main);

        inMemoryKeePassService.delete(inMemoryMainModel.getId());
    }

    private void saveSharedChanges(InMemoryKeePassModel inMemorySharedKeePassModel, KeePassModel originalSharedKeePass){
        KeePassModel keePassModel = keePassModelMapper.inMemoryModelToKeePass(inMemorySharedKeePassModel);
        keePassModel.setKdbxFilePassword(originalSharedKeePass.getKdbxFilePassword());
        keePassModel.setCreated(originalSharedKeePass.getCreated());
        keePassService.save(keePassModel);

        inMemoryKeePassService.delete(inMemorySharedKeePassModel.getId());
    }

}