package com.example.chapter.five;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

public class CustomBackPressureSubscriber extends BaseSubscriber<Integer> {

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        subscription.request(1L);
    }

    @Override
    protected void hookOnNext(Integer value) {
        System.out.println("request publisher");
        request(1L);
    }
}
