package com.josepinodev.appdomirest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AppdomirestApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppdomirestApplication.class, args);
	}

}
