package hu.lacztam.userservice;

import hu.lacztam.userservice.service.FirstRunInitialization;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "hu.lacztam.token" , "hu.lacztam.userservice" } )
@AllArgsConstructor
public class UserserviceApplication implements CommandLineRunner {

	private final FirstRunInitialization firstRunInitialization;

	public static void main(String[] args) {
		SpringApplication.run(UserserviceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		try{
			firstRunInitialization.registerSampleUser();
		}catch (Exception e){
			System.err.println(e.getMessage());
		}
	}
}
