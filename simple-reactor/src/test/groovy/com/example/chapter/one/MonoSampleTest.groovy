package com.example.chapter.one

import reactor.test.StepVerifier
import spock.lang.Specification

class MonoSampleTest extends Specification {

    def monoSample = new MonoSample()

    def "Monoがデータ数1つのデータストリームを供給していること"(){
        given:
        def num = 3

        when:
        def source = monoSample.simpleMono(num)

        then:
        StepVerifier.create(source)
            .expectNext(3)
            .verifyComplete()
    }


    def "データストリームの各要素毎に任意の処理を実行できる(onNext)"(){
        given:
        def num = 3

        when:
        def source = monoSample.simpleMono(num).subscribe(value -> System.out.printf("OnNext %d", value))

        then:
        true
    }

    def "データストリームでエラーが発生した時に任意の処理を実行できる(onError)"(){
        given:
        def num = 3

        when:
        def source = monoSample.simpleMono(num)
                                    .map(i -> {
                                        if(i == 3){
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
        def source = monoSample.simpleMono(num)
                .subscribe(value -> System.out.printf("OnNext %d", value),
                        error -> System.out.printf("onError %s", error.getMessage()),
                        () -> System.out.printf("onComplete stream finished"))

        then:
        true
    }

    def "データストリーム開始時に任意の処理が実施できる(onSubscribe)"(){
        given:
        def num = 3

        when:
        // Fluxではdeprecated
        def source = monoSample.simpleMono(num)
                .subscribe(value -> System.out.printf("OnNext %d", value),
                        error -> System.out.printf("onError %s", error.getMessage()),
                        () -> System.out.printf("onComplete stream finished"),
                        subscription -> {
                            System.out.print("onSubscribe subscribe Start")
                            subscription.request(1L)
                        })

        then:
        true
    }

    def "任意のSubscriberで処理を実施できること"(){
        given:
        def num = 3

        when:
        def source = monoSample.simpleMono(num)
                .subscribeWith(new SampleSubscriber())

        then:
        true
    }

    def "PublisherはSubscribeされない限りデータストリームを流し始めないこと"(){
        given:
        def num = 3

        when:
        def source = monoSample.simpleMono(num)
                .map(i -> {
                    System.out.printf("DataStream %d", i)
                    return i
                })


        then:
        true

    }
}