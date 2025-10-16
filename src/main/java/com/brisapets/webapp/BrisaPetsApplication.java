package com.brisapets.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
// ^ Importação necessária! nao apagar

@SpringBootApplication
public class BrisaPetsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrisaPetsApplication.class, args);
    }
}