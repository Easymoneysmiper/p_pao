package com.itpk.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.itpk.usercenter.mapper")
@EnableScheduling
public class P_pao_backendApplication {

    public static void main(String[] args) {
        SpringApplication.run(P_pao_backendApplication.class, args);
    }

}
