package org.example.rewardservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableDiscoveryClient
@MapperScan("org.example.rewardservice.mapper")
@SpringBootApplication(scanBasePackages = {"org.example.rewardservice", "org.example.common"})
public class RewardServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RewardServiceApplication.class, args);
    }
}
