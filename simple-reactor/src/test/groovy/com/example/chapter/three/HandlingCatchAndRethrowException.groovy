package com.example.chapter.three

import com.example.OriginalException
import com.example.chapter.one.FluxSample
import com.example.chapter.three.WhenInputThreeThrowException
import reactor.core.publisher.Flux
import spock.lang.Specification

class HandlingCatchAndRethrowException extends Specification{
    def fluxSample = new FluxSample()


    def "CatchAndRethrow : onErrorResumeで任意の例外にマッピングする"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.map(WhenInputThreeThrowException::throwExceptionWhenThree)
                .onErrorResume(e -> Flux.error( new OriginalException("Original")))
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()),
                        () -> System.out.println("Subscribe Finish"))

        then:
        true
    }

    def "CatchAndRethrow : onErrorMapで任意の例外にマッピングする"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.map(WhenInputThreeThrowException::throwExceptionWhenThree)
                .onErrorMap(e -> new OriginalException("Original"))
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()),
                        () -> System.out.println("Subscribe Finish"))

        then:
        true
    }

    def "CatchAndRethrow : onErrorMapでflatMapで生じたエラーをハンドリングする"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i ->{
            WhenInputThreeThrowException.throwExceptionWhenThreeFlux(i)
        }).onErrorMap(e ->  new OriginalException("Original"))
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }


    def "CatchAndRethrow : 内部ストリームのハンドリング"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i ->{
            WhenInputThreeThrowException.throwExceptionWhenThreeWrapByFlux(i).onErrorMap(e ->  new OriginalException("Original"))
        })
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()),
                        () -> System.out.println("Subscribe Finish"))

        then:
        true
    }


}
