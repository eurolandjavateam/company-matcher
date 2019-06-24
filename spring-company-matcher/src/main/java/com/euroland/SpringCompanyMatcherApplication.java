package com.euroland;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.euroland.matcher.service.MatcherService;

@SpringBootApplication
public class SpringCompanyMatcherApplication implements CommandLineRunner{

	@Autowired
	MatcherService matcherService;
	
	public static void main(String[] args) {
		SpringApplication.run(SpringCompanyMatcherApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		matcherService.processMatching();
		System.exit(0);
	}

}
