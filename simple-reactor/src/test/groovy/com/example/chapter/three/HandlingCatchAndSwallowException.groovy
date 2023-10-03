package com.example.chapter.three

import com.example.chapter.one.FluxSample
import spock.lang.Specification

class HandlingCatchAndSwallowException extends Specification{
    def fluxSample = new FluxSample()

    def "CatchAndSwallow : mapでエラーを握りつぶす"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.map(WhenInputThreeThrowException::throwExceptionWhenThree)
                .onErrorComplete()
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }

    def "CatchAndSwallow : flatmapでエラーを握りつぶす"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i ->{
            WhenInputThreeThrowException.throwExceptionWhenThreeFlux(i)
        }).onErrorComplete()
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }

    def "CatchAndSwallow : flatmapでエラーを握りつぶせないパターン"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i ->{
            WhenInputThreeThrowException.throwExceptionWhenThreeFlux(i).onErrorComplete()
        })
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }


}
