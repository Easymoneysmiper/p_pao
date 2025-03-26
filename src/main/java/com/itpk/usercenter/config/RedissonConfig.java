package com.itpk.usercenter.config;


import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spring.redis",name = "host") // 明确检查host属性是否存在
@Data
public class RedissonConfig {

    @Value("${spring.redis.host}") // 确保配置中存在spring.redis.host
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    @Bean
    public RedissonClient redisson() {
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer()
                .setAddress(redisAddress)
                .setPassword("pkpkpk2005")
                .setDatabase(3);
        return Redisson.create(config);
    }
}
