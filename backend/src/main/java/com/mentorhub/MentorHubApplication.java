package com.mentorhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MentRApplication - The main entry point of our Spring Boot application.
 *
 * @SpringBootApplication combines:
 * - @Configuration: marks this class as a source of bean definitions
 * - @EnableAutoConfiguration: auto-configures Spring based on classpath
 * - @ComponentScan: scans all packages under com.mentorhub for components
 *
 * When you run this class, Spring Boot starts an embedded Tomcat server on port 8080.
 */
@SpringBootApplication
public class MentorHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(MentorHubApplication.class, args);
        System.out.println("========================================");
        System.out.println("  MentR Backend Started!");
        System.out.println("  API running at: http://localhost:8081");
        System.out.println("========================================");
    }
}
