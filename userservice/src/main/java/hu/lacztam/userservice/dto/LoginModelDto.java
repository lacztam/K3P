package hu.lacztam.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@NotNull
public class LoginModelDto {

    @NotEmpty
    @NotNull
    private String emailDto;
    @NotNull
    @NotEmpty
    private String passwordDto;

}
