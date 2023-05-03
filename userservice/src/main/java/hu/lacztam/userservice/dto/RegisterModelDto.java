package hu.lacztam.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NotNull
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterModelDto {

    @NotNull
    @NotEmpty
    private String firstNameDto;
    @NotNull
    @NotEmpty
    private String lastNameDto;
    @NotEmpty
    @NotNull
    private String emailDto;
    @NotEmpty
    @NotNull
    private String passwordDto;

}
