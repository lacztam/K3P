package hu.lacztam.timer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfig {

    public static final String START_IN_MEMORY_KEEPASS_TIMER = "login-timer-from-US-to-TS";
    public static final String IN_MEMORY_KEEPASS_TIMER_HAS_EXPIRED = "timer-from-TS-to-FS";
    public static final String RESTART_IN_MEMORY_TIMER = "timer-from-TS-to-GS";

    @Bean
    public MessageConverter jacsonJmsMessageConverter(ObjectMapper objectMapper){
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        return converter;
    }
}
