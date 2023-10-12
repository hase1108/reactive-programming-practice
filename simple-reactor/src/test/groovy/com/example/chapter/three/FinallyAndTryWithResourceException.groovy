package com.example.chapter.three

import com.example.chapter.one.FluxSample
import reactor.core.publisher.Flux
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class FinallyAndTryWithResourceException extends Specification{
    def fluxSample = new FluxSample()

    def "finally : 正常終了時の最後に何らかの処理を実施する"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.map(i ->"number " + i)
                .doFinally( type -> System.out.println("Type: " + type.toString()))
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()),
                            System.out.println("Subscribed"))

        then:
        true
    }

    def "finally : 異常終了時の最後に何らかの処理を実施する"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.map(i ->WhenInputThreeThrowException.throwExceptionWhenThree(i))
                .doFinally( type -> System.out.println("Type: " + type.toString()))
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()),
                        System.out.println("Subscribed"))

        then:
        true
    }


    def "using : flatMapでエラーをログに出力するがシーケンス自体に変更は加えない"(){
        when:
        Flux.using(() -> Files.lines(Paths.get("E:/workspace/reactive-programming-practice/.gitignore")),
                Flux::fromStream,
                s -> {
                    System.out.println("closing")
                    s.close()
                })
        .map(i -> {
            System.out.println(i)
            return i
        })
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }


}
