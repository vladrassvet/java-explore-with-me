package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = {"ru.practicum.*", "stats-client"})

public class MainService {
    public static void main(String[] args) {
        SpringApplication.run(MainService.class, args);
    }
}
