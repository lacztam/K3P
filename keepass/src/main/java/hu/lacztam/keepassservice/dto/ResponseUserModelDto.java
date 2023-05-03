package hu.lacztam.keepassservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUserModelDto {

    private String jwtToken;
    private String firstName;
    private String lastName;
    private String email;
    private String roles;
}
