package com.egfs.biometrictest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ZktecoProperties.class)
public class BiometrictestApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiometrictestApplication.class, args);
    }

}
