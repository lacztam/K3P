package hu.lacztam.keepassservice.service.redis;

import de.slackspace.openkeepass.domain.*;
import de.slackspace.openkeepass.domain.zipper.GroupZipper;
import hu.lacztam.keepassservice.config.ModelType;
import hu.lacztam.keepassservice.dto.GroupDto;
import hu.lacztam.keepassservice.dto.KdbxFileDto;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import hu.lacztam.keepassservice.service.EntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;

// Don't use lombok, application context will form a cycle
@Service
public class InMemoryGroupService {

//    @Autowired KeePassService keePassService;
    @Lazy @Autowired EntryService entryService;
    @Autowired InMemoryKeePassService inMemoryKeePassService;


    @Transactional
    public Group getTopGroupWithoutPassword(InMemoryKeePassModel keePassModel) {
        if (keePassModel == null)
            throw new NullPointerException("K3PModel can not be null.");

        KeePassFile keePassFile = keePassModel.getKeePassFile(keePassModel.getPassword());
        Group group = mapTopGroupWithoutPassword(keePassFile.getRoot().getGroups().get(0));

        return group;
    }

    @Transactional
    public Group mapTopGroupWithoutAttachment(Group group) {
        if (group == null)
            throw new NullPointerException("Group can not be null.");

        List<Entry> mappedEntries = entryService.mapEntriesWithoutAttachments(group.getEntries());

        GroupBuilder groupBuilder = new GroupBuilder(group.getName()).addEntries(mappedEntries);

        for (Group g : group.getGroups()) {
            Group newGrp = mapTopGroupWithoutAttachment(g);
            groupBuilder.addGroup(newGrp);
        }

        return groupBuilder.build();
    }

    private Group mapTopGroupWithoutPassword(Group group) {
        if (group == null)
            throw new NullPointerException("Group can not be null.");

        List<Entry> mappedEntries = entryService.mapEntriesWithoutPassword(group.getEntries());

        GroupBuilder groupBuilder = new GroupBuilder(group.getName()).addEntries(mappedEntries);
        for (Group g : group.getGroups()) {
            Group newGrp = mapTopGroupWithoutAttachment(g);
            groupBuilder.addGroup(newGrp);
        }

        return groupBuilder.build();
    }

    @Transactional
    public Group getGroupNodeFromDirection(KeePassFile keePassFile, String targetDirection) {
        if (keePassFile == null)
            throw new NullPointerException("KeePassFile can not be null.");

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
        Group targetGroup = groupZipper.getNode();

        return targetGroup;
    }

    @Transactional
    public Group removeGroupAndBuildGroup(Group group, Group targetGroup) {
        if (group == null || targetGroup == null)
            throw new NullPointerException("Group can not be null.");

        GroupBuilder groupBuilder = null;
        if (group.equals(targetGroup)) {
            groupBuilder = new GroupBuilder(group.getName());
        } else {
            groupBuilder = new GroupBuilder(group.getName()).addEntries(group.getEntries());
        }

        for (Group g : group.getGroups()) {
            if (g.equals(targetGroup)) {
                continue;
            }
            Group newGrp = removeGroupAndBuildGroup(g, targetGroup);
            groupBuilder.addGroup(newGrp);
        }

        return groupBuilder.build();
    }

    //TO-DO: missing entries from target group
    @Transactional
    public Group moveGroupToAnotherGroupAndBuildGroup(Group group, Group sourceGroup, Group targetGroup) {
        if (group == null || sourceGroup == null || targetGroup == null)
            throw new NullPointerException("Group can not be null.");

        GroupBuilder groupBuilder = new GroupBuilder(group.getName()).addEntries(group.getEntries());

        if (group.equals(targetGroup)) {
            List<Entry> sourceEntries = sourceGroup.getEntries();
            GroupBuilder sourceGroupBuilder = new GroupBuilder().name(sourceGroup.getName()).addEntries(sourceEntries);
            groupBuilder.addGroup(sourceGroupBuilder.build());
        }

        for (Group g : group.getGroups()) {
            if (g.equals(sourceGroup))
                continue;

            Group newGrp = moveGroupToAnotherGroupAndBuildGroup(g, sourceGroup, targetGroup);
            groupBuilder.addGroup(newGrp);
        }

        return groupBuilder.build();
    }

    @Transactional
    public Group addNewGroupAndBuildGroup(Group group, Group newGroup, Group targetGroup) {
        if (group == null || newGroup == null || targetGroup == null)
            throw new NullPointerException("Group can not be null.");

        GroupBuilder groupBuilder = new GroupBuilder(group.getName()).addEntries(group.getEntries());

        if (group.equals(targetGroup)) {
            GroupBuilder sourceGroupBuilder
                    = new GroupBuilder().name(newGroup.getName());

            groupBuilder.addGroup(sourceGroupBuilder.build());
        }

        for (Group g : group.getGroups()) {
            Group newGrp = moveGroupToAnotherGroupAndBuildGroup(g, newGroup, targetGroup);
            groupBuilder.addGroup(newGrp);
        }

        return groupBuilder.build();
    }

