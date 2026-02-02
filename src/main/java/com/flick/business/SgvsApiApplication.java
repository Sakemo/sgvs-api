package com.flick.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.flick.business")
public class SgvsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SgvsApiApplication.class, args);
    }
}
