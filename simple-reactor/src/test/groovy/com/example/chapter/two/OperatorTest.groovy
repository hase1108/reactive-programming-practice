package com.example.chapter.two

import com.example.chapter.one.FluxSample
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.Duration

class OperatorTest extends Specification{

    def fluxSample = new FluxSample()

    def "mapのテスト"(){

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

    def "flatMaprのテスト"(){

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

    def "flatMaprのテスト2"(){

        given:

        def flux = fluxSample.simpleFlux(100)

        when:
        flux.flatMap(i -> {
            System.out.println("mapped : " + i)
            return Flux.just("String : " + i, String.valueOf(i))
        })
                .subscribe(value -> System.out.println(value))
        then:
        true
    }

    def "flatMaprのテスト3"(){

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

    def "flatMaprのテスト4"(){

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

    def "flatMaprのテスト5"(){

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
