package com.example.seclibtestapp;

import com.seclib.SecLibConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SecLibConfiguration.class)
public class SecLibTestAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecLibTestAppApplication.class, args);
    }

}
