package com.BO.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정
 */
@Configuration
public class RedisConfig {

    // Redis host 설정
    @Value("${spring.data.redis.host}")
    private String localhost = "localhost";

    // 현재 Redis 도커로 3307:3306으로 작동 중 → 포트 번호 관련 설정은 application.properties에 설정되어 있음.
    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // application.properties에서 host, port 읽어서 설정
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(localhost, port);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();

        // Redis 연결 수행
        redisTemplate.setConnectionFactory(connectionFactory);

        // Key-Value 형태의 String으로 직렬화
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        // Hash Key-Value 형태의 String으로 직렬화
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
