package hu.lacztam.keepassservice.mapper;

import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.dto.KeePassModelDto;
import hu.lacztam.keepassservice.model.redis.InMemoryKeePassModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface KeePassModelMapper {

    @Mapping(target = "kdbxFileIdDto", source ="id")
    @Mapping(target = "redisIdDto", source = "redisId")
    @Mapping(target = "emailDto", source = "email")
    @Mapping(target = "createdDto", source = "created")
    @Mapping(target = "kdbxFileDto", ignore = true)
    @Mapping(target = "kdbxFilePasswordDto", ignore = true)
    KeePassModelDto keePassModelToDto(KeePassModel keePassModel);

    @Named("with_kdbx_file")
    @Mapping(target = "kdbxFileIdDto", source ="id")
    @Mapping(target = "redisIdDto", source = "redisId")
    @Mapping(target = "emailDto", source = "email")
    @Mapping(target = "createdDto", source = "created")
    @Mapping(target = "kdbxFileDto", source = "kdbxFile")
    @Mapping(target = "kdbxFilePasswordDto", ignore = true)
    KeePassModelDto keePassModelToDtoWithKdbxFile(KeePassModel keePassModel);

}
