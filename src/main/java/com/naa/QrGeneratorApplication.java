package com.naa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QrGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(QrGeneratorApplication.class, args);
        System.out.println("\n=========================================");
        System.out.println("Откройте в браузере: http://localhost:8080");
        System.out.println("=========================================\n");
    }
}