package hu.lacztam.keepassservice.service.redis;

import de.slackspace.openkeepass.domain.*;
import hu.lacztam.keepassservice.dto.EntryDto;
import hu.lacztam.keepassservice.dto.ModifyEntryDto;
import hu.lacztam.keepassservice.mapper.EntryMapper;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.model.redis.KeePassFileSerialization;
import hu.lacztam.keepassservice.model.redis.KeePassFileSerializationBuilder;
import hu.lacztam.keepassservice.service.postgres.GroupService;
import hu.lacztam.keepassservice.service.postgres.KeePassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.*;

@Service
public class InMemoryEntryService {

    // Don't use lombok, application context will form a cycle
    @Autowired KeePassService keePassService;
    @Autowired GroupService groupServiceOLD;
    @Autowired EntryMapper entryMapper;

    //TO-DO: authentication
    public Entry getEntryByUUID(KeePassModel keePassModel, String password, UUID uuid) {
        KeePassFile keePassFile = keePassModel.getKeePassFile(password);

        Optional<Entry> optionalEntry = keePassFile.getEntries()
                .stream()
                .filter(e -> e.getUuid().equals(uuid))
                .findFirst();

        if (optionalEntry.isPresent())
            return optionalEntry.get();
        else
            throw new NullPointerException("There is no entry with this uuid:" + uuid.toString());
    }

    @Transactional
    public KeePassModel modifyEntryAndUploadKeePassFile(ModifyEntryDto modifyEntryDto) {
        KeePassModel keePassModel = keePassService.findByID(modifyEntryDto.getKdbxFileDto().getKdbxFileIdDto());
        String kdbxFilePw = modifyEntryDto.getKdbxFileDto().getKdbxFilePwDto();

        KeePassFileSerialization originalKeePassFile = new KeePassFileSerializationBuilder(
            keePassModel.getKeePassFile(kdbxFilePw)
        ).build();

        Group modified = modifyEntryAndBuildGroup(originalKeePassFile, modifyEntryDto.getEntryDto());

        KeePassFile modifiedKeePassFile = new KeePassFileBuilder(originalKeePassFile
                .getMeta())
                .addTopGroups(modified)
                .build();

        byte[] kdbxInBytes
                = keePassService.makeKdbxFileInBytesWithoutAttachments(modifiedKeePassFile, kdbxFilePw);

        keePassModel.setKdbxFile(kdbxInBytes);
        keePassModel = keePassService.save(keePassModel);

        return keePassModel;
    }

    private Group modifyEntryAndBuildGroup(KeePassFile keePassFile, EntryDto entryDto) {
        Entry modifiedEntry = makeEntryFieldModifications(keePassFile, entryDto).build();

        Group modified
                = mapTopGroupWithEntryModification(keePassFile.getRoot().getGroups().get(0), modifiedEntry);

        return modified;
    }

    private EntryBuilder makeEntryFieldModifications(KeePassFile keePassFile, EntryDto entryDto) {
        Entry originalEntry = keePassFile.getEntryByUUID(entryDto.getEntryUuidDto());

        EntryBuilder entryBuilder = new EntryBuilder();

        entryBuilder
                .uuid(originalEntry.getUuid())
                .title(originalEntry.getTitle())
                .username(originalEntry.getUsername())
                .password(originalEntry.getPassword())
                .tags(originalEntry.getTags())
                .notes(originalEntry.getNotes())
                .url(originalEntry.getUrl())
                .times(originalEntry.getTimes())
                .iconId(originalEntry.getIconId())
                .iconData(originalEntry.getIconData())
                .history(originalEntry.getHistory());

        if (!originalEntry.getTitle().equals(entryDto.getEntryTitleDto()))
            entryBuilder.title(entryDto.getEntryTitleDto());

        if (!originalEntry.getUsername().equals(entryDto.getEntryUsernameDto()))
            entryBuilder.username(entryDto.getEntryUsernameDto());

        if (!originalEntry.getPassword().equals(entryDto.getEntryPasswordDto()))
            entryBuilder.password(entryDto.getEntryPasswordDto());

        if (!originalEntry.getNotes().equals(entryDto.getEntryNotesDto()))
            entryBuilder.notes(entryDto.getEntryNotesDto());

        if (!originalEntry.getUrl().equals(entryDto.getEntryUrlDto()))
            entryBuilder.url(entryDto.getEntryUrlDto());

        if (originalEntry.getTags() != null && entryDto.getEntryTagsDto() != null) {
            Collections.sort(originalEntry.getTags());
            List<String> entryDtoTagList = entryDto.stringTagToArray(entryDto.getEntryTagsDto());
            Collections.sort(entryDtoTagList);

            if (!originalEntry.getTags().equals(entryDtoTagList))
                entryBuilder.tags(entryDtoTagList);
        }

        if (entryDto.getExpiresDto()) {
            if (!originalEntry.getTimes().getExpiryTime().equals(entryDto.getEntryExpiryTimeDto())) {
                TimesBuilder timesBuilder = new TimesBuilder(originalEntry.getTimes());
                timesBuilder
                        .expires(entryDto.getExpiresDto())
                        .expiryTime(entryDto.getEntryExpiryTimeDto());
                entryBuilder.times(timesBuilder.build());
            }
        }

        return entryBuilder;
    }

