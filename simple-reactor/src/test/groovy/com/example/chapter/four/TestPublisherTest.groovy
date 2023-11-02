package com.example.chapter.four

import reactor.test.publisher.TestPublisher
import reactor.test.StepVerifier
import spock.lang.Specification

class TestPublisherTest extends Specification{

    def "TestPublisherで手動で任意のデータを流すことができる"(){
        given:
        def subscriber = new SubscriberForTestPublisher()
        def testPublisher =TestPublisher<Integer>.create()
        testPublisher.subscribe(subscriber)

        when:
        testPublisher.next(1,3,5)
        testPublisher.error(new RuntimeException("test"))


        then:
        subscriber.getOnNext().size() == 3
        subscriber.getThrown().getClass() == RuntimeException.class
    }

    def "TestPublisherでSubscriptionの状態を検証することもできる"(){
        given:
        def subscriber = new SubscriberForTestPublisher()
        def testPublisher =TestPublisher<Integer>.create()
        testPublisher.subscribe(subscriber)

        when:
        testPublisher.next(1,3,5)


        then:
        testPublisher.assertSubscribers()
    }
}
