package hu.lacztam.userservice.service;

import hu.lacztam.userservice.dto.RegisterModelDto;
import lombok.AllArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import static hu.lacztam.userservice.config.JmsConfig.NEW_ACCOUNT_CREATE_KEEPASS_FILE;

@Service
@AllArgsConstructor
public class FirstRunInitialization {

    private final AccountService accountService;
    private final JmsTemplate jmsTemplate;

    public void registerSampleUser(){
        String email = "u";

        RegisterModelDto registerModelDto
                = new RegisterModelDto("Sample", "User", email, email);
        accountService.registerAccount(registerModelDto);

        this.jmsTemplate.convertAndSend(NEW_ACCOUNT_CREATE_KEEPASS_FILE, email);
    }
}