    private Group mapTopGroupWithEntryModification(Group group, Entry modifiedEntry) {
        if (group != null) {
            List<Entry> entryList = group.getEntries();

            for (Entry e : entryList) {
                if (e.getUuid().equals(modifiedEntry.getUuid())) {
                    entryList.remove(e);
                    entryList.add(modifiedEntry);
                }
            }

            GroupBuilder groupBuilder = new GroupBuilder(group.getName()).addEntries(entryList);

            for (Group g : group.getGroups()) {
                Group newGrp = mapTopGroupWithEntryModification(g, modifiedEntry);
                groupBuilder.addGroup(newGrp);
            }

            return groupBuilder.build();
        } else {
            throw new NullPointerException("\nEntryService:mapTopGroupWithEntryModification(): error");
        }

    }

    @Transactional
    public List<Entry> mapEntriesWithoutPassword(List<Entry> entries) {
        List<Entry> entryList = new ArrayList<>();

        for (Entry entry : entries) {
            EntryBuilder entryBuilder = new EntryBuilder();

            entryBuilder
                    .uuid(entry.getUuid())
                    .title(entry.getTitle())
                    .username(entry.getUsername())
                    .tags(entry.getTags())
                    .notes(entry.getNotes())
                    .url(entry.getUrl())
                    .times(entry.getTimes())
                    .iconId(entry.getIconId())
                    .iconData(entry.getIconData())
                    .history(entry.getHistory())
                    .password("");

            entryList.add(entryBuilder.build());
        }

        return entryList;
    }

    @Transactional
    public List<Entry> mapEntriesWithoutAttachments(List<Entry> entries) {
        List<Entry> entryList = new ArrayList<>();

        for (Entry entry : entries) {
            EntryBuilder entryBuilder = new EntryBuilder();

            entryBuilder
                    .uuid(entry.getUuid())
                    .title(entry.getTitle())
                    .username(entry.getUsername())
                    .password(entry.getPassword())
                    .tags(entry.getTags())
                    .notes(entry.getNotes())
                    .url(entry.getUrl())
                    .times(entry.getTimes())
                    .iconId(entry.getIconId())
                    .iconData(entry.getIconData())
                    .history(entry.getHistory());

            entryList.add(entryBuilder.build());
        }

        return entryList;
    }

    //TO-DO: authentication
    @Transactional
    public KeePassModel findK3PModelWithOrWithoutAttachment(long kdbxFileId, HttpServletRequest request) {
        Optional<KeePassModel> optionalK3PModel
                = keePassService.findByIDWithAttachmentsWithUserAuth(kdbxFileId, request);

        KeePassModel keePassModel = null;
        if (optionalK3PModel.isPresent()) {
            keePassModel = optionalK3PModel.get();
        } else {
            keePassModel = keePassService.findByIdNoAttachmentWithUserAuth(kdbxFileId, request);
        }

        return keePassModel;
    }

    @Transactional
    public Group addNewEntryToGroup(Group group, Entry newEntry, Group entryTargetGroup) {
        if (group != null) {

            List<Entry> entries = group.getEntries();

            GroupBuilder groupBuilder = null;
            if (group.equals(entryTargetGroup)) {
                groupBuilder = new GroupBuilder(group.getName()).addEntries(entries).addEntry(newEntry);
            } else {
                groupBuilder = new GroupBuilder(group.getName()).addEntries(entries);
            }

            for (Group g : group.getGroups()) {
                Group newGrp = addNewEntryToGroup(g, newEntry, entryTargetGroup);
                groupBuilder.addGroup(newGrp);
            }

            return groupBuilder.build();
        } else {
            throw new NullPointerException("Group can not be null.");
        }
    }

    //TO-DO: check in postman
    public Group moveEntryToAnotherGroupInKeePassFile(Group group, Entry movingEntry, Group entryTargetGroup) {
        if (group != null) {

            List<Entry> entries = group.getEntries();

            for (Entry e : entries) {
                if (e.getUuid().equals(movingEntry.getUuid())) {
                    entries.remove(e);
                    break;
                }
            }
            GroupBuilder groupBuilder = null;
            if (group.equals(entryTargetGroup)) {
                groupBuilder = new GroupBuilder(group.getName()).addEntries(entries).addEntry(movingEntry);
            } else {
                groupBuilder = new GroupBuilder(group.getName()).addEntries(entries);
            }

            for (Group g : group.getGroups()) {
                Group newGrp = moveEntryToAnotherGroupInKeePassFile(g, movingEntry, entryTargetGroup);
                groupBuilder.addGroup(newGrp);
            }

            return groupBuilder.build();
        } else {
            throw new NullPointerException("Group can not be null.");
        }
    }

