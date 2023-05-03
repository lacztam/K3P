package hu.lacztam.userservice.mapper;

import hu.lacztam.userservice.dto.ApplicationUserDto;
import hu.lacztam.userservice.model.ApplicationUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApplicationUserMapper {

    @Mapping(target = "userIdDto", source ="userId")
    @Mapping(target = "firstNameDto", source ="firstName")
    @Mapping(target = "lastNameDto", source ="lastName")
    @Mapping(target = "emailDto", source ="email")
    @Mapping(target = "passwordDto", source ="password")
    @Mapping(target = "accountNonExpiredDto", source ="accountNonExpired")
    @Mapping(target = "accountNonLockedDto", source ="accountNonLocked")
    @Mapping(target = "credentialsNonExpiredDto", source ="credentialsNonExpired")
    @Mapping(target = "enabledDto", source ="enabled")
    @Mapping(target = "secretDto", source ="secret")
    @Mapping(target = "registrationTimeDto", source ="registrationTime")
    @Mapping(target = "rolesDto", ignore = true)
    ApplicationUserDto applicationUserToDto(ApplicationUser applicationUser);

    //TO-DO:
    @Mapping(target = "userId", source ="userIdDto")
    @Mapping(target = "firstName", source ="firstNameDto")
    @Mapping(target = "lastName", source ="lastNameDto")
    @Mapping(target = "email", source ="emailDto")
    @Mapping(target = "password", source ="passwordDto")
    @Mapping(target = "accountNonExpired", source ="accountNonExpiredDto")
    @Mapping(target = "accountNonLocked", source ="accountNonLockedDto")
    @Mapping(target = "credentialsNonExpired", source ="credentialsNonExpiredDto")
    @Mapping(target = "enabled", source ="enabledDto")
    @Mapping(target = "secret", source ="secretDto")
    @Mapping(target = "registrationTime", source ="registrationTimeDto")
    @Mapping(target = "roles", source ="rolesDto")
    ApplicationUser dtoToApplicationUser(ApplicationUserDto dto);

    List<ApplicationUserDto> applicationUsersToDtos(List<ApplicationUser> applicationUsers);
    List<ApplicationUser> dtoListToApplicationUser(List<ApplicationUserDto> applicationUserDtos);
}
