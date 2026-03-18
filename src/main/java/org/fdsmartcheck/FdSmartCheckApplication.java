package org.fdsmartcheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class FdSmartCheckApplication {

    public static void main(String[] args) {
        // Garante que LocalDateTime.now() e toda lógica de tempo usem SP
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        SpringApplication.run(FdSmartCheckApplication.class, args);
    }
}