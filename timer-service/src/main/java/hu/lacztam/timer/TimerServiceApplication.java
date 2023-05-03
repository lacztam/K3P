package hu.lacztam.timer;

import hu.lacztam.timer.config.ConfigProperties;
import hu.lacztam.timer.service.FirstRunInitialization;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AllArgsConstructor
public class TimerServiceApplication implements CommandLineRunner {

    private final FirstRunInitialization firstRunInitialization;
    @Autowired
    ConfigProperties configProperties;


    public static void main(String[] args) {
        SpringApplication.run(TimerServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        boolean isTableExists = firstRunInitialization.checkIfTableExists();
        if(!isTableExists){
            firstRunInitialization.createTableForScheduledTasks();
        }

    }
}