package com.flick.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@ComponentScan(basePackages = "com.flick.business")
@EnableWebSecurity
public class SgvsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SgvsApiApplication.class, args);
    }
}
