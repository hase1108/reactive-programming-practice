package com.example.chapter.three

import com.example.chapter.one.FluxSample
import reactor.core.publisher.Flux
import spock.lang.Specification

class HandlingStaticFallBackException extends Specification{
    def fluxSample = new FluxSample()

    def "static fall back : mapでfallbackメソッドによって値が送出される"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.map(WhenInputThreeThrowException::throwExceptionWhenThree)
                .onErrorReturn("RECOVERED")
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }

    def "static fall back : flatMapでfallbackメソッドによって値が送出される"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i ->{
            WhenInputThreeThrowException.throwExceptionWhenThreeFlux(i)
        }).onErrorReturn("RECOVERED")
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }

    def "static fall back : flatMapでfallbackメソッドによって値が送出される2"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i ->{
            WhenInputThreeThrowException.throwExceptionWhenThreeFlux(i)
                    .onErrorReturn("RECOVERED")
        })
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }


}
