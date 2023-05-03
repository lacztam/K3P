package hu.lacztam.token;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;

import static hu.lacztam.token.JwtAuthFilter.createUserDetailsFromAuthHeader;

@AllArgsConstructor
@Service
@Configuration
public class UserDetailsFromJwtToken {
    private static final String AUTHORIZATION = "Authorization";
    private final JwtService jwtService;

    @Transactional
    public boolean hasAdminRole(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION);
        UsernamePasswordAuthenticationToken authentication = createUserDetailsFromAuthHeader(authHeader, jwtService);
        Collection<GrantedAuthority> authorities = authentication.getAuthorities();

        Optional<GrantedAuthority> filter = authorities.stream().findFirst()
                .filter(a -> a.getAuthority().equals("ADMIN"));

        return filter.isPresent();
    }

    @Transactional
    public UserDetails getUserDetailsFromJwtToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION);
        UsernamePasswordAuthenticationToken authentication = createUserDetailsFromAuthHeader(authHeader, jwtService);

        return ((UserDetails) authentication.getPrincipal());
    }

    @Transactional
    public boolean checkUser(HttpServletRequest request, String username) {
        UserDetails userDetails = getUserDetailsFromJwtToken(request);
        String usernameFromJwtToken = userDetails.getUsername();

        return username.equals(usernameFromJwtToken);
    }

}

