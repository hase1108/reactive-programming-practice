package com.example.chapter.one;

import org.reactivestreams.Subscription;
import reactor.core.Exceptions;
import reactor.core.publisher.BaseSubscriber;

public class SampleSubscriber extends BaseSubscriber<Integer> {

    protected void hookOnSubscribe(Subscription subscription){
        System.out.println("onSubscribe subscribe start");
        subscription.request(Long.MAX_VALUE);
    }


    protected void hookOnNext(Integer value){
        System.out.printf("onNext %d",value);
    }


    protected void hookOnComplete() {
        System.out.println("onComplete subscribe end");
    }


    protected void hookOnError(Throwable throwable) {
        System.out.printf("onError %s", throwable.getMessage());
        throw Exceptions.errorCallbackNotImplemented(throwable);
    }


    protected void hookOnCancel() {
        //NO-OP
    }
}