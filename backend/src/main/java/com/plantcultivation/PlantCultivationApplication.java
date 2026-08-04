package com.plantcultivation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class PlantCultivationApplication {

    public static void main(String[] args) {
        // 统一 JVM 默认时区，与 JDBC URL 的 serverTimezone=Asia/Shanghai 一致，
        // 避免服务器 JVM 为 UTC 时 LocalDateTime 时间戳偏移 8 小时
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(PlantCultivationApplication.class, args);
    }
}
