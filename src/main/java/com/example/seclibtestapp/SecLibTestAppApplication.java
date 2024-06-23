package com.example.seclibtestapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.seclibtestapp", "com.seclib"})
public class SecLibTestAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecLibTestAppApplication.class, args);
    }

}
