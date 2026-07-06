package com.sportbuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SportbuddyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SportbuddyApplication.class, args);
	}

}
