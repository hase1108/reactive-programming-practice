package com.example;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;

public class SampleReactor {

    public static void main(String[] args) {
        Mono.just(5)
                .flatMap(SampleReactor::processNumber)
                .doOnSuccess(System.out::println)
                .onErrorContinue((error, value) ->{
                    System.out.println("test");
                })
                .subscribe(System.out::println,
                        error -> System.out.println("ERROR " + error.getMessage()));
    }

    public static Mono<Integer> processNumber(int num) {
        if (num == 5) {
            return Mono.error(new RuntimeException("Error occurred for number 5."));
        }
        return Mono.just(num);
    }
}
