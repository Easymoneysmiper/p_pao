package com.itpk.usercenter.service;
import java.util.Date;

import com.itpk.usercenter.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    public void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();

        valueOperations.set("name", "pkpk");
        valueOperations.set("int", 1);
        valueOperations.set("double", 2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("ppkk");
        valueOperations.set("user", user);
        Assertions.assertTrue(valueOperations.get("name").equals("pkpk"));
        Assertions.assertTrue(valueOperations.get("int").equals(1));
        Assertions.assertTrue(valueOperations.get("double").equals(2.0));
        Assertions.assertTrue(valueOperations.get("user").equals(user));

    }
}
