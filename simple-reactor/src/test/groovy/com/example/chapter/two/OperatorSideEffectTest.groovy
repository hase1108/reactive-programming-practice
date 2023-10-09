package com.example.chapter.two

import com.example.chapter.one.FluxSample
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import spock.lang.Specification

class OperatorSideEffectTest extends Specification {

    def fluxSample = new FluxSample()

    def "logのテスト: logがどのような要素を出力しているか"() {

        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.log()
            .map(i -> {
                System.out.println("mapped : " + i)
                return "String : " + i
            })
                .subscribe(value -> System.out.println(value))

        then:
        true
    }

    def "doOnNext: 副作用を伴う処理"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux
                .doOnNext(i -> {
                    System.out.println("mapped : " + i)
                })
                .subscribe(value -> System.out.println(value))

        then:
        true
    }

    def "doOnNext: 副作用を伴う処理2"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux
                .map(i -> {System.out.println(i)
                return i})
                .doOnNext(i -> {
                    Thread.sleep(3000L)
                    System.out.println("doOnNext1 : " + i)
                })
                .doOnNext(i -> {
                    System.out.println("doOnNext2 : " + i)
                })
                .map(i -> {System.out.println(i)
                    return i})
                .subscribe(value -> System.out.println(value))

        then:
        true
    }

    def "doOnNext: 副作用を伴う処理3"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux

                .doOnNext(i -> {
                    if(i == 3){
                        throw new RuntimeException("test")
                    }
                }).onErrorContinue((e, i) -> System.out.println(e.getMessage() + " : " +i))
                .doOnNext(i -> {
                    System.out.println("doOnNext2 : " + i)
                })
                .map(i -> {System.out.println(i)
                    return i})
                .subscribe(value -> System.out.println(value),
                e -> System.out.println(e.getMessage()))

        then:
        true
    }

    def "doOnNext: 副作用を伴う処理4"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux

                .map(i -> {
                    if(i == 3){
                        throw new RuntimeException("test")
                    }
                    return i
                }).onErrorContinue((e, i) -> System.out.println(e.getMessage() + " : " +i))
                .map(i -> {System.out.println(i)
                    return i})
                .subscribe(value -> System.out.println(value),
                        e -> System.out.println(e.getMessage()))

        then:
        true
    }

    def "doOnNext: 副作用を伴う処理5"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux

                .flatMap(i -> {
                    if(i == 3){
                        throw new RuntimeException("test")
                    }
                    return Mono.just(i)
                }).onErrorContinue((e, i) -> System.out.println(e.getMessage() + " : " +i))
                .map(i -> {System.out.println(i)
                    return i})
                .subscribe(value -> System.out.println(value),
                        e -> System.out.println(e.getMessage()))

        then:
        true
    }

    def "doOnNext: 副作用を伴う処理 外部ファイルへの書き込みが非同期に実行され成功する"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux
                .doOnNext(i -> {
                    try (FileWriter writer = new FileWriter("output.txt", true)) {
                        System.out.println("Value : " + i + " Thread : " + Thread.currentThread().getName())
                        writer.write(i + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .doOnNext(i -> {
                    System.out.println("Value : " + i + " Thread : " + Thread.currentThread().getName())
                    System.out.println("mapped : " + i)
                    return "String : " + i
                })
                .subscribe(value -> {
                    System.out.println(value)
                    System.out.println("Value : " + value +" Thread : " + Thread.currentThread().getName())})

        then:
        true
    }
    def "doOnNext: 副作用を伴う処理 外部ファイルへの書き込みが失敗する"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux
                .map(i -> {
                    try (FileWriter writer = new FileWriter("output.txt", true)) {
                        System.out.println("Value : " + i + " Thread : " + Thread.currentThread().getName())
                        writer.write(i + "\n");
                        return i
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .map(i -> {
                    System.out.println("mapped : " + i)
                    System.out.println("Value : " + i + " Thread : " + Thread.currentThread().getName())
                    return "String : " + i
                })
                .subscribe(value -> {
                    System.out.println(value)
                    System.out.println("Value : " + value + " Thread : " + Thread.currentThread().getName())
                })

        then:
        true
    }

    def "doOnNext: 副作用を伴う処理 外部ファイルへの書き込みが"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux
                .flatMap(i -> {
                    try (FileWriter writer = new FileWriter("output.txt", true)) {
                        System.out.println("Value : " + i + " Thread : " + Thread.currentThread().getName())
                        writer.write(i + "\n")
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .map(i -> {
                    System.out.println("mapped : " + i)
                    System.out.println("Value : " + i + " Thread : " + Thread.currentThread().getName())
                    return "String : " + i
                })
                .subscribeOn(Schedulers.parallel())
                .subscribe(value -> {
                    System.out.println(value)
                    System.out.println("Value : " + value + " Thread : " + Thread.currentThread().getName())
                })

        System.out.println("Thread : " + Thread.currentThread().getName())
        Thread.sleep(10000L)
        then:
        true
    }
}