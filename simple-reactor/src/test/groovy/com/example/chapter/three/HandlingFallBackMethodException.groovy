package com.example.chapter.three

import com.example.chapter.one.FluxSample
import com.example.chapter.three.WhenInputThreeThrowException
import reactor.core.publisher.Flux
import spock.lang.Specification

class HandlingFallBackMethodException extends Specification{
    def fluxSample = new FluxSample()

    def "fall back method : mapでfallbackメソッドによって値が送出される"(){
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

    def "fall back method : flatMapでfallbackメソッドによって値が送出される"(){
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

    /*
    onErrorCompleteなどと同様内部ストリームでのハンドリングも可能
    返り値などに注意すること
    onErrorReturnなどと同様にPredicateなどを引数に取り、例外のパターンによってハンドリングするメソッドを変えることもできる
     */
    def "static fall back : 内部ストリームでハンドリングをするパターン"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i -> WhenInputThreeThrowException.throwExceptionWhenThreeWrapByFlux(i)
                .onErrorResume(e -> Flux.just("fall back : " + i))
        )
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }


}
