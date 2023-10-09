package com.example.chapter.three

import com.example.chapter.one.FluxSample
import spock.lang.Specification

class HandlingTryCatchException extends Specification{

    def fluxSample = new FluxSample()

    /*
    3が流れてくるとRuntimeExceptionが発生する
    本来は1,2,3,4,5と流れる事が期待されるが、3が流れてくるとデータストリームが終了し、onErrorで定義されているメソッドが実行される。
    onCompleteは実行されない
     */
    def "try-catch : 3以降のデータは流れない"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.map(WhenInputThreeThrowException::throwExceptionWhenThree)
        .subscribe(value -> System.out.println(value),
        error -> System.out.println("ERROR " + error.getMessage()),
                () -> System.out.println("Subscribe Finish"))

        then:
        true
    }

    /*
    FlatMapでも同様の動きをする
     */
    def "try-catch : 3以降のデータは流れない2"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(WhenInputThreeThrowException::throwExceptionWhenThreeFlux)
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()),
                        () -> System.out.println("Subscribe Finish"))

        then:
        true
    }

    /*
    エラーの投げ方としては、直接例外を投げる場合とPublisherでWrapするケースがある。
    今回の場合はどちらでも同様の動きになるが、場合によっては挙動が異なるので注意
     */
    def "try-catch : 3以降のデータは流れない3"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(WhenInputThreeThrowException::throwExceptionWhenThreeWrapByFlux)
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()),
                        () -> System.out.println("Subscribe Finish"))

        then:
        true
    }
}
