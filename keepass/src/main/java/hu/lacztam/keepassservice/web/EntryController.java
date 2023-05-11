package hu.lacztam.keepassservice.web;

import de.slackspace.openkeepass.domain.Group;
import hu.lacztam.keepassservice.dto.EntryDto;
import hu.lacztam.keepassservice.service.PasswordGenerator;
import hu.lacztam.keepassservice.dto.KdbxFileDto;
import hu.lacztam.keepassservice.dto.ModifyEntryDto;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.service.postgres.EntryService;
import hu.lacztam.keepassservice.service.postgres.GroupService;
import hu.lacztam.keepassservice.service.postgres.KeePassService;
import hu.lacztam.token.UserDetailsFromJwtToken;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.UUID;

//TO-DO: add entry, remove entry
@AllArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/kdbx/{kdbxFileId}/entries")
public class EntryController {

    private final KeePassService keePassService;
    private final EntryService entryService;
    private final GroupService groupServiceOLD;
    private final UserDetailsFromJwtToken userDetailsFromJwtToken;


    //TO-DO: findById authenticate user
    @PostMapping
    public Group addNewEntryInKeePassFile(
            @RequestBody ModifyEntryDto modifyEntryDto,
            @PathVariable long kdbxFileId,
            HttpServletRequest request) {
        modifyEntryDto.getKdbxFileDto().setKdbxFileIdDto(kdbxFileId);

        KeePassModel keePassModel = entryService.addNewEntryToKeePassFile(modifyEntryDto, request);

        return groupServiceOLD.getTopGroupWithoutPassword(keePassModel, modifyEntryDto.getKdbxFileDto().getKdbxFilePwDto());
    }

    @PutMapping("/{entryUuid}/move-entry")
    public Group moveEntryToAnotherGroupInKeePassFile(
            @Valid @RequestBody ModifyEntryDto modifyEntryDto,
            @NotNull @PathVariable UUID entryUuid,
            @NotNull @PathVariable long kdbxFileId,
            HttpServletRequest request) {
        modifyEntryDto.getKdbxFileDto().setKdbxFileIdDto(kdbxFileId);
        modifyEntryDto.getEntryDto().setEntryUuidDto(entryUuid);

        KeePassModel keePassModel = entryService.moveEntryToAnotherGroupInKeePassFile(modifyEntryDto, request);

        return groupServiceOLD.getTopGroupWithoutPassword(keePassModel, modifyEntryDto.getKdbxFileDto().getKdbxFilePwDto());
    }

    @DeleteMapping("/{entryUuid}")
    public Group removeEntryInKeePassFile(
            @RequestBody ModifyEntryDto modifyEntryDto,
            @PathVariable long kdbxFileId,
            @PathVariable UUID entryUuid,
            HttpServletRequest request) {
        modifyEntryDto.getKdbxFileDto().setKdbxFileIdDto(kdbxFileId);
        modifyEntryDto.getEntryDto().setEntryUuidDto(entryUuid);

        KeePassModel keePassModel = entryService.removeEntryFromKeePassFile(modifyEntryDto, request);

        return groupServiceOLD.getTopGroupWithoutPassword(keePassModel, modifyEntryDto.getKdbxFileDto().getKdbxFilePwDto());
    }

    //TO-DO: history
    @PutMapping("/{entryUuid}")
    public Group modifyEntryInKeePassFile(
            @Valid @RequestBody ModifyEntryDto modifyEntryDto,
            @PathVariable long kdbxFileId,
            @PathVariable UUID entryUuid,
            HttpServletRequest request) {
        modifyEntryDto.getKdbxFileDto().setKdbxFileIdDto(kdbxFileId);
        modifyEntryDto.getEntryDto().setEntryUuidDto(entryUuid);

        String kdbxFilePw = modifyEntryDto.getKdbxFileDto().getKdbxFilePwDto();

        keePassService.findByIDWithUserCheck(kdbxFileId, request);

        KeePassModel keePassModel
                = entryService.modifyEntryAndUploadKeePassFile(modifyEntryDto);

        return groupServiceOLD.getTopGroupWithoutPassword(keePassModel, kdbxFilePw);
    }

    //TO-DO: mappers!
    // with no attachment it throws an exception -> handle the kdbx file with no attachments
    // obtaining the proper k3pmodel in service
    @GetMapping("/{entryUuid}")
    public EntryDto getEntryDetails(
            @PathVariable long kdbxFileId,
            @PathVariable UUID entryUuid,
            @RequestBody KdbxFileDto kdbxFileDto,
             HttpServletRequest request
    ) {

        EntryDto entryDto = new EntryDto();
        ModifyEntryDto modifyEntryDto = new ModifyEntryDto();

        modifyEntryDto.setKdbxFileDto(kdbxFileDto);
        modifyEntryDto.setEntryDto(entryDto);

        modifyEntryDto.getKdbxFileDto().setKdbxFileIdDto(kdbxFileId);
        modifyEntryDto.getEntryDto().setEntryUuidDto(entryUuid);

        EntryDto result = entryService.getEntryDetails(modifyEntryDto, request);
        return result;
    }

    @PostMapping("/generate-password")
    public String generatePassword(@RequestBody PasswordGenerator passwordGenerator) {
        String password = passwordGenerator.generatePassword();

        return password;
    }

}




