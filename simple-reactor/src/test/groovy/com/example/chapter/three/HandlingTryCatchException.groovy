package com.example.chapter.three

import com.example.chapter.one.FluxSample
import spock.lang.Specification

class HandlingTryCatchException extends Specification{

    def fluxSample = new FluxSample()

    def "try-catch : 3以降のデータは流れない"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.map(WhenInputThreeThrowException::throwExceptionWhenThree)
        .subscribe(value -> System.out.println(value),
        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }

    def "try-catch : 3以降のデータは流れない2"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(WhenInputThreeThrowException::throwExceptionWhenThreeFlux)
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }


    def "try-catch : 3以降のデータは流れない3"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(WhenInputThreeThrowException::throwExceptionWhenThreeMono)
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()))

        then:
        true
    }
}
