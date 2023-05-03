package hu.lacztam.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfig {

    public static final String NEW_ACCOUNT_CREATE_KEEPASS_FILE = "new-account-from-US-to-KS";
    public static final String DELETE_KEEPASS_FILE = "delete-account-from-US-to-KS";
    public static final String DELETE_KEYPAIR = "delete-account-from-US-to-CS";
    public static final String LOGIN_CONVERT_KEEPASS_DATA = "login-from-US-to-KS";
    public static final String START_IN_MEMORY_KEEPASS_TIMER = "login-timer-from-US-to-TS";

    @Bean
    public MessageConverter jacsonJmsMessageConverter(ObjectMapper objectMapper){
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        return converter;
    }
}
