package com.encounter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EncounterApplication {

    public static void main(String[] args) {
        SpringApplication.run(EncounterApplication.class, args);
    }
}
