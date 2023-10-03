package com.example.chapter.two

import com.example.chapter.one.FluxSample
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.Duration

class OperatorTest extends Specification{

    def fluxSample = new FluxSample()

    def "mapのテスト: mapが要素を1:1で変換していること"(){

        given:

        def flux = fluxSample.simpleFlux(100)

        when:
        flux.map(i -> {
            System.out.println("mapped : " + i)
            return "String : " + i
        })
        .subscribe(value -> System.out.println(value))

        then:
        true
    }

    def "flatMaprのテスト: flatmapが要素を1:1で変換していること"(){

        given:

        def flux = fluxSample.simpleFlux(100)

        when:
        flux.flatMap(i -> {
            System.out.println("mapped : " + i)
            return Mono.just("String : " + i)
        })
                .subscribe(value -> System.out.println(value))

        then:
        true
    }

    def "flatMaprのテスト2: Fluxが要素を1:2で変換していること"(){

        given:

        def flux = fluxSample.simpleFlux(10)

        when:
        flux.flatMap(i -> {
            System.out.println("mapped : " + i)
            return Flux.range(0, i)
        })
                .subscribe(value -> System.out.println(value))
        then:
        true
    }

    def "flatMaprのテスト3: FlatMapの順序性について"(){

        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i -> {
            System.out.println("mapped : " + i)
            return Flux.just("String : " + i, String.valueOf(i)).delayElements(Duration.ofSeconds(1))
        })
                .subscribe(value -> System.out.println(value))
        try {
            Thread.sleep(6000); // データが送信されるのを待つ
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        then:
        true
    }

    def "concatMapのテスト: データストリームの入力の順番毎に全ての処理が実行されること"(){

        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.concatMap(i -> {
            System.out.println("mapped : " + i)
            return Flux.just("String : " + i, String.valueOf(i)).delayElements(Duration.ofMillis(100))
        })
                .subscribe(value -> System.out.println(value))
        try {
            Thread.sleep(8000); // データが送信されるのを待つ
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        then:
        true
    }

    def "flatMapSequentialのテスト:各operatorの処理がデータストリームの入力順になること"(){

        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMapSequential(i -> {
            System.out.println("mapped : " + i)
            return Flux.just("String : " + i, String.valueOf(i)).delayElements(Duration.ofMillis(100))
        })
                .subscribe(value -> System.out.println(value))
        try {
            Thread.sleep(8000); // データが送信されるのを待つ
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        then:
        true
    }
}
