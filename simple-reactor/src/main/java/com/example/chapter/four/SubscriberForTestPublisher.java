package com.example.chapter.four;

import org.reactivestreams.Subscription;
import reactor.core.Exceptions;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SubscriberForTestPublisher extends BaseSubscriber<Integer> {

    private boolean subscribed = false;

    private boolean completed = false;

    private Throwable thrown = null;

    private List<Integer> onNext = Collections.synchronizedList(new ArrayList<>());

    private boolean cancelled = false;



    protected void hookOnSubscribe(Subscription subscription) {
        this.subscribed = true;
        subscription.request(Long.MAX_VALUE);
    }

    protected void hookOnNext(Integer value) {
        this.onNext.add(value);
    }

    protected void hookOnComplete() {
        this.completed = true;
    }

    protected void hookOnError(Throwable throwable) {
        this.thrown = throwable;
        throw Exceptions.errorCallbackNotImplemented(throwable);
    }

    protected void hookOnCancel() {
        this.cancelled = true;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Throwable getThrown() {
        return thrown;
    }

    public List<Integer> getOnNext() {
        return onNext;
    }

    public boolean getCancelled() {
        return cancelled;
    }

}
