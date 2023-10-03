package com.example.chapter.one

import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

class FluxSampleTest extends Specification {

    def fluxSample = new FluxSample()

    def "Fluxが期待したrangeのデータストリームを供給していること"(){
        given:
        def num = 3

        when:
        def source = fluxSample.simpleFlux(num)

        then:
        StepVerifier.create(source)
            .expectNext(0)
            .expectNext(1)
            .expectNext(2)
            .verifyComplete()
    }


    def "データストリームの各要素毎に任意の処理を実行できる(onNext)"(){
        given:
        def num = 3

        when:
        def source = fluxSample.simpleFlux(num).subscribe(value -> System.out.printf("OnNext %d", value))

        then:
        true
    }

    def "データストリームでエラーが発生した時に任意の処理を実行できる(onError)"(){
        given:
        def num = 3

        when:
        def source = fluxSample.simpleFlux(num)
                                    .map(i -> {
                                        if(i == 2){
                                            throw new RuntimeException("error occured")
                                        }
                                        return i
                                    })
                                    .subscribe(value -> System.out.printf("OnNext %d", value),
                                    error -> System.out.printf("onError %s", error.getMessage()))

        then:
        true
    }

    def "データストリームで値を全て処理した場合に任意の処理が実施できる(onComplete)"(){
        given:
        def num = 3

        when:
        def source = fluxSample.simpleFlux(num)
                .subscribe(value -> System.out.printf("OnNext %d", value),
                        error -> System.out.printf("onError %s", error.getMessage()),
                        () -> System.out.printf("onComplete stream finished"))

        then:
        true
    }

    def "任意のSubscriberで処理を実施できること"(){
        given:
        def num = 3

        when:
        def source = fluxSample.simpleFlux(num)
                .subscribeWith(new SampleSubscriber())

        then:
        true
    }

    def "PublisherはSubscribeされない限りデータストリームを流し始めないこと"(){
        given:
        def num = 3

        when:
        def source = fluxSample.simpleFlux(num)
                .map(i -> {
                    System.out.printf("DataStream %d", i)
                    return i
                })

        then:
        true

    }

    def "PublisherはSubscribeされないoperatoの内側のPublisherは外側のSubscribeに関係しない"(){
        given:
        def num = 3

        when:
        fluxSample.simpleFlux(num)
                .map(i -> {
                    Mono.just(1).doOnNext(value -> System.out.println("Mono : " + value))
                    return i
                }).subscribe(value -> System.out.printf("OnNext %d", value))

        then:
        true

    }

}