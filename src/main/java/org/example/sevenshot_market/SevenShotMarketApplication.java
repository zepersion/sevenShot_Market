package org.example.sevenshot_market;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.sevenshot_market.mapper")
public class SevenShotMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(SevenShotMarketApplication.class, args);
    }

}
