package com.ahmete.authservice;

import com.ahmete.authservice.jwt.config.JwtProperties;
import com.ahmete.authservice.userclient.config.UserServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		JwtProperties.class,
		UserServiceProperties.class
})
public class AuthServiceApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}