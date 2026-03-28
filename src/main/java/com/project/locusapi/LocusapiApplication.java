package com.project.locusapi;

import com.project.locusapi.config.JwtPropertiesConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtPropertiesConfig.class)
public class LocusapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocusapiApplication.class, args);
    }

}
