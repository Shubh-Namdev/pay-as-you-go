package com.sunking.payg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PayAsYouGoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayAsYouGoApplication.class, args);
	}

}
