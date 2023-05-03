//package hu.lacztam.keepassservice.web;
//
//import de.slackspace.openkeepass.domain.Group;
//import hu.lacztam.keepassservice.dto.KdbxFileDto;
//import hu.lacztam.keepassservice.dto.ModifyGroupDto;
//import hu.lacztam.keepassservice.model.postgres.KeePassModel;
//import hu.lacztam.keepassservice.service.postgres.GroupService;
//import hu.lacztam.keepassservice.service.postgres.KeePassService;
//import lombok.AllArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.validation.Valid;
//import javax.validation.constraints.NotNull;
//
//@AllArgsConstructor
//@RestController
//@CrossOrigin
//@RequestMapping("/api/kdbx/{kdbxFileId}/groups")
//public class GroupController {
//
//    private final GroupService groupService;
//    private final KeePassService keePassService;
//
//    @PostMapping("/get-top-group")
//    public Group getTopGroupWithoutPasswords(
//            @RequestBody KdbxFileDto kdbxFileDto,
//            @NotNull @PathVariable long kdbxFileId,
//            HttpServletRequest request) {
//
//        KeePassModel keePassModel = keePassService.findByIDWithUserCheck(kdbxFileId, request);
//
//        Group topGroup = groupService.getTopGroupWithoutPassword(keePassModel, kdbxFileDto.getKdbxFilePwDto());
//
//        return topGroup;
//    }
//
//    @PostMapping
//    public Group createNewGroup(
//            @RequestBody ModifyGroupDto modifyGroupDto,
//            @NotNull @PathVariable long kdbxFileId,
//            HttpServletRequest request) {
//
//            modifyGroupDto.getKdbxFileDto().setKdbxFileIdDto(kdbxFileId);
//
//        KeePassModel keePassModel = groupService.addNewGroupToKeePassFile(modifyGroupDto, request);
//
//        return groupService.getTopGroupWithoutPassword(keePassModel, modifyGroupDto.getKdbxFileDto().getKdbxFilePwDto());
//    }
//
//    // TO-DO
//    @PutMapping
//    public Group modifyGroup(
//            @RequestBody ModifyGroupDto modifyGroupDto,
//            @NotNull @PathVariable long kdbxFileId,
//            HttpServletRequest request) {
//        modifyGroupDto.getKdbxFileDto().setKdbxFileIdDto(kdbxFileId);
//
//        KeePassModel keePassModel = groupService.modifyGroupInKeePassFile(modifyGroupDto, request);
//
//        return groupService.getTopGroupWithoutPassword(keePassModel, modifyGroupDto.getKdbxFileDto().getKdbxFilePwDto());
//    }
//
//    @PutMapping("/move-group")
//    public Group moveExistingGroupToAnotherGroup(
//            @Valid @RequestBody ModifyGroupDto modifyGroupDto,
//            @NotNull @PathVariable long kdbxFileId,
//            HttpServletRequest request) {
//        modifyGroupDto.getKdbxFileDto().setKdbxFileIdDto(kdbxFileId);
//
//        KeePassModel keePassModel = groupService.moveExistingGroupToAnotherGroupInKeePassFile(modifyGroupDto, request);
//
//        return groupService.getTopGroupWithoutPassword(keePassModel, modifyGroupDto.getKdbxFileDto().getKdbxFilePwDto());
//    }
//
//    @DeleteMapping
//    public Group deleteGroup(
//            @Valid @RequestBody ModifyGroupDto modifyGroupDto,
//            @NotNull @PathVariable long kdbxFileId,
//            HttpServletRequest request) {
//        modifyGroupDto.getKdbxFileDto().setKdbxFileIdDto(kdbxFileId);
//
//        KeePassModel keePassModel = groupService.deleteGroupFromKeePassFile(modifyGroupDto, request);
//
//        return groupService.getTopGroupWithoutPassword(keePassModel, modifyGroupDto.getKdbxFileDto().getKdbxFilePwDto());
//    }
//
//
//}