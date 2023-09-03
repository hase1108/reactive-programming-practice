package com.example.chapter.one;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MonoSample {

    public Mono<Integer> simpleMono(int num){
        return Mono.just(num);
    }

    public Mono<Integer> emptyMono(int num){
        return Mono.empty();
    }
}
