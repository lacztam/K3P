package hu.lacztam.keepassservice.service.redis;

import de.slackspace.openkeepass.domain.*;
import de.slackspace.openkeepass.domain.zipper.GroupZipper;
import hu.lacztam.keepassservice.config.ModelType;
import hu.lacztam.keepassservice.dto.GroupDto;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import hu.lacztam.keepassservice.service.MakeKdbxByteService;
import hu.lacztam.token.UserDetailsFromJwtToken;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.Map;

@Repository
@AllArgsConstructor
public class InMemoryKeePassService {

    private final String HASH_KEY = "KeePassFile";
    private final UserDetailsFromJwtToken userDetailsFromJwtToken;
    private final MakeKdbxByteService makeKdbxByteService;

//    @Resource(name="redisTemplate")          // 'redisTemplate' is defined as a Bean in AppConfig.java
//    private HashOperations<String, String, KeePassModel> hashOperations;
    private RedisTemplate redisTemplate;

    public InMemoryKeePassModel save(InMemoryKeePassModel inMemoryKeePassModel) {
        //creates one record in Redis DB if record with that Id is not present
//        hashOperations.putIfAbsent(hashReference, keePassModel.getId(), keePassModel);
        redisTemplate.opsForHash().putIfAbsent(HASH_KEY, inMemoryKeePassModel.getId(), inMemoryKeePassModel);
        return inMemoryKeePassModel;
    }

    public InMemoryKeePassModel getKeePassFile(String email) {
        String redisId = email + ModelType.MAIN_KEEPASS;
        return (InMemoryKeePassModel) redisTemplate.opsForHash().get(HASH_KEY, redisId);
    }

    public InMemoryKeePassModel getKeePassFile(HttpServletRequest request, String keePassType) {
        String email = userDetailsFromJwtToken.getUserDetailsFromJwtToken(request).getUsername();
        String redisId = email + keePassType;
        InMemoryKeePassModel inMemoryKeePassModel
                = (InMemoryKeePassModel) redisTemplate.opsForHash().get(HASH_KEY, redisId);

        if(inMemoryKeePassModel != null)
            return inMemoryKeePassModel;
        else
            throw new NullPointerException("Can not invoke KeePass model from memory.");
    }

    public void delete(String id) {
        redisTemplate.opsForHash().delete(HASH_KEY, id);
    }

    public InMemoryKeePassModel update(InMemoryKeePassModel inMemoryKeePassModel) {
        redisTemplate.opsForHash().put(HASH_KEY, inMemoryKeePassModel.getId(), inMemoryKeePassModel);
        return inMemoryKeePassModel;
    }

    public Map<String, InMemoryKeePassModel> getAllKeePassModel() {
        return redisTemplate.opsForHash().entries(HASH_KEY);
    }

    @Transactional
    public InMemoryKeePassModel uploadModifiedKdbxFile(
            HttpServletRequest request,
            Group modifiedGroup,
            String keepassType) {
        InMemoryKeePassModel inMemoryKeePassModel = getKeePassFile(request, keepassType);

        if (inMemoryKeePassModel != null) {

            KeePassFile originalKeePassFile = inMemoryKeePassModel.getKeePassFile(inMemoryKeePassModel.getPassword());

            KeePassFile modifiedKeePass =
                    new KeePassFileBuilder(originalKeePassFile.getMeta())
                            .addTopGroups(modifiedGroup)
                            .build();

            byte[] keePassFileInBytes
                    = makeKdbxByteService.makeKdbx(modifiedKeePass, inMemoryKeePassModel.getPassword());

            inMemoryKeePassModel.setKdbxFile(keePassFileInBytes);
            inMemoryKeePassModel = update(inMemoryKeePassModel);

            return inMemoryKeePassModel;
        } else {
            throw new NullPointerException("K3PModel can not be null.");
        }
    }

