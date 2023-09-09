package com.example.chapter.zero;

import reactor.core.publisher.Flux;

public class CheckTreadSample {

    public void checkThreadNormal(int range){
        Flux.range(0,range)
                .map(value -> {
                    System.out.println("Now : " + value + " Thread : " + Thread.currentThread().getName());
                    return value;
                }).subscribe(value -> {
                    System.out.println("Now : " + value + " Thread : " + Thread.currentThread().getName());
                });
    }

    public void checkNestedThreadSample(int range){
        Flux.range(0,range)
                .map(value -> {
                    System.out.println("Now 1 : " + value + " Thread : " + Thread.currentThread().getName());
                    return value;
                }).subscribe(value -> {
                    System.out.println("Now 2 : " + value + " Thread : " + Thread.currentThread().getName());
                    Flux.range(0, value).subscribe(v -> {
                        System.out.println("Now 3 : " + v + " Thread : " + Thread.currentThread().getName());
                    });
                });

    }
}
