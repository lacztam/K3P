package hu.lacztam.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginModel {

    @NotEmpty
    @NotNull
    private String email;
    @NotNull
    @NotEmpty
    private String password;
}
