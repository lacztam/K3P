package hu.lacztam.userservice.dto;

import hu.lacztam.userservice.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUserDto {
    private long userIdDto;
    private String firstNameDto;
    private String lastNameDto;
    private String emailDto;
    private String passwordDto;
    private boolean accountNonExpiredDto;
    private boolean accountNonLockedDto;
    private boolean credentialsNonExpiredDto;
    private boolean enabledDto;
    private String secretDto;
    private LocalDateTime registrationTimeDto;
    private Set<UserRole> rolesDto;

    public void addRole(String rolename){
        if(this.rolesDto == null)
            this.rolesDto = new HashSet<>();
        rolesDto.add(new UserRole(rolename));
    }

/*
    public List<String> getRoleList(){
        return roleAuthorityDetailsListDto
                .stream()
                .map(ra -> ra.getRoleAuthority())
                .collect(Collectors.toList())
                .stream()
                .map(r -> r.getAuthority())
                .collect(Collectors.toList());
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleAuthorityDetailsListDto
                .stream()
                .map(roleAuthorityDetails -> roleAuthorityDetails.getRoleAuthority())
                .collect(Collectors.toList());
    }*/
}
