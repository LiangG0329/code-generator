package com.code.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

/**
 * @author Liang
 * @create 2024/3/6
 */
@Configuration
public class HttpSessionConfig {

    /**
     *  解决redis集群环境没有开启Keyspace notifications导致的
     *  Error creating bean with name 'enableRedisKeyspaceNotificationsInitializer' defined in class path resource
     * */
    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }
}