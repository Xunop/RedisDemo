package com.example.redisdemo;

import com.example.redisdemo.pojo.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class RedisDemoApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        connection.flushDb();
        redisTemplate.opsForValue().set("key1", "Test1");
        stringRedisTemplate.opsForValue().set("key2", "Test2");
    }

    @Test
    void test1() throws JsonProcessingException {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        connection.flushDb();
        User user = new User("xxx", 88);
//        String jsonUser = new ObjectMapper().writeValueAsString(user);
        redisTemplate.opsForValue().set("user", user);
        System.out.println(redisTemplate.opsForValue().get("user"));
    }
}
