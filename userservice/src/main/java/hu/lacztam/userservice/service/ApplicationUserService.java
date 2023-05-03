package hu.lacztam.userservice.service;

import hu.lacztam.userservice.model.ApplicationUser;
import hu.lacztam.userservice.repository.ApplicationUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
@Transactional
public class ApplicationUserService implements UserDetailsService {

    private final ApplicationUserRepository applicationUserRepository;

    @Transactional
    public ApplicationUser save(ApplicationUser applicationUser) {
        return applicationUserRepository.save(applicationUser);
    }

    @Transactional
    public void delete(ApplicationUser applicationUser) {
        applicationUserRepository.delete(applicationUser);
    }

    @Transactional
    public List<ApplicationUser> findAllApplicationUser() {
        return applicationUserRepository.findAll();
    }

    @Transactional
    public ApplicationUser findByEmail(String email) {
        return applicationUserRepository.findByEmailOptional(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Optional<ApplicationUser> findByEmailOptional(String email) {
        return applicationUserRepository.findByEmailOptional(email);
    }

    @Transactional
    public Optional<ApplicationUser> findByEmailOptionalWithRoleAuthorityDetailsList(String email) {
        return applicationUserRepository.findByEmailOptionalWithRoles(email);
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        try {
            ApplicationUser user = findByEmail(email);

            return user;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}