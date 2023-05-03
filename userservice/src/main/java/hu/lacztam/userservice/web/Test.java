package hu.lacztam.userservice.web;

import hu.lacztam.token.UserDetailsFromJwtToken;
import lombok.AllArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/test")
@AllArgsConstructor
public class Test {

    private final JmsTemplate jmsTemplate;
    public static final String RESTART_IN_MEMORY_TIMER = "timer-from-TS-to-GS";
    public static final String START_IN_MEMORY_KEEPASS_TIMER = "login-timer-from-US-to-TS";
    private final  UserDetailsFromJwtToken userDetailsFromJwtToken;

    @PostMapping("/re")
    public void jmsReScheduleTask(HttpServletRequest request){
        String email = userDetailsFromJwtToken.getUserDetailsFromJwtToken(request).getUsername();
        jmsTemplate.convertAndSend(RESTART_IN_MEMORY_TIMER, email);
    }

    @PostMapping("/start")
    public void startTask(HttpServletRequest request){
        String email = userDetailsFromJwtToken.getUserDetailsFromJwtToken(request).getUsername();
        jmsTemplate.convertAndSend(START_IN_MEMORY_KEEPASS_TIMER, email);
        System.err.println("task started START_IN_MEMORY_KEEPASS_TIMER");
    }

}
