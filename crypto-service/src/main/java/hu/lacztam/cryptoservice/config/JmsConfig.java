package hu.lacztam.cryptoservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfig {

    public static final String NEW_ACCOUNT_CREATE_KEYPAIR_ENCRYPT_KEEPASS_PASSWORD = "new-account-from-KS-to-CS";
    public static final String DELETE_KEYPAIR = "delete-account-from-US-to-CS";

    //TO-DO: is feignclient more appropriate for this usage?
    public static final String LOGIN_DECRYPT_KEEPASS_FILE_PASSWORD = "decrypt-from-KS-to-CS";

    @Bean
    public MessageConverter jacsonJmsMessageConverter(ObjectMapper objectMapper){
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        return converter;
    }
}