    // TO-DO: cant modify expire time
    @Transactional
    public InMemoryKeePassModel uploadModifiedKdbxFile_modifyGroup(
            InMemoryKeePassModel inMemoryKeePassModel,
            GroupDto groupDto,
            Group originalGroup) {

        if (inMemoryKeePassModel == null)
            throw new NullPointerException("In memory keepass model can not be null.");

        KeePassFile keePassFile = inMemoryKeePassModel.getKeePassFile(inMemoryKeePassModel.getPassword());

        TimesBuilder timesBuilder = null;
        Group modifiedGroup = null;

        modifiedGroup = new GroupBuilder(originalGroup)
                        .name(groupDto.getGroupNameDto())
                        .times(setTimeFromOriginalGroup(originalGroup, groupDto))
                        .addEntries(originalGroup.getEntries())
                        .isExpanded(groupDto.isExpandedDto())
                        .build();

        System.err.println("modifiedGroup.getTimes(): " + modifiedGroup.getTimes());
        System.err.println("modifiedGroup.isExpanded(): " + modifiedGroup.isExpanded());

        KeePassFile modifiedKeePassFile
                = modifyGroup(  keePassFile,
                                groupDto.getTargetGroupDirectionDto(),
                                modifiedGroup);

        byte[] keePassFileInBytes = makeKdbxByteService.makeKdbx(modifiedKeePassFile, inMemoryKeePassModel.getPassword());
        inMemoryKeePassModel.setKdbxFile(keePassFileInBytes);
        inMemoryKeePassModel = update(inMemoryKeePassModel);

        return inMemoryKeePassModel;
    }

    private Times setTimeFromOriginalGroup(Group originalGroup, GroupDto groupDto){
        TimesBuilder timesBuilder = null;
        System.err.println("setTimeFromOriginalGroup:\n");



        if (originalGroup.getTimes() != null) {
            timesBuilder = new TimesBuilder(originalGroup.getTimes());

            System.err.println("timesBuilder.getCreationTime():" + timesBuilder.getCreationTime());
            System.err.println("timesBuilder.getExpiryTime():" + timesBuilder.getExpiryTime());

            if (groupDto.isExpiresDto()) {
                timesBuilder.expires(groupDto.isExpiresDto());
                timesBuilder.expiryTime(groupDto.getModifyGroupExpiryTimeDto());

                System.err.println("original grp get times != null\ngroupDto.getGroupExpiryTimeDto():" + groupDto.getModifyGroupExpiryTimeDto());
                System.err.println("original grp get times != null\ntimesBuilder.getExpiryTime(): " + timesBuilder.getExpiryTime());
                return timesBuilder.build();
            }
        }else if(groupDto.isExpiresDto()){
            timesBuilder = new TimesBuilder();
            timesBuilder.expires(groupDto.isExpiresDto());
            timesBuilder.expiryTime(groupDto.getModifyGroupExpiryTimeDto());

            System.err.println("groupDto.getGroupExpiryTimeDto():" + groupDto.getModifyGroupExpiryTimeDto());
            System.err.println("timesBuilder.getExpiryTime(): " + timesBuilder.getExpiryTime());

            return timesBuilder.build();
        }
        return null;
    }


    private KeePassFile modifyGroup(KeePassFile keePassFile, String targetDirection, Group editGroup) {
        GroupZipper groupZipper = new GroupZipper(keePassFile);

        for (int i = 0; i < targetDirection.length(); i++) {
            if (targetDirection.charAt(i) == 'D') {
                groupZipper = groupZipper.down();
            } else if (targetDirection.charAt(i) == 'R') {
                groupZipper = groupZipper.right();
            } else {
                throw new NullPointerException("Error invalid target direction: " + targetDirection.charAt(i));
            }
        }

        groupZipper = groupZipper.replace(editGroup);
        KeePassFile modifiedKeePassFile = groupZipper.close();

        return modifiedKeePassFile;
    }

}