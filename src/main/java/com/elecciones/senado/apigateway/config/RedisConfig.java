package com.elecciones.senado.apigateway.config;

import com.elecciones.senado.apigateway.constants.ErrorMessages;
import com.elecciones.senado.apigateway.exceptions.InvalidHeaderException;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class RedisConfig {

    private final ErrorMessages errorMessages = new ErrorMessages();

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("server", 6379));
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate() {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            try {
                return Mono.just(validateSecretHeader(exchange));
            } catch (InvalidHeaderException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private String validateSecretHeader(ServerWebExchange exchange) throws InvalidHeaderException {
        String secretHeader = System.getenv("SECRET_HEADER");
        if(exchange.getRequest().getHeaders().getFirst(secretHeader) != null) return "";
        throw new InvalidHeaderException(errorMessages.INVALID_HEADER_ERROR);
    }
}

