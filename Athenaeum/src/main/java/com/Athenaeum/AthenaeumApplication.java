package com.Athenaeum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class AthenaeumApplication {

    public static void main(String[] args) {
        SpringApplication.run(AthenaeumApplication.class, args);
    }
}