    @Transactional
    public Group addNewGroupAndBuildGroup(GroupDto groupDto, HttpServletRequest request, String keePassType) {
        InMemoryKeePassModel inMemoryKeePassModel = inMemoryKeePassService.getKeePassFile(request, keePassType);
        KeePassFile keePassFile = inMemoryKeePassModel.getKeePassFile(inMemoryKeePassModel.getPassword());

        String targetGroupDirection = groupDto.getTargetGroupDirectionDto();

        Group targetGroup = getGroupNodeFromDirection(keePassFile, targetGroupDirection);
        TimesBuilder timesBuilder = new TimesBuilder();
        timesBuilder.creationTime(groupDto.getCreationTimeDto());

        if(groupDto.isExpiresDto()){
            timesBuilder.expires(groupDto.isExpiresDto());
            timesBuilder.expiryTime(groupDto.getExpireTimeDto());
        }

        GroupBuilder groupBuilder
                = new GroupBuilder(groupDto.getGroupNameDto())
                        .times(timesBuilder.build());

        Group newGrp = groupBuilder.build();
        Group modifiedGroup = addNewGroupAndBuildGroup(keePassFile.getRoot().getGroups().get(0), newGrp, targetGroup);

        return modifiedGroup;
    }

    @Transactional
    public InMemoryKeePassModel addNewGroupToKeePassFile(HttpServletRequest request, GroupDto groupDto, String keePassType) {
        Group addedGroup = addNewGroupAndBuildGroup(groupDto, request, keePassType);

        InMemoryKeePassModel inMemoryKeePassModel
                = inMemoryKeePassService.uploadModifiedKdbxFile(request, addedGroup, keePassType);

        return inMemoryKeePassModel;
    }

    @Transactional
    public InMemoryKeePassModel editGroupNameOrExpireTime(
            GroupDto groupDto,
            HttpServletRequest request,
            String keePassType) {

        InMemoryKeePassModel inMemoryKeePassModel = inMemoryKeePassService.getKeePassFile(request, keePassType);
        KeePassFile keePassFile = inMemoryKeePassModel.getKeePassFile(inMemoryKeePassModel.getPassword());

        Group originalGroup
                = getGroupNodeFromDirection(keePassFile, groupDto.getTargetGroupDirectionDto());

        inMemoryKeePassModel = inMemoryKeePassService.uploadModifiedKdbxFile_modifyGroup(inMemoryKeePassModel, groupDto, originalGroup);

        return inMemoryKeePassModel;
    }

    @Transactional
    public InMemoryKeePassModel moveGroupToAnotherGroup(
            GroupDto groupDto,
            HttpServletRequest request,
            String modelType) {

        InMemoryKeePassModel inMemoryKeePassModel = inMemoryKeePassService.getKeePassFile(request, modelType);
        KeePassFile keePassFile = inMemoryKeePassModel.getKeePassFile(inMemoryKeePassModel.getPassword());

        Group targetGroup
                = getGroupNodeFromDirection(keePassFile, groupDto.getTargetGroupDirectionDto());
        Group sourceGroup
                = getGroupNodeFromDirection(keePassFile, groupDto.getSourceGroupDirectionDto());
        Group modifiedGroup
                = moveGroupToAnotherGroupAndBuildGroup(keePassFile.getRoot().getGroups().get(0), sourceGroup, targetGroup);

        inMemoryKeePassModel = inMemoryKeePassService.uploadModifiedKdbxFile(request, modifiedGroup, modelType);

        return inMemoryKeePassModel;
    }

    @Transactional
    public InMemoryKeePassModel deleteGroup(
            GroupDto groupDto,
            HttpServletRequest request,
            String keePassType) {

        InMemoryKeePassModel inMemoryKeePassModel = inMemoryKeePassService.getKeePassFile(request, keePassType);
        KeePassFile keePassFile = inMemoryKeePassModel.getKeePassFile(inMemoryKeePassModel.getPassword());

        Group targetGroup
                = getGroupNodeFromDirection(keePassFile, groupDto.getTargetGroupDirectionDto());

        System.err.println("Group name: " + targetGroup.toString());

        Group modifiedGroup
                = removeGroupAndBuildGroup(keePassFile.getRoot().getGroups().get(0), targetGroup);

        inMemoryKeePassModel = inMemoryKeePassService.uploadModifiedKdbxFile(request, modifiedGroup, ModelType.MAIN_KEEPASS);

        return inMemoryKeePassModel;
    }

//    @Transactional
//    public boolean modifyKeePassFileDatabaseName(KdbxFileDto kdbxFileDto){
//        if(kdbxFileDto.getKeePassDatabaseNameDto() == null)
//            return false;
//
//        boolean validateFileName = checkIfFileNameIsValid(kdbxFileDto.getKeePassDatabaseNameDto());
//
//        if(validateFileName){
//            KeePassModel keePassModel = findByID(kdbxFileDto.getKdbxFileIdDto());
//            KeePassFile keePassFile = keePassModel.getKeePassFile(kdbxFileDto.getKdbxFilePwDto());
//
//            Meta metaWithNewDbName = new MetaBuilder(keePassFile.getMeta())
//                    .databaseName(kdbxFileDto.getKeePassDatabaseNameDto())
//                    .build();
//
//            Group root = groupServiceOLD.mapTopGroupWithoutAttachment(keePassFile.getRoot().getGroups().get(0));
//            KeePassFile uploadKeePass = new KeePassFileBuilder(metaWithNewDbName).addTopGroups(root).build();
//
//            byte[] kdbxInBytes = makeKdbxByteService.makeKdbx(uploadKeePass, kdbxFileDto.getKdbxFilePwDto());
//
//            keePassModel.setKdbxFile(kdbxInBytes);
//            keePassModel = save(keePassModel);
//
//            return true;
//        }else{
//            return false;
//        }
//    }


}
