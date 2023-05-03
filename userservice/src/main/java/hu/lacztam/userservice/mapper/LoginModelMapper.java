package hu.lacztam.userservice.mapper;

import hu.lacztam.userservice.dto.LoginModelDto;
import hu.lacztam.userservice.model.LoginModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoginModelMapper {

    @Mapping(target = "email", source = "emailDto")
    @Mapping(target = "password", source = "passwordDto")
    LoginModel loginModel(LoginModelDto loginModelDto);
}
