package com.shiksha;

import org.springframework.boot.SpringApplication;

public class TestShikshaLmsApplication {

    public static void main(String[] args) {
        SpringApplication.from(ShikshaLmsApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
