package hu.lacztam.userservice.security;

import hu.lacztam.token.JwtService;
import hu.lacztam.userservice.dto.LoginModelDto;
import hu.lacztam.userservice.dto.ResponseUserModelDto;
import hu.lacztam.userservice.model.ApplicationUser;
import hu.lacztam.userservice.service.ApplicationUserService;
import lombok.AllArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

import static hu.lacztam.userservice.config.JmsConfig.START_IN_MEMORY_KEEPASS_TIMER;
import static hu.lacztam.userservice.config.JmsConfig.LOGIN_CONVERT_KEEPASS_DATA;

@AllArgsConstructor
@CrossOrigin(maxAge = 3600)
@RestController
public class JwtLoginController {

	private final AuthenticationManager authenticationManager;
	private final ApplicationUserService applicationUserService;
	private final JwtService jwtService;
	private final JmsTemplate jmsTemplate;

	@CrossOrigin("*")
	@PostMapping("/api/jwt-login")
	public ResponseUserModelDto login(@RequestBody @Valid LoginModelDto loginModelDto) {

		Authentication authenticate =
				authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(loginModelDto.getEmailDto(), loginModelDto.getPasswordDto() ));

		Optional<ApplicationUser> optionalUser
				= applicationUserService.findByEmailOptionalWithRoleAuthorityDetailsList(loginModelDto.getEmailDto());

		ApplicationUser applicationUser = null;
		if(authenticate != null){
			if(optionalUser.isEmpty()){
				throw new NullPointerException();
			} else {
				applicationUser = optionalUser.get();
				String applicationUserEmail = applicationUser.getEmail();

				String jwtToken = jwtService.createJwtToken((UserDetails)authenticate.getPrincipal());

				ResponseUserModelDto response = new ResponseUserModelDto();
				response.setJwtToken(jwtToken);
				response.setEmail(applicationUserEmail);
				response.setFirstName(applicationUser.getFirstName());
				response.setLastName(applicationUser.getLastName());

				jmsTemplate.convertAndSend(LOGIN_CONVERT_KEEPASS_DATA, applicationUserEmail);
				jmsTemplate.convertAndSend(START_IN_MEMORY_KEEPASS_TIMER, applicationUserEmail);

				return response;
			}
		}else
			throw new SecurityException("Can not authenticate.");
	}

}