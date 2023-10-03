package com.example.chapter.three

import com.example.chapter.one.FluxSample
import reactor.core.publisher.Flux
import spock.lang.Specification

class HandlingFallBackMethodException extends Specification{
    def fluxSample = new FluxSample()

    def "fall back method : mapでfallbackメソッドによって値が送出される(あまり意味がない)"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.map(i ->{
            WhenInputThreeThrowException.throwExceptionWhenThree(i)
        }).onErrorResume(e -> "RECOVERED")
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }

    def "fall back method : flatMapでfallbackメソッドによって値が送出される(あまり意味がない)"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i ->{
            WhenInputThreeThrowException.throwExceptionWhenThreeFlux(i)
        }).onErrorResume(e -> Flux.just("RECOVERED"))
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }

    def "static fall back : fallbackされないパターン"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i -> WhenInputThreeThrowException.throwExceptionWhenThreeMono(i).onErrorResume(e -> "fall back : " + i)
        )
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }


}
