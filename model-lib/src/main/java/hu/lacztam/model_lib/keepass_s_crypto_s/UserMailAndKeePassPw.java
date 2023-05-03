package hu.lacztam.model_lib.keepass_s_crypto_s;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserMailAndKeePassPw implements Serializable {
    private String email;
    private String password;

}
