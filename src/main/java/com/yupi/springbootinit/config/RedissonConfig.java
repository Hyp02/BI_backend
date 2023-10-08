package com.yupi.springbootinit.config;

import com.qcloud.cos.model.ciModel.auditing.Conf;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Han
 * @data 2023/10/8
 * @apiNode
 */
@ConfigurationProperties("spring.redis")
@Configuration
@Data
public class RedissonConfig {

    private Integer database;
    private String host;
    private Integer port;
    private String password;

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://"+host+":"+port)
                .setPassword(password)
                .setDatabase(database);
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }


}
