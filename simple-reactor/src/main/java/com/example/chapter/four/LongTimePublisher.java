package com.example.chapter.four;

import reactor.core.publisher.Flux;

import java.time.Duration;

public class LongTimePublisher {

    public static Flux<Integer> getLongTimePublisher(int elements,long seconds){
        return Flux.range(0,elements).delayElements(Duration.ofSeconds(seconds));
    }

    public static Flux<Integer> getLongDaysPublisher(int elements){
        return Flux.range(0,elements).delayElements(Duration.ofDays(1L));
    }
}
