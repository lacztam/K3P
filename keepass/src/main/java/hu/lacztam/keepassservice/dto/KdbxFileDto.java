package hu.lacztam.keepassservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KdbxFileDto {
    @NotNull
    @NotEmpty
    private long kdbxFileIdDto;
    @NotNull
    @NotEmpty
    private String kdbxFilePwDto;
    private String pathToSaveDto;

    private String newPasswordDto;
    private String keePassDatabaseNameDto;
    private String kdbxFileNameDto;

}
