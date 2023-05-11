package hu.lacztam.keepassservice.service.postgres;

import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.GroupBuilder;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.domain.zipper.GroupZipper;
import hu.lacztam.keepassservice.dto.ModifyGroupDto;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;

// Don't use lombok, application context will form a cycle
@Service
public class GroupService {

    @Autowired
    KeePassService keePassService;
    @Lazy @Autowired
    EntryService entryService;

    public Group getTopGroupWithoutPassword(KeePassModel keePassModel, String password) {
        if (keePassModel != null) {
            KeePassFile keePassFile = keePassModel.getKeePassFile(password);
            Group group = mapTopGroupWithoutPassword(keePassFile.getRoot().getGroups().get(0));

            return group;
        } else {
            throw new NullPointerException("K3PModel can not be null.");
        }
    }

    public Group mapTopGroupWithoutAttachment(Group group) {
        if (group != null) {
            List<Entry> mappedEntries = entryService.mapEntriesWithoutAttachments(group.getEntries());

            GroupBuilder groupBuilder = new GroupBuilder(group.getName()).addEntries(mappedEntries);

            for (Group g : group.getGroups()) {
                Group newGrp = mapTopGroupWithoutAttachment(g);
                groupBuilder.addGroup(newGrp);
            }

            return groupBuilder.build();

        } else {
            throw new NullPointerException("Group can not be null.");
        }

    }

    private Group mapTopGroupWithoutPassword(Group group) {
        if (group != null) {
            List<Entry> mappedEntries = entryService.mapEntriesWithoutPassword(group.getEntries());

            GroupBuilder groupBuilder = new GroupBuilder(group.getName()).addEntries(mappedEntries);

            for (Group g : group.getGroups()) {
                Group newGrp = mapTopGroupWithoutAttachment(g);
                groupBuilder.addGroup(newGrp);
            }

            return groupBuilder.build();

        } else {
            throw new NullPointerException("Group can not be null.");
        }

    }

    @Transactional
    public Group getGroupNodeFromDirection(KeePassFile keePassFile, String targetDirection) {
        if (keePassFile != null) {
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
        } else {
            throw new NullPointerException("KeePassFile can not be null.");
        }
    }

    @Transactional
    public Group removeGroupAndBuildGroup(Group group, Group targetGroup) {
        if (group != null && targetGroup != null) {
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
        } else {
            throw new NullPointerException("Group can not be null.");
        }
    }

    //TO-DO: missing entries from target group
    @Transactional
    public Group moveExistingGroupToAnotherGroupAndBuildGroup(Group group, Group sourceGroup, Group targetGroup) {
        if (group != null && sourceGroup != null && targetGroup != null) {
            GroupBuilder groupBuilder = new GroupBuilder(group.getName()).addEntries(group.getEntries());

            if (group.equals(targetGroup)) {
                List<Entry> sourceEntries = sourceGroup.getEntries();
                GroupBuilder sourceGroupBuilder = new GroupBuilder().name(sourceGroup.getName()).addEntries(sourceEntries);
                groupBuilder.addGroup(sourceGroupBuilder.build());
            }

            for (Group g : group.getGroups()) {
                if (g.equals(sourceGroup))
                    continue;

                Group newGrp = moveExistingGroupToAnotherGroupAndBuildGroup(g, sourceGroup, targetGroup);
                groupBuilder.addGroup(newGrp);
            }

            return groupBuilder.build();
        } else {
            throw new NullPointerException("Group can not be null.");
        }
    }

    @Transactional
    public Group addNewGroupAndBuildGroup(Group group, Group newGroup, Group targetGroup) {
        if (group != null && newGroup != null && targetGroup != null) {
            GroupBuilder groupBuilder = new GroupBuilder(group.getName()).addEntries(group.getEntries());

            if (group.equals(targetGroup)) {
                GroupBuilder sourceGroupBuilder
                        = new GroupBuilder().name(newGroup.getName());

                groupBuilder.addGroup(sourceGroupBuilder.build());
            }

            for (Group g : group.getGroups()) {
                Group newGrp = moveExistingGroupToAnotherGroupAndBuildGroup(g, newGroup, targetGroup);
                groupBuilder.addGroup(newGrp);
            }

            return groupBuilder.build();
        } else {
            throw new NullPointerException("Group can not be null.");
        }
    }

