package com.example;

import com.example.chapter.zero.CheckTreadSample;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;

public class SampleReactor {

    public static void main(String[] args) {
        new CheckTreadSample().checkThreadMapNormal(1000);
    }
}
