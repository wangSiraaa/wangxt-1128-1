package com.ad.schedule;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ad.schedule.mapper")
public class AdScheduleApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdScheduleApplication.class, args);
    }
}
