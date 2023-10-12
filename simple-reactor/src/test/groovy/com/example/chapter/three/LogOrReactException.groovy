package com.example.chapter.three

import com.example.chapter.one.FluxSample
import reactor.core.publisher.Flux
import spock.lang.Specification

class LogOrReactException extends Specification{
    def fluxSample = new FluxSample()

    def "log or react : mapでエラーをログに出力するがシーケンス自体に変更は加えない"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.map(i ->{
            WhenInputThreeThrowException.throwExceptionWhenThree(i)
        }).doOnError(e -> System.out.println("Logged"))
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }

    def "log or react : flatMapでエラーをログに出力するがシーケンス自体に変更は加えない"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i ->{
            WhenInputThreeThrowException.throwExceptionWhenThreeFlux(i)
        }).doOnError(e -> System.out.println("Logged"))
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }


    def "log or react : 内部ストリームでハンドリングをするパターン"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i -> WhenInputThreeThrowException.throwExceptionWhenThreeWrapByFlux(i)
                .doOnError(e -> System.out.println("Logged"))
        )
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }

    def "log or react : シーケンスの変更自体は別のoperatorでハンドリングするパターン"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i -> WhenInputThreeThrowException.throwExceptionWhenThreeWrapByFlux(i)
                .doOnError(e -> System.out.println("Logged"))
                .onErrorResume(e -> Flux.just("fall back : " + i))
        )
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }



}
