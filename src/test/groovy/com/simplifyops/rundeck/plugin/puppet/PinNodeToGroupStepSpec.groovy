package com.simplifyops.rundeck.plugin.puppet

import spock.lang.Specification

/**
 * Created by greg on 3/10/16.
 */
class PinNodeToGroupStepSpec extends Specification {
    def "generate rules"() {
        given:
        def nodes = ['a', 'b', 'c']
        when:

        def result = PinNodeToGroupStep.generateRules(nodes)

        then:
        result == [
                ["=",["trusted","certname"],"a"],
                ["=",["trusted","certname"],"b"],
                ["=",["trusted","certname"],"c"],
        ]
    }
}
