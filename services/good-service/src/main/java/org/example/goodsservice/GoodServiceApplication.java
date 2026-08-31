package org.example.goodsservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@EnableScheduling
@EnableDiscoveryClient
@MapperScan("org.example.goodsservice.mapper")
@SpringBootApplication(scanBasePackages = {"org.example.goodsservice", "org.example.common"})
public class GoodServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoodServiceApplication.class, args);
    }
}
