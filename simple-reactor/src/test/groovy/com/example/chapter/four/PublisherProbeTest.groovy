package com.example.chapter.four

import com.example.chapter.one.FluxSample
import com.example.chapter.three.WhenInputThreeThrowException
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import reactor.test.publisher.PublisherProbe
import spock.lang.Specification

class PublisherProbeTest extends Specification{

    def fluxSample = new FluxSample()

    def "PublisherProbeの投入でどのようなパスを通ったか検証する"(){
        given:
        def probe = PublisherProbe.of(Flux.just(3).flatMap((i) ->{
            WhenInputThreeThrowException.throwExceptionWhenThreeFlux(i)}))


        when:
        def source = fluxSample.simpleFlux(2).flatMap((i) ->{
            WhenInputThreeThrowException.throwExceptionWhenThreeFlux(i)
        })

        then:
        StepVerifier.create(source.concatWith(probe.flux())).expectNext("input : 0").expectNext("input : 1").verifyError()
        probe.assertWasRequested()
    }
}
