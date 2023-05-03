package hu.lacztam.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;


@Data
@AllArgsConstructor
@Entity
@Table(name = "application_user")
public class ApplicationUser implements UserDetails {
    @Id
    @GeneratedValue
    @Column(name = "application_user_id", unique = true)
    private long userId;
    @Column(name ="first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "email", unique = true)
    private String email;
    @Column(name = "password")
    private String password;
    @Column(name = "account_non_expired")
    private boolean accountNonExpired;
    @Column(name = "account_non_locked")
    private boolean accountNonLocked;
    @Column(name = "credentials_non_expired")
    private boolean credentialsNonExpired;
    @Column(name = "enabled")
    private boolean enabled;
    @Column(name = "secret")
    private String secret;
    @Column(name = "registration_time")
    private LocalDateTime registrationTime;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(
                    name = "user_id",
                    referencedColumnName = "application_user_id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id",
                    referencedColumnName = "role_id"
            )
    )
    private Set<UserRole> roles;

    public ApplicationUser() {
        this.secret = Base32.random();
        this.registrationTime = LocalDateTime.now();
    }

    public ApplicationUser(String firstName, String lastName, String email, String password) {
        this.secret = Base32.random();
        this.registrationTime = LocalDateTime.now();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.accountNonExpired = true;
        this.credentialsNonExpired = true;
        this.accountNonLocked = true;
        this.enabled = false;
    }

    public void addRole(UserRole role){
        if(this.roles == null)
            roles = new HashSet<>();
        roles.add(role);
    }

    public void addRoles(Collection<UserRole> roles){
        if(this.roles == null)
            roles = new HashSet<>();
        roles.addAll(roles);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthoritySet = new HashSet<>();
        this.roles.forEach(r -> grantedAuthoritySet.add(new SimpleGrantedAuthority(r.getRolename())));

        return grantedAuthoritySet;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    public String getRolesInString(){
        List<String> r = new ArrayList<>();
        while(roles.iterator().hasNext()){
            r.add(roles.iterator().next().getRolename());
        }
        return r.toString();
    }
}
