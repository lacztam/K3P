package hu.lacztam.keepassservice.jms.redis;

import hu.lacztam.keepassservice.mapper.InMemoryKeePassModelMapper;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import hu.lacztam.keepassservice.service.MakeKdbxByteService;
import hu.lacztam.keepassservice.service.jms.LoadKeePassDataToMemoryConsumerService;
import hu.lacztam.keepassservice.service.postgres.KeePassService;
import hu.lacztam.keepassservice.service.redis.InMemoryKeePassService;
import hu.lacztam.model_lib.keepass_s_crypto_s.UserMailAndKeePassPw;
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
//    private final KeePassModelMapper keePassModelMapper;
    private final InMemoryKeePassModelMapper inMemoryKeePassModelMapper;
    private final MakeKdbxByteService makeKdbxByteService;
    private final LoadKeePassDataToMemoryConsumerService loadKeePassDataToMemoryConsumerService;


    @JmsListener(destination = IN_MEMORY_KEEPASS_TIMER_HAS_EXPIRED)
    public void inMemoryKeePassTimerHasExpired(String email){
        System.err.println("in memory keepass timer has expired: " + email);

        InMemoryKeePassModel inMemoryMainKeePassModel = inMemoryKeePassService.getKeePassModel(email);
        KeePassModel originalMainKeePass = keePassService.findMainKeePassByUserEmail(email);

        if(inMemoryMainKeePassModel != null){
            boolean isMainKeePassFileTheSame = compareHash(inMemoryMainKeePassModel, originalMainKeePass);
            if(isMainKeePassFileTheSame)
                inMemoryKeePassService.delete(inMemoryMainKeePassModel.getId());
            else
                saveMainChanges(inMemoryMainKeePassModel);
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

    private boolean compareHash(InMemoryKeePassModel inMemoryKeePassModel, KeePassModel keePassModel){
        UserMailAndKeePassPw userMailAndKeePassPw
                = new UserMailAndKeePassPw(inMemoryKeePassModel.getEmail(), keePassModel.getKdbxFilePassword());

        String decryptedPassword = loadKeePassDataToMemoryConsumerService.decryptPassword(userMailAndKeePassPw);

        byte [] inMemoryKdbxFile = makeKdbxByteService.makeKdbx(inMemoryKeePassModel.getKeePassFile(), decryptedPassword);

        int hash1 = createHashFromKeePassFile(inMemoryKdbxFile);
        int hash2 = createHashFromKeePassFile(keePassModel.getKdbxFile());

        return hash1 == hash2;
    }

    private void saveMainChanges(InMemoryKeePassModel inMemoryMainModel){
        KeePassModel main = inMemoryKeePassModelMapper.inMemoryModelToKeePass(inMemoryMainModel);

        keePassService.save(main);

        inMemoryKeePassService.delete(inMemoryMainModel.getId());
    }

    private void saveSharedChanges(InMemoryKeePassModel inMemorySharedModel){
        KeePassModel shared = inMemoryKeePassModelMapper.inMemoryModelToKeePass(inMemorySharedModel);

        keePassService.save(shared);

        inMemoryKeePassService.delete(inMemorySharedModel.getId());
    }

}