package com.sojoga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for the SoJoga application.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.sojoga")
public class SoJoga {

    public static void main(String[] args) {
        SpringApplication.run(SoJoga.class, args);
    }
}
