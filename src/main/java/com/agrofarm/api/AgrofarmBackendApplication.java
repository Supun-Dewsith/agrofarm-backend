package com.agrofarm.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgrofarmBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgrofarmBackendApplication.class, args);
    }

}
