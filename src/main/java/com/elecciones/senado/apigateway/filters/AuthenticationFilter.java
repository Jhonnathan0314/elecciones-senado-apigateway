package com.elecciones.senado.apigateway.filters;

import com.elecciones.senado.apigateway.config.VariablesConfig;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<VariablesConfig> {

    private final WebClient webClient;

    public AuthenticationFilter(WebClient.Builder webClientBuilder) {
        super(VariablesConfig.class);
        this.webClient = webClientBuilder.build();
    }

    @Override
    public GatewayFilter apply(VariablesConfig config) {
        return (exchange, chain) -> validateToken(exchange, config.getValidationUrl())
                .flatMap(isValidToken -> {
                    if (!isValidToken) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                });
    }

    private Mono<Boolean> validateToken(ServerWebExchange exchange, String validationUrl) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        return this.webClient.get()
                .uri(validationUrl)
                .header("Authorization", token)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().equals(HttpStatus.OK))
                .onErrorResume(e -> Mono.just(false));
    }

}
