package com.example.chapter.five

import com.example.chapter.one.FluxSample
import reactor.core.publisher.BufferOverflowStrategy
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.Duration

class BackPressureTest extends Specification{

    def fluxSample = new FluxSample()

    def "subscribe時に無制限モードでリクエストするのでrequestシグナルが送信されない"(){

        when:
                // 0から9までデータを流す
                fluxSample.simpleFlux(10)
                .log()
                // Publisherから溢れてしまった場合にここに到達する
                .onBackpressureDrop(t -> System.out.println("dropped : " + t))
                .blockLast()


        then:
        true
    }

    def "concatMapがリクエストシグナルを送信する(concatMap側がbackpressureを調節している)"(){

        when:
        fluxSample.simpleFlux(10)
                .log()
                // 処理のボトルネック
                // 内部のPublisherと非同期に発行された要素をフラット化する
                .concatMap(x -> {
                    // onNextのシグナルが到達してからDurationで指定した時間分送らせる
                    Mono.delay(Duration.ofMillis(100))
                })
                .blockLast()
        then:
        true
    }

    def "requestを無視して強制的にデータを流すケース"(){

        when:
                Flux.interval(Duration.ofMillis(1)).take(10)
                        .log()
                // 処理のボトルネック
                // 内部のPublisherと非同期に発行された要素をフラット化する
                .concatMap(x -> {
                    // onNextのシグナルが到達してからDurationで指定した時間分送らせる
                    Mono.delay(Duration.ofMillis(100))
                })
                .blockLast()
        // overflowする
        then:
        true
    }

    def "onBackpressureDropでバックプレッシャーから溢れた要素をハンドリングする"(){

        when:
        Flux.interval(Duration.ofMillis(1)).take(10)
                .log()
        // 処理のボトルネック
        // 内部のPublisherと非同期に発行された要素をフラット化する
                .onBackpressureDrop(t -> System.out.println("dropped : " + t))
                .concatMap(x -> {
                    // onNextのシグナルが到達してからDurationで指定した時間分送らせる
                    Mono.delay(Duration.ofMillis(100))
                })
                .doOnNext(t -> System.out.println("doOnNext : " + t))
                .blockLast()
        // overflowする
        then:
        true
    }

    def "onBackpressureDropを適切な場所に置く必要がある"(){

        when:
        Flux.interval(Duration.ofMillis(1)).take(10)
                .log()
        // 処理のボトルネック
        // 内部のPublisherと非同期に発行された要素をフラット化する
                .concatMap(x -> {
                    // onNextのシグナルが到達してからDurationで指定した時間分送らせる
                    Mono.delay(Duration.ofMillis(100))
                })
                .onBackpressureDrop(t -> System.out.println("dropped : " + t))
                .blockLast()
        // overflowする
        then:
        true
    }

    def "onBackPressureDropではなくDropした要素をbufferしておく"(){

        when:
        Flux.interval(Duration.ofMillis(1)).take(10)
                .log()
                .onBackpressureBuffer(2, BufferOverflowStrategy.DROP_LATEST)
                .concatMap(x -> {
                    // onNextのシグナルが到達してからDurationで指定した時間分送らせる
                    Mono.delay(Duration.ofMillis(100))
                })
                .doOnNext(t -> System.out.println("doOnNext : " + t))
                .blockLast()
        Thread.sleep(1000L)
        then:
        true
    }

    def "requestの個数を指定するsubscriberを登録する"(){

        when:
        fluxSample.simpleFlux(10)
                .log()
                .onBackpressureDrop(t -> System.out.println("dropped : " + t))
                .subscribe(new CustomBackPressureSubscriber())
        Thread.sleep(1000L)
        then:
        true
    }

    def "buffer演算子によって、サブスクライバ―が要求したデータ数以上のデータをPublisher側に要求する"(){
        when:
        fluxSample.simpleFlux(10)
                .log()
                .onBackpressureDrop(t -> System.out.println("dropped : " + t))
                .buffer(2)
                .subscribe(new CustomListBackPressureSubscriber())

        then:
        true
    }

    def "prefetchでリクエストの前にデータを取得する"(){
        when:
        fluxSample.simpleFlux(10)
                .log()
                .concatMap(t ->  Mono.just(Arrays.asList(t, 1)), 3)
                .subscribe(new CustomListBackPressureSubscriber())

        then:
        true
    }
}
