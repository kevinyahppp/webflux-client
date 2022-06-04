package com.webfluxclient;

import com.webfluxclient.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {
    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler productHandler) {
        return RouterFunctions.route(RequestPredicates.GET("/api/client"), productHandler::list)
                .andRoute(RequestPredicates.GET("/api/client/{id}"), productHandler::view)
                .andRoute(RequestPredicates.POST("/api/client"), productHandler::create)
                .andRoute(RequestPredicates.PUT("/api/client/{id}"), productHandler::edit)
                .andRoute(RequestPredicates.DELETE("/api/client/{id}"), productHandler::delete)
                .andRoute(RequestPredicates.POST("/api/client/upload/{id}"), productHandler::upload);
    }
}
