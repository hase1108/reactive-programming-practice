package com.example.chapter.zero

import spock.lang.Specification

class CheckTreadSampleTest extends Specification{

    def "thread"(){
        given:
        def range = 1000

        when:
        new CheckTreadSample().checkThreadMapNormal(range)

        then:
        true
    }

    def "thread3"(){
        given:
        def range = 1000

        when:
        new CheckTreadSample().checkThreadFlatMapNormal(range)

        then:
        true
    }

    def "thread2"(){
        given:
        def range = 3

        when:
        new CheckTreadSample().checkNestedThreadSample(range)

        then:
        true
    }
}
