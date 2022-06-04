package com.webfluxclient.models.services;

import com.webfluxclient.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private WebClient.Builder webClient;
    @Override
    public Flux<Product> findAll() {
        return webClient.build().get().accept(MediaType.APPLICATION_JSON)
//                .exchange().flatMapMany(clientResponse ->
//                        clientResponse.bodyToFlux(Product.class));
                .exchangeToFlux(clientResponse -> clientResponse.bodyToFlux(Product.class));
    }

    @Override
    public Mono<Product> findById(String id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return webClient.build().get().uri("/{id}", params)
                .accept(MediaType.APPLICATION_JSON)
//                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(Product.class));
                .retrieve().bodyToMono(Product.class);
    }

    @Override
    public Mono<Product> save(Product product) {
        return webClient.build().post()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(product))
                .retrieve().bodyToMono(Product.class);
    }

    @Override
    public Mono<Product> edit(Product product, String id) {
        return webClient.build().put()
                .uri("/{id}", Collections.singletonMap("id", id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(product))
                .retrieve().bodyToMono(Product.class);
    }

    @Override
    public Mono<Void> delete(String id) {
        return webClient.build().delete()
                .uri("/{id}", Collections.singletonMap("id", id))
                .retrieve().bodyToMono(Void.class).then();
    }

    @Override
    public Mono<Product> upload(FilePart filePart, String id) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.asyncPart("filePart", filePart.content(), DataBuffer.class).headers(httpHeaders -> {
           httpHeaders.setContentDispositionFormData("filePart", filePart.filename());
        });
        return webClient.build().post()
                .uri("/upload/{id}", Collections.singletonMap("id", id))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromValue(bodyBuilder.build()))
                .retrieve().bodyToMono(Product.class);
    }
}
