package com.example.chapter.three

import com.example.OriginalException
import com.example.OriginalException2
import com.example.chapter.one.FluxSample
import com.example.chapter.three.WhenInputThreeThrowException
import reactor.core.publisher.Flux
import spock.lang.Specification

class HandlingStaticFallBackException extends Specification{
    def fluxSample = new FluxSample()

    /*
    onErrorReturnではエラー発生時に静的な値をフォールバックすることができる。
    ただし、大元のデータストリームは従来通りにエラーが発生した時点で終了する。
    またonErrorによるハンドリングは行われなくなる
     */
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

    def "static fall back : 例外クラスを引数にとる"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i ->{
            WhenInputThreeThrowException.throwExceptionWhenThreeFlux(i)

        }).onErrorReturn(RuntimeException.class,"RECOVERED")
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }

    def "static fall back : predicateを引数にとる"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i ->{
            WhenInputThreeThrowException.throwExceptionWhenThreeFlux(i)

        }).onErrorReturn(error -> error.class == RuntimeException.class,"RECOVERED")
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }

    def "static fall back : 複数のOnErrorReturnでハンドリングできる"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i ->{
            WhenInputThreeThrowException.throwOriginalExceptionWhenThree(i)

        }).onErrorReturn(error -> error.class == OriginalException.class,"OriginalException")
                .onErrorReturn(error -> error.class == OriginalException2.class,"OriginalException2")
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }

    /*
    FlatMapの場合、内部でそれぞれデータストリームが発生しているので、直接その内部ストリームをハンドリングすることもできる
    ここで注意が必要なのが、2点

    1.内部ストリームを直接ハンドリングした場合は、大元のデータストリームは終了しない
 　　これはFlatMapの仕組みを考えるとわかりやすい

    2.PublisherでWrapしてないエラーはハンドリングできない
     */
    def "static fall back : 内部ストリームでハンドリングした場合は大元のデータストリームが中断しない"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i ->{
            WhenInputThreeThrowException.throwExceptionWhenThreeWrapByFlux(i)
                    .onErrorReturn("RECOVERED")
        })
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }

    def "static fall back : Publisherでwrapしていないエラーはハンドリングできない"(){
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
