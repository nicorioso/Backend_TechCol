package com.techgroup.techcop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class TechCopApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechCopApplication.class, args);
	}

}
