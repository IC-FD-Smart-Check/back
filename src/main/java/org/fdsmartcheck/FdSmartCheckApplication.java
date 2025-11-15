package org.fdsmartcheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class FdSmartCheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(FdSmartCheckApplication.class, args);
    }
}