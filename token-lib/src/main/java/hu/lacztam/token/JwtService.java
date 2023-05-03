package hu.lacztam.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final String AUTH = "auth";
    
    private String issuer = "k3p-user-service";
    
    @Value("${hu.lacztam.token.keypaths.private:#{null}}")
    private String pathToPemWithPrivateKey;
    
    @Value("${hu.lacztam.token.keypaths.public:#{null}}")
    private String pathToPemWithPublicKey;
    
    private Algorithm signerAlg;
    private Algorithm validatorAlg;
//    private Algorithm alg = Algorithm.HMAC512("HMAC512");
    
    @PostConstruct
    public void init() throws Exception {
    	if(pathToPemWithPrivateKey != null) {
    		signerAlg = Algorithm.ECDSA512(null, (ECPrivateKey) PemUtils.getPrivateKey(pathToPemWithPrivateKey));
    	}
    	
    	if(pathToPemWithPublicKey != null) {
    		validatorAlg = Algorithm.ECDSA512((ECPublicKey) PemUtils.getPublicKey(pathToPemWithPublicKey), null);
    	}
    }
    
    public String createJwtToken(UserDetails principal) {
        return JWT.create()
            .withSubject(principal.getUsername())
            .withArrayClaim(AUTH, principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new))
            .withExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(20)))
            .withIssuer(issuer)
            .sign(signerAlg);
    }

    public UserDetails parseJwt(String jwtToken) {
        DecodedJWT decodedJwt = JWT.require(validatorAlg)
            .withIssuer(issuer)
            .build()
            .verify(jwtToken);
        return new User(decodedJwt.getSubject(), "dummy", 
                decodedJwt.getClaim(AUTH).asList(String.class)
                .stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                );
    }
}