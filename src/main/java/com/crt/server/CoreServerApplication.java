package com.crt.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoreServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreServerApplication.class, args);
        System.out.println("Core Server Started, on http://localhost:8080");
        System.out.println("WebSocket endpoint available at: ws://localhost:8080/ws");
    }

}