    @Transactional
    public Group addNewGroupAndBuildGroup(ModifyGroupDto modifyGroupDto, HttpServletRequest request) {
        KeePassModel keePassModel = keePassService.findByIDWithUserCheck(modifyGroupDto.getKdbxFileDto().getKdbxFileIdDto(), request);
        KeePassFile keePassFile = keePassModel.getKeePassFile(modifyGroupDto.getKdbxFileDto().getKdbxFilePwDto());

        String targetGroupDirection = modifyGroupDto.getGroupDto().getTargetGroupDirectionDto();

        Group targetGroup = getGroupNodeFromDirection(keePassFile, targetGroupDirection);
        GroupBuilder groupBuilder = new GroupBuilder(modifyGroupDto.getGroupDto().getGroupNameDto());
        Group newGrp = groupBuilder.build();
        Group modifiedGroup = addNewGroupAndBuildGroup(keePassFile.getRoot().getGroups().get(0), newGrp, targetGroup);

        return modifiedGroup;
    }

    @Transactional
    public KeePassModel addNewGroupToKeePassFile(ModifyGroupDto modifyGroupDto, HttpServletRequest request) {
        Group addedGroup = addNewGroupAndBuildGroup(modifyGroupDto, request);

        KeePassModel keePassModel = keePassService.uploadModifiedKdbxFile(modifyGroupDto, addedGroup, request);

        return keePassModel;
    }

    @Transactional
    public KeePassModel modifyGroupInKeePassFile(ModifyGroupDto modifyGroupDto, HttpServletRequest request) {

        KeePassModel keePassModel = keePassService.findByIDWithUserCheck(modifyGroupDto.getKdbxFileDto().getKdbxFileIdDto(), request);
        KeePassFile keePassFile = keePassModel.getKeePassFile(modifyGroupDto.getKdbxFileDto().getKdbxFilePwDto());

        Group originalGroup
                = getGroupNodeFromDirection(keePassFile, modifyGroupDto.getGroupDto().getTargetGroupDirectionDto());

        keePassModel = keePassService.uploadModifiedKdbxFile_modifyGroup(keePassModel, modifyGroupDto, originalGroup);

        return keePassModel;
    }

    @Transactional
    public KeePassModel moveExistingGroupToAnotherGroupInKeePassFile(ModifyGroupDto modifyGroupDto, HttpServletRequest request) {
        KeePassModel keePassModel = keePassService.findByIDWithUserCheck(modifyGroupDto.getKdbxFileDto().getKdbxFileIdDto(), request);
        KeePassFile keePassFile = keePassModel.getKeePassFile(modifyGroupDto.getKdbxFileDto().getKdbxFilePwDto());

        Group targetGroup
                = getGroupNodeFromDirection(keePassFile, modifyGroupDto.getGroupDto().getTargetGroupDirectionDto());
        Group sourceGroup
                = getGroupNodeFromDirection(keePassFile, modifyGroupDto.getGroupDto().getSourceGroupDirectionDto());
        Group modifiedGroup
                = moveExistingGroupToAnotherGroupAndBuildGroup(keePassFile.getRoot().getGroups().get(0), sourceGroup, targetGroup);

        keePassModel = keePassService.uploadModifiedKdbxFile(keePassModel, modifyGroupDto, modifiedGroup);

        return keePassModel;
    }

    @Transactional
    public KeePassModel deleteGroupFromKeePassFile(ModifyGroupDto modifyGroupDto, HttpServletRequest request) {
        KeePassModel keePassModel = keePassService.findByIDWithUserCheck(modifyGroupDto.getKdbxFileDto().getKdbxFileIdDto(), request);
        KeePassFile keePassFile = keePassModel.getKeePassFile(modifyGroupDto.getKdbxFileDto().getKdbxFilePwDto());

        Group targetGroup
                = getGroupNodeFromDirection(keePassFile, modifyGroupDto.getGroupDto().getTargetGroupDirectionDto());

        Group modifiedGroup
                = removeGroupAndBuildGroup(keePassFile.getRoot().getGroups().get(0), targetGroup);

        keePassModel = keePassService.uploadModifiedKdbxFile(keePassModel, modifyGroupDto, modifiedGroup);

        return keePassModel;
    }

}
