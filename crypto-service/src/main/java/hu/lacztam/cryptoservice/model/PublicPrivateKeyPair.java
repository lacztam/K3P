package hu.lacztam.cryptoservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "keypair")
public class PublicPrivateKeyPair {

    @Id
    @GeneratedValue
    private Long id;

    private String email;

    // to encrypt data
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String publicKey;

    // to decrypt data
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String privateKey;

}
