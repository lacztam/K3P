package hu.lacztam.userservice.web;

import hu.lacztam.token.UserDetailsFromJwtToken;
import hu.lacztam.userservice.dto.RegisterModelDto;
import hu.lacztam.userservice.model.ApplicationUser;
import hu.lacztam.userservice.service.AccountService;
import hu.lacztam.userservice.service.ApplicationUserService;
import lombok.AllArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

import static hu.lacztam.userservice.config.JmsConfig.*;

@AllArgsConstructor
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final ApplicationUserService applicationUserService;
    private final AccountService accountService;
    private final JmsTemplate jmsTemplate;
    private final UserDetailsFromJwtToken userDetailsFromJwtToken;

    @CrossOrigin("*")
    @PostMapping
    public boolean registerAccount(@RequestBody @Valid RegisterModelDto registerModelDto) {

        boolean isRegistrationSucceed = accountService.registerAccount(registerModelDto);

        String email = registerModelDto.getEmailDto();
        if(email == null)
            throw new NullPointerException("Email can not be null.");

        //temp solution,
        // the user must confirm registration within email
        System.err.println("sending jms to create keepass file");
        this.jmsTemplate.convertAndSend(NEW_ACCOUNT_CREATE_KEEPASS_FILE, email);
        System.err.println("after jms");


//        Object isKeePassPwEncrypted = this.jmsTemplate.receiveAndConvert(NEW_ACCOUNT_IS_KEEPASS_PW_ENCRYPTED);
//        boolean encryptedPassword = (Boolean)isKeePassPwEncrypted;
//        if(!encryptedPassword)
//            throw new RuntimeException("Error on keepass file password encryption.");
//
//        System.out.println("jms isKeePassPwEncrypted: " + encryptedPassword);
//
//        Object isKeePassFileCreated = this.jmsTemplate.receiveAndConvert(NEW_ACCOUNT_IS_KEEPASS_CREATED);
//        boolean createdKeePassFile = (Boolean)isKeePassFileCreated;
//        if(!createdKeePassFile)
//            throw new RuntimeException("Error on keepass file creation.");
//
//        System.out.println("jms isKeePassFileCreated: " + createdKeePassFile);
        // end of temp solution

        return isRegistrationSucceed;
    }

    //TO-DO: delete user model from redisdb after delete command executed
    @DeleteMapping
    public boolean deleteAccount(HttpServletRequest request) {
        boolean result = accountService.deleteAccount(request);
        return result;
    }

    @PutMapping
    public void requestNewRegistrationToken(String email, HttpServletRequest request) {
        ApplicationUser applicationUser = applicationUserService.findByEmail(email);
        accountService.publishEvent(applicationUser, request.getLocale());
    }

    @GetMapping("/confirm")
    public String confirmAccountRegistration(
            @RequestParam("token") final String token,
            HttpServletRequest request) {

        final String checkToken = accountService.validateVerificationToken(token);

        if (checkToken.equals("valid")) {
            String email = userDetailsFromJwtToken.getUserDetailsFromJwtToken(request).getUsername();
            this.jmsTemplate.convertAndSend(NEW_ACCOUNT_CREATE_KEEPASS_FILE, email);
            return "Account verification was successfull.";
        } else if (checkToken.equals("expired")) {
            return "Token expired.";
        } else {
            return "Token is invalid.";
        }
    }

    public void authWithoutPassword(ApplicationUser user) {
        List<GrantedAuthority> authorities = (List<GrantedAuthority>) user.getAuthorities();
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}