package hu.lacztam.keepassservice;

import hu.lacztam.token.JwtAuthFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = {JwtAuthFilter.class, KeePassServiceApplication.class} )
public class KeePassServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeePassServiceApplication.class, args);
	}

}
