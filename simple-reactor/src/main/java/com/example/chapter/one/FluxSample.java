package com.example.chapter.one;

import reactor.core.publisher.Flux;

public class FluxSample {

    public Flux<Integer> simpleFlux(int num){
        return Flux.range(0, num);
    }

    public Flux<Integer> emptyFlux(){
        return Flux.empty();
    }
}
