package com.example.chapter.four

import com.example.chapter.one.FluxSample
import com.example.chapter.three.WhenInputThreeThrowException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.test.StepVerifierOptions
import spock.lang.Specification

import java.time.Duration

class StepVerifierTest extends Specification{

    def fluxSample = new FluxSample()

    def "onNextおよびonCompleteを期待するテスト"(){
        given:
        def num = 3

        when:
        def source = fluxSample.simpleFlux(num)

        then:
        StepVerifier.create(source)
                .expectNext(0)
                .expectNext(1)
                .expectNext(2)
                .expectComplete()
                .verify()
    }

    def "本来であれば2,3のイベントが来るにも関わらずcompleteしてしまっているのでこのテストは失敗する"(){
        given:
        def num = 3

        when:
        def source = fluxSample.simpleFlux(num)

        then:
        StepVerifier.create(source)
                .expectNext(0)
                .expectComplete()
                .verify()
    }

    def "こちらはexpectNextCountで検証しているので成功する"(){
        given:
        def num = 3

        when:
        def source = fluxSample.simpleFlux(num)

        then:
        StepVerifier.create(source)
                .expectNext(0)
                .expectNextCount(2L)
                .expectComplete()
                .verify()
    }

    def "consumeNextWithで検証をカスタマイズもできる"(){
        given:
        def num = 3

        when:
        def source = fluxSample.simpleFlux(num)

        then:
        StepVerifier.create(source)
                .expectNext(0)
                .consumeNextWith( t -> t instanceof Integer)
                .consumeNextWith( t -> t instanceof Integer)
                .expectComplete()
                .verify()
    }

    def "recordWith/consumeRecordedWithで流れてきたデータを一括で検証することもできる"(){
        given:
        def num = 3

        when:
        def source = fluxSample.simpleFlux(num)

        then:
        StepVerifier.create(source)
                .recordWith(() -> Collections.synchronizedList(new ArrayList<>()))
                .expectNextCount(3)
                .consumeRecordedWith(colle -> colle.size() == 3)
                .expectComplete()
                .verify()
    }

    def "OnErrorが発生することを検証するテスト"(){
        given:
        def flux = fluxSample.simpleFlux(5)

        when:
        def source = flux.map(i -> WhenInputThreeThrowException.throwExceptionWhenThree(i))

        then:
        StepVerifier.create(source)
                .expectNextCount(3L)
                .expectError()
                .verify()
    }

    def "OnErrorが発生することを検証するテスト2"(){
        given:
        def flux = fluxSample.simpleFlux(5)

        when:
        def source = flux.map(i -> WhenInputThreeThrowException.throwExceptionWhenThree(i))

        then:
        StepVerifier.create(source)
                .expectNextCount(3L)
                .expectError(RuntimeException.class)
                .verify()
    }

    def "as()によって各ステップで何を検証しているか記述できる"(){
        given:
        def num = 3

        when:
        def source = fluxSample.simpleFlux(num)

        then:
        StepVerifier.create(source)
                .expectNextCount(3L)
                .as("3つのデータが流れてくること")
                .expectComplete()
                .verify()
    }

    def "as()によって各ステップで何を検証しているか記述し、テストが当該箇所で失敗するとそれをログに出してくれる"(){
        given:
        def num = 3

        when:
        def source = fluxSample.simpleFlux(num)

        then:
        StepVerifier.create(source)
                .expectNextCount(4L)
                .as("3つのデータが流れてくること")
                .expectComplete()
                .verify()
    }

    def "scenarioName()によってシナリオ全体の説明を記述できる"(){
        given:
        def num = 3
        def options = StepVerifierOptions.create().scenarioName("シナリオ名")


        when:
        def source = fluxSample.simpleFlux(num)

        then:
        StepVerifier.create(source, options)
                .expectNextCount(3L)
                .expectComplete()
                .verify()
    }

    def "scenarioName()に記述したシナリオはテストが失敗した場合はログにでる"(){
        given:
        def num = 3
        def options = StepVerifierOptions.create().scenarioName("シナリオ名")


        when:
        def source = fluxSample.simpleFlux(num)

        then:
        StepVerifier.create(source, options)
                .expectNextCount(4L)
                .expectComplete()
                .verify()
    }

    def "withVirtualTimeを利用して時間がかかるテストを効率よく実施する"(){
        when:
        def elements = 3
        def seconds = 29L

        then:
        // lambdaの中でPublisherのインスタンスを作成すること
        StepVerifier.withVirtualTime(() -> LongTimePublisher.getLongTimePublisher(elements, seconds))
            .expectSubscription()
            .expectNoEvent(Duration.ofSeconds(20L))
            .expectNext(0)
            .expectNoEvent(Duration.ofSeconds(20L))
            .expectNext(1)
            .expectNoEvent(Duration.ofSeconds(20L))
            .expectNext(2)
            .verifyComplete()
    }

    def "withVirtualTimeを遅延がかかるようなテストを実施する2"(){
        when:
        def elements = 3
        def seconds = 29L

        then:
        // lambdaの中でPublisherのインスタンスを作成すること
        StepVerifier.withVirtualTime(() -> LongTimePublisher.getLongTimePublisher(elements, seconds))
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(20L))
                .expectNext(0)
                .thenAwait(Duration.ofSeconds(20L))
                .expectNext(1)
                .thenAwait(Duration.ofSeconds(20L))
                .expectNext(2)
                .verifyComplete()
    }

    def "withVirtualTimeをちゃんと使えていない"(){
        when:
        def elements = 3
        def seconds = 29L
        def source = LongTimePublisher.getLongTimePublisher(elements, seconds)

        then:
        // lambdaの中でPublisherのインスタンスを作成すること
        StepVerifier.withVirtualTime(() -> source)
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(20L))
                .expectNext(0)
                .thenAwait(Duration.ofSeconds(20L))
                .expectNext(1)
                .thenAwait(Duration.ofSeconds(20L))
                .expectNext(2)
                .verifyComplete()
    }

}
