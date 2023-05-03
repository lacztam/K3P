package hu.lacztam.keepassservice.web.redis;

import de.slackspace.openkeepass.domain.Group;
import hu.lacztam.keepassservice.config.ModelType;
import hu.lacztam.keepassservice.dto.GroupDto;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import hu.lacztam.keepassservice.service.redis.InMemoryGroupService;
import hu.lacztam.keepassservice.service.redis.InMemoryKeePassService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/group")
public class InMemoryGroupController {

    private final InMemoryGroupService inMemoryGroupService;
    private final InMemoryKeePassService inMemoryKeePassService;

    @GetMapping("/{modelType}")
    public Group getMainTopGroupWithoutPasswords(
            HttpServletRequest request,
            @PathVariable @NotNull String modelType) {

        InMemoryKeePassModel inMemoryKeePassModel = null;
        if(modelType.equals("main"))
            inMemoryKeePassModel
                    = inMemoryKeePassService.getKeePassFile(request, ModelType.MAIN_KEEPASS);

        if(modelType.equals("shared"))
            inMemoryKeePassModel
                    = inMemoryKeePassService.getKeePassFile(request, ModelType.SHARED_KEEPASS);

        Group topGroup = inMemoryGroupService.getTopGroupWithoutPassword(inMemoryKeePassModel);
        
        System.err.println("topGroup.getGroups().get(0).getGroups().get(0).getName():"  +
                topGroup.getGroups().get(0).getGroups().get(0).getName());

        try {
            System.err.println("topGroup.getGroups().get(0).getGroups().get(0).getTimes().getExpiryTime(): " +
                    topGroup.getGroups().get(0).getGroups().get(0).getTimes().getExpiryTime()
            );
        } catch (NullPointerException e){
            System.err.println(e.getMessage());
        }

        return topGroup;
    }

    @PostMapping("/{modelType}")
    public Group createGroup(
            @RequestBody GroupDto groupDto,
            HttpServletRequest request,
            @PathVariable @NotNull String modelType) {

        InMemoryKeePassModel inMemoryKeePassModel = null;
        if(modelType.equals("main"))
            inMemoryKeePassModel
                = inMemoryGroupService.addNewGroupToKeePassFile(request, groupDto, ModelType.MAIN_KEEPASS);

        if(modelType.equals("shared"))
            inMemoryKeePassModel
                = inMemoryGroupService.addNewGroupToKeePassFile(request, groupDto, ModelType.SHARED_KEEPASS);

        return inMemoryGroupService.getTopGroupWithoutPassword(inMemoryKeePassModel);
    }

    @DeleteMapping("/{modelType}")
    public Group deleteGroup(
            @RequestBody GroupDto groupDto,
            HttpServletRequest request,
            @PathVariable @NotNull String modelType) {

        InMemoryKeePassModel inMemoryKeePassModel = null;
        if(modelType.equals("main"))
            inMemoryKeePassModel
                = inMemoryGroupService.deleteGroup(groupDto, request, ModelType.MAIN_KEEPASS);
        if(modelType.equals("shared"))
            inMemoryKeePassModel
                = inMemoryGroupService.deleteGroup(groupDto, request, ModelType.SHARED_KEEPASS);

        return inMemoryGroupService.getTopGroupWithoutPassword(inMemoryKeePassModel);
    }

    // TO-DO can not set expire time
    @PutMapping("/modify/{modelType}")
    public Group editGroupNameOrExpireTime(
            @RequestBody GroupDto groupDto,
            HttpServletRequest request,
            @PathVariable @NotNull String modelType) {

        InMemoryKeePassModel inMemoryKeePassModel = null;

        if(modelType.equals("main"))
            inMemoryKeePassModel
                    = inMemoryGroupService.editGroupNameOrExpireTime(groupDto, request, ModelType.MAIN_KEEPASS);

        if(modelType.equals("shared"))
            inMemoryKeePassModel
                    = inMemoryGroupService.editGroupNameOrExpireTime(groupDto, request, ModelType.SHARED_KEEPASS);

        return inMemoryGroupService.getTopGroupWithoutPassword(inMemoryKeePassModel);
    }

    @PutMapping("/move/{modelType}")
    public Group moveGroup(
            @RequestBody GroupDto groupDto,
            @NotNull @PathVariable String modelType,
            HttpServletRequest request) {

        InMemoryKeePassModel inMemoryKeePassModel = null;

        if(modelType.equals("main"))
            inMemoryKeePassModel
                    = inMemoryGroupService.moveGroupToAnotherGroup(
                            groupDto,
                            request,
                            ModelType.MAIN_KEEPASS);

        if(modelType.equals("shared"))
            inMemoryKeePassModel
                    = inMemoryGroupService.moveGroupToAnotherGroup(
                            groupDto,
                            request,
                            ModelType.SHARED_KEEPASS);

        return inMemoryGroupService.getTopGroupWithoutPassword(inMemoryKeePassModel);
    }

}
