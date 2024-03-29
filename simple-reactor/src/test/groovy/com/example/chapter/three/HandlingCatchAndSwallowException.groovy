package com.example.chapter.three

import com.example.chapter.one.FluxSample
import com.example.chapter.three.WhenInputThreeThrowException
import spock.lang.Specification

class HandlingCatchAndSwallowException extends Specification{
    def fluxSample = new FluxSample()

    /*
    opnErrorCompleteで発生したエラーを握りつぶして正常終了させることができる
    ただし、データストリーム自体はエラーが発生した時点で打ち切られることに注意が必要
    onCompleteメソッドは実行される
     */

    def "CatchAndSwallow : mapでエラーを握りつぶす"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.map(WhenInputThreeThrowException::throwExceptionWhenThree)
                .onErrorComplete()
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()),
                        () -> System.out.println("Subscribe Finish"))

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

    /*
    onErrorReturnと同様、FlatMapの内部ストリームでエラーが発生/ハンドリングを実施する場合、エラーが発生した要素のみ握りつぶして
    大元のデータストリームは停止させないことができる
     */
    def "CatchAndSwallow : 内部ストリームのハンドリング"(){
        given:

        def flux = fluxSample.simpleFlux(5)

        when:
        flux.flatMap(i ->{
            WhenInputThreeThrowException.throwExceptionWhenThreeWrapByFlux(i).onErrorComplete()
        })
                .subscribe(value -> System.out.println(value),
                        error -> System.out.println("ERROR " + error.getMessage()),
                        () -> System.out.println("Subscribe Finish"))

        then:
        true
    }


}
