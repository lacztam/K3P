package hu.lacztam.userservice.service;

import hu.lacztam.token.UserDetailsFromJwtToken;
import hu.lacztam.userservice.config.ConfigProperties;
import hu.lacztam.userservice.dto.RegisterModelDto;
import hu.lacztam.userservice.exception.UserAlreadyExistException;
import hu.lacztam.userservice.model.ApplicationUser;
import hu.lacztam.userservice.model.UserRole;
import hu.lacztam.userservice.model.VerificationToken;
import hu.lacztam.userservice.registration.IUserService;
import hu.lacztam.userservice.registration.OnRegistrationCompleteEvent;
import hu.lacztam.userservice.repository.ApplicationUserRepository;
import hu.lacztam.userservice.repository.VerificationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;

import static hu.lacztam.userservice.config.JmsConfig.DELETE_KEEPASS_FILE;
import static hu.lacztam.userservice.config.JmsConfig.DELETE_KEYPAIR;


@AllArgsConstructor
@Service
public class AccountService implements IUserService {

    private final ApplicationEventPublisher eventPublisher;
    private final ApplicationUserService applicationUserService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final ConfigProperties config;
    private final UserRoleService userRoleService;
    private final UserDetailsFromJwtToken userDetailsFromJwtToken;
    public static final String TOKEN_INVALID = "invalidToken";
    public static final String TOKEN_EXPIRED = "expired";
    public static final String TOKEN_VALID = "valid";
    private final JmsTemplate jmsTemplate;
    private final SimpleMessageConverter converter = new SimpleMessageConverter();

    // TO-DO: appUrl
    @Transactional
    public boolean registerAccount(@RequestBody @Valid RegisterModelDto registerModelDto) {
        if (applicationUserService.findByEmailOptional(registerModelDto.getEmailDto()).isPresent())
            throw new UserAlreadyExistException("Account already exists: "
                    + registerModelDto.getEmailDto());

        ApplicationUser registered = null;
        try {
            registered = createApplicationUserModel(registerModelDto);

            if (registered == null)
                return false;

            //temporary solution
            registered.setEnabled(true);
            applicationUserService.save(registered);
            //end of temp sol

            //publishEvent(registered, request.getLocale());
        } catch (UserAlreadyExistException e) {
            throw new UserAlreadyExistException("An account with that email already exists.");
        }

        return true;
    }

    @Transactional
    public void publishEvent(ApplicationUser applicationUser, Locale locale) {
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(applicationUser, locale, config.getAppUrl()));
    }

    @Transactional
    public String validateVerificationToken(String token) {

        final VerificationToken verificationToken = verificationTokenRepository.findByToken(token);

        if (verificationToken == null) {
            return TOKEN_INVALID;
        }

        final ApplicationUser user = verificationToken.getApplicationUser();
        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate()
                .getTime() - cal.getTime()
                .getTime()) <= 0) {

            verificationTokenRepository.delete(verificationToken);
            return TOKEN_EXPIRED;
        }

        verificationTokenRepository.delete(verificationToken);

        user.setEnabled(true);
        applicationUserRepository.save(user);

        return TOKEN_VALID;
    }

    @Override
    public VerificationToken getVerificationToken(String verificationToken) {
        return verificationTokenRepository.findByToken(verificationToken);
    }

    @Transactional
    @Override
    public ApplicationUser createApplicationUserModel(RegisterModelDto registerModelDto) throws UserAlreadyExistException {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        ApplicationUser applicationUser
                = new ApplicationUser(
                registerModelDto.getFirstNameDto(),
                registerModelDto.getLastNameDto(),
                registerModelDto.getEmailDto(),
                passwordEncoder.encode(registerModelDto.getPasswordDto())
        );
        applicationUser = applicationUserService.save(applicationUser);

        UserRole role = userRoleService.findByRolenameOrCrateIt("USER");
        applicationUser.addRole(role);
        applicationUser = applicationUserService.save(applicationUser);

        return applicationUser;
    }

    @Override
    public ApplicationUser getUser(String verificationToken) {
        ApplicationUser applicationUser = verificationTokenRepository
                .findByToken(verificationToken)
                .getApplicationUser();

        return applicationUser;
    }

    @Override
    public void createVerificationToken(ApplicationUser user, String token) {
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);
    }

    // TO-DO: KeePassService
    @Transactional
    public boolean deleteAccount(HttpServletRequest request) {

        String email = userDetailsFromJwtToken.getUserDetailsFromJwtToken(request).getUsername();

        Optional<ApplicationUser> optionalUser
                = applicationUserService.findByEmailOptionalWithRoleAuthorityDetailsList(email);

        if (optionalUser.isEmpty())
            return false;

        deleteKeePassFile(email);
        deleteKeyPair(email);

        ApplicationUser applicationUser = optionalUser.get();
        applicationUserService.delete(applicationUser);

        return true;
    }

    private void deleteKeePassFile(String email){
        deleteQueue(email, DELETE_KEEPASS_FILE);
    }

    private void deleteKeyPair(String email){
        deleteQueue(email, DELETE_KEYPAIR);
    }

    private void deleteQueue(String email, String queue) {
        Object receiveDeletionProcessResult = this.jmsTemplate.sendAndReceive(queue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage(email);
            }
        });

        Object isDeleted = null;
        try {
            isDeleted = this.converter.fromMessage((ObjectMessage) receiveDeletionProcessResult);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
        boolean isKeePassFileDeleted = (Boolean) isDeleted;
    }

}