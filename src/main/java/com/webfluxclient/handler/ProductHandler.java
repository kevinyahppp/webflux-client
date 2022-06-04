package com.webfluxclient.handler;

import com.webfluxclient.models.Product;
import com.webfluxclient.models.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProductHandler {
    @Autowired
    private ProductService productService;
    public Mono<ServerResponse> list(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(productService.findAll(), Product.class);
    }

    public Mono<ServerResponse> view(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return error(productService.findById(id)
                .flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(product)))
                .switchIfEmpty(ServerResponse.notFound().build())
                );
    }

    public Mono<ServerResponse> create(ServerRequest serverRequest) {
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);
        return productMono.flatMap(product -> {
            if (product.getCreateAt() == null) {
                product.setCreateAt(new Date());
            }
            return productService.save(product);
        }).flatMap(product -> ServerResponse.created(URI.create("/api/client/"
                        .concat(product.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(product)))
                .onErrorResume(throwable -> {
                    WebClientResponseException responseException = (WebClientResponseException) throwable;
                    if (responseException.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(responseException.getResponseBodyAsString()));
                    }
                    return Mono.error(responseException);
                });
    }

    public Mono<ServerResponse> edit(ServerRequest serverRequest) {
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);
        String id = serverRequest.pathVariable("id");
        return  error(productMono.flatMap(product -> productService.edit(product, id))
                .flatMap(product -> ServerResponse.created(URI.create("/api/client/"
                        .concat(product.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(product)))
               );
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return error(productService.delete(id).then(ServerResponse.noContent().build()));
    }

    public Mono<ServerResponse> upload(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return error(serverRequest.multipartData().map(multipart -> multipart.toSingleValueMap().get("filePart"))
                .cast(FilePart.class)
                .flatMap(filePart -> productService.upload(filePart, id))
                .flatMap(product -> ServerResponse.created(URI.create("/api/client/".concat(product.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(product)))
                );
    }

    private Mono<ServerResponse> error(Mono<ServerResponse> responseMono) {
        return responseMono.onErrorResume(throwable -> {
            WebClientResponseException responseException = (WebClientResponseException) throwable;
            if (responseException.getStatusCode() == HttpStatus.NOT_FOUND) {
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("error", "Product not exist: ".concat(responseException.getMessage()));
                objectMap.put("timestamp", new Date());
                objectMap.put("status", responseException.getStatusCode().value());
//                return ServerResponse.status(HttpStatus.NOT_FOUND).syncBody(objectMap);
                return ServerResponse.status(HttpStatus.NOT_FOUND).body(BodyInserters.fromValue(objectMap));
            }
            return Mono.error(responseException);
        });
    }
}