    @Transactional
    public Group removeEntryAndBuildGroup(Group group, UUID removeEntryUuid) {
        if (group != null) {

            GroupBuilder groupBuilder = null;

            List<Entry> entries = new ArrayList<>();
            if (entries != null) {
                for (Entry e : group.getEntries()) {
                    if (e.getUuid().equals(removeEntryUuid)) {
                        continue;
                    } else {
                        entries.add(e);
                    }
                }
            }
            groupBuilder = new GroupBuilder(group.getName()).addEntries(entries);

            for (Group g : group.getGroups()) {
                Group newGrp = removeEntryAndBuildGroup(g, removeEntryUuid);
                groupBuilder.addGroup(newGrp);
            }

            return groupBuilder.build();
        } else {
            throw new NullPointerException("Group can not be null.");
        }
    }

    @Transactional
    public KeePassModel addNewEntryToKeePassFile(ModifyEntryDto modifyEntryDto, HttpServletRequest request) {
        KeePassModel keePassModel = keePassService.findByIDWithUserCheck(modifyEntryDto.getKdbxFileDto().getKdbxFileIdDto(), request);

        KeePassFile keePassFile = keePassModel.getKeePassFile(modifyEntryDto.getKdbxFileDto().getKdbxFilePwDto());

        String targetGroupDirection = modifyEntryDto.getEntryDto().getTargetGroupDirectionDto();
        Group targetGroup = groupServiceOLD.getGroupNodeFromDirection(keePassFile, targetGroupDirection);

        Entry entry = entryMapper.dtoToEntry(modifyEntryDto.getEntryDto());

        Group rootGroup = keePassFile.getRoot().getGroups().get(0);
        Group addedNewEntryToGroup = addNewEntryToGroup(rootGroup, entry, targetGroup);

        keePassModel = keePassService.uploadModifiedKdbxFile(keePassModel, modifyEntryDto, addedNewEntryToGroup);

        return keePassModel;
    }

    @Transactional
    public KeePassModel moveEntryToAnotherGroupInKeePassFile(ModifyEntryDto modifyEntryDto, HttpServletRequest request) {
        String kdbxFilePw = modifyEntryDto.getKdbxFileDto().getKdbxFilePwDto();

        KeePassModel keePassModel = keePassService.findByIDWithUserCheck(modifyEntryDto.getKdbxFileDto().getKdbxFileIdDto(), request);

        KeePassFile keePassFile = keePassModel.getKeePassFile(kdbxFilePw);
        Entry moveEntryToAnotherGroup = keePassFile.getEntryByUUID(modifyEntryDto.getEntryDto().getEntryUuidDto());

        Group targetGroup = groupServiceOLD.getGroupNodeFromDirection(
                keePassFile,
                modifyEntryDto.getEntryDto().getTargetGroupDirectionDto());

        Group movedEntryToAnotherGroup
                = moveEntryToAnotherGroupInKeePassFile(keePassModel.getRootGroup(kdbxFilePw), moveEntryToAnotherGroup, targetGroup);

        keePassModel = keePassService.uploadModifiedKdbxFile(keePassModel, modifyEntryDto, movedEntryToAnotherGroup);

        return keePassModel;
    }

    @Transactional
    public KeePassModel removeEntryFromKeePassFile(ModifyEntryDto modifyEntryDto, HttpServletRequest request) {
        KeePassModel keePassModel = keePassService.findByIDWithUserCheck(modifyEntryDto.getKdbxFileDto().getKdbxFileIdDto(), request);

        Group removedEntry = removeEntryAndBuildGroup(
                keePassModel.getRootGroup(modifyEntryDto.getKdbxFileDto().getKdbxFilePwDto()),
                modifyEntryDto.getEntryDto().getEntryUuidDto());

        keePassModel = keePassService.uploadModifiedKdbxFile(keePassModel, modifyEntryDto, removedEntry);

        return keePassModel;
    }

    @Transactional
    public EntryDto getEntryDetails(ModifyEntryDto modifyEntryDto, HttpServletRequest request) {
        KeePassModel keePassModel = findK3PModelWithOrWithoutAttachment(modifyEntryDto.getKdbxFileDto().getKdbxFileIdDto(), request);

        KeePassFile keePassFile = keePassModel.getKeePassFile(modifyEntryDto.getKdbxFileDto().getKdbxFilePwDto());

        Optional<Entry> optionalEntry = keePassFile.getEntries()
                .stream()
                .filter(e -> e.getUuid().equals(modifyEntryDto.getEntryDto().getEntryUuidDto()))
                .findFirst();

        if (optionalEntry.isPresent()) {
            EntryDto entryDto = entryMapper.entryToDtoWithoutAttachmentData(optionalEntry.get());
            return entryDto;
        } else {
            throw new NullPointerException("\nThere is no entry with this UUID: " + modifyEntryDto.getEntryDto().getEntryUuidDto());
        }
    }

}
