package com.mprevisic.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Entry point of the application
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@SpringBootApplication(scanBasePackages = { "com.mprevisic.user" })
@EnableJpaRepositories("com.mprevisic.user.repository")
@EntityScan("com.mprevisic.user.domain")
public class UserApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserApiApplication.class, args);
	}
}
