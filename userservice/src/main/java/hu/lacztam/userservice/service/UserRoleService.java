package hu.lacztam.userservice.service;

import hu.lacztam.userservice.model.UserRole;
import hu.lacztam.userservice.repository.UserRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@AllArgsConstructor
@Service
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    @Transactional
    public UserRole save(UserRole userRole){
        return userRoleRepository.save(userRole);
    }

    @Transactional
    public UserRole save(String rolename){
        return save(new UserRole(rolename));
    }

    @Transactional
    public UserRole findByRolenameOrCrateIt(String rolename){
        Optional<UserRole> optionalUserRole = userRoleRepository.findByRolename(rolename);
        return optionalUserRole.isPresent() ? optionalUserRole.get() : save(rolename);
    }

}
