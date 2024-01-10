package com.chiton.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class ChitonApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChitonApiApplication.class, args);
	}

}
