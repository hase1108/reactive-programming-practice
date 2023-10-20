package com.example.chapter.three

import reactor.core.publisher.Flux
import spock.lang.Specification

import java.time.Duration

class RetryException extends Specification{

    def "retry: リトライを1回実施する"(){
        when:
        Flux.interval(Duration.ofMillis(250))
                .map(input -> {
                    WhenInputThreeThrowException.throwExceptionWhenThree((int)input)
                })
                .retry(1)
                .elapsed() // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#elapsed--
                .subscribe(System.out::println, System.err::println)

        Thread.sleep(2100)

        then:
        true
    }

    def "retry: リトライを1回実施する２"(){
        when:
        Flux.interval(Duration.ofMillis(250))
                .flatMap(input -> {
                    WhenInputThreeThrowException.throwExceptionWhenThreeFlux((int)input)
                })
                .retry(1)
                .elapsed() // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#elapsed--
                .subscribe(System.out::println, System.err::println)

        Thread.sleep(2100)

        then:
        true
    }
}
