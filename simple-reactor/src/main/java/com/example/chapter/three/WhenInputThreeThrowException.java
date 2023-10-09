package com.example.chapter.three;

import com.example.OriginalException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class WhenInputThreeThrowException {

    public static String throwExceptionWhenThree(int i){
        if(i == 3){
            throw new RuntimeException("Error Occured 3");
        }

        return "input : " + i;
    }

    public static Flux<String> throwExceptionWhenThreeFlux(int i){
        if(i == 3){
            throw new RuntimeException("Error Occured 3");
        }

        return Flux.just("input : " + i);
    }

    public static Flux<String> throwExceptionWhenThreeWrapByFlux(int i){
        if(i == 3){
            return Flux.error( new RuntimeException("Error Occured 3"));
        }

        return Flux.just("input : " + i);
    }

    public static Flux<String> throwOriginalExceptionWhenThree(int i){
        if(i == 3){
            throw new OriginalException("Error Occured 3");
        }

        return Flux.just("input : " + i);
    }

    public static Flux<String> throwOriginalException2WhenThree(int i){
        if(i == 3){
            throw new OriginalException("Error Occured 3");
        }

        return Flux.just("input : " + i);
    }
}
