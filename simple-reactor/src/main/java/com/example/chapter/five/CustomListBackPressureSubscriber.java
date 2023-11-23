package com.example.chapter.five;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.util.List;

public class CustomListBackPressureSubscriber extends BaseSubscriber<List<Integer>> {

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        subscription.request(1L);
    }

    @Override
    protected void hookOnNext(List<Integer> value) {
        System.out.println("value : " + value.toString());
        request(1L);
    }
}
