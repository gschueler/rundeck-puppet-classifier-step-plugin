package com.simplifyops.util.puppet

import com.simplifyops.util.puppet.classifierapi.Group
import spock.lang.Specification

/**
 * Created by greg on 3/9/16.
 */
class ClassifierAPISpec extends Specification {
    def "create update group rule"() {
        given:
        def rules = ["=", "name", "mynode"]
        when:
        def result = ClassifierAPI.updateGroupRules(rules)

        then:
        result.rule == rules
    }

    def "merge update group rule existing op"() {
        given:
        def rules = [["=", "name", "mynode"]]
        def group = new Group()
        group.rule = ["or",
                      ["=", "name", "othernode"],
                      ["=", "name", "othernode2"]
        ]
        when:
        def result = ClassifierAPI.updateGroupRulesMerge(group, rules)

        then:
        result.rule == ["or",
                        ["=", "name", "othernode"],
                        ["=", "name", "othernode2"],
                        ["=", "name", "mynode"]
        ]

    }

    def "merge update group rule no existing rules"() {
        given:
        def rules = [["=", "name", "mynode"]]
        def group = new Group()
        group.rule = []
        when:
        def result = ClassifierAPI.updateGroupRulesMerge(group, rules)

        then:
        result.rule == ["or",
                        ["=", "name", "mynode"]
        ]

    }

    def "update group rule remove nodes no change"() {
        given:
        def nodes = ["anode", "bnode"]
        def group = new Group()
        group.rule = ["or",
                      ["=", "name", "othernode"],
                      ["=", "name", "othernode2"]
        ]
        when:
        def result = ClassifierAPI.updateGroupRulesRemoveNodes(group, nodes)

        then:
        result == null

    }

    def "update group rule remove nodes"() {
        given:
        def group = new Group()
        group.rule = ["or"] + orig.collect { ["=", "name", it] }

        when:
        def result = ClassifierAPI.updateGroupRulesRemoveNodes(group, nodes)

        then:
        result != null
        result.rule == (expected ? ["or"] + expected.collect { ["=", "name", it] } : [])
        where:
        orig               | nodes                           | expected
        ["anode", "bnode"] | ["anode", "bnode", "othernode"] | []
        ["anode", "bnode"] | ["anode"]                       | ["bnode"]
        ["anode", "bnode"] | ["anode", "bnode"]              | []

    }


    def "update group rule remove nodes complex"() {
        given:
        def nodes = ["ubuntu1404b.syd.puppetlabs.demo"]
        def group = new Group()
        group.rule = ["or",
                      [
                              "and",
                              [
                                      "=",
                                      [
                                              "fact",
                                              "osfamily"
                                      ],
                                      "RedHat"
                              ],
                              [
                                      "=",
                                      [
                                              "fact",
                                              "kernel"
                                      ],
                                      "Linux"
                              ]
                      ],
                      [
                              "=",
                              "name",
                              "ubuntu1404b.syd.puppetlabs.demo"
                      ],
                      [
                              "=",
                              "name",
                              "ubuntu1404a.pdx.puppetlabs.demo"
                      ]
        ]
        when:
        def result = ClassifierAPI.updateGroupRulesRemoveNodes(group, nodes)

        then:
        result != null
        result.rule == ["or",
                        [
                                "and",
                                [
                                        "=",
                                        [
                                                "fact",
                                                "osfamily"
                                        ],
                                        "RedHat"
                                ],
                                [
                                        "=",
                                        [
                                                "fact",
                                                "kernel"
                                        ],
                                        "Linux"
                                ]
                        ],
                        [
                                "=",
                                "name",
                                "ubuntu1404a.pdx.puppetlabs.demo"
                        ]
        ]
    }

    def "merge update group rule data"() {
        given:
        def rules = [["=", "name", "mynode"]]
        def group = new Group()
        group.rule = ["or",
                      [
                              "and",
                              [
                                      "=",
                                      [
                                              "fact",
                                              "osfamily"
                                      ],
                                      "RedHat"
                              ],
                              [
                                      "=",
                                      [
                                              "fact",
                                              "kernel"
                                      ],
                                      "Linux"
                              ]
                      ],
                      [
                              "=",
                              "name",
                              "ubuntu1404b.syd.puppetlabs.demo"
                      ],
                      [
                              "=",
                              "name",
                              "ubuntu1404a.pdx.puppetlabs.demo"
                      ]
        ]
        when:
        def result = ClassifierAPI.updateGroupRulesMerge(group, rules)

        then:
        result.rule == ["or",
                        [
                                "and",
                                [
                                        "=",
                                        [
                                                "fact",
                                                "osfamily"
                                        ],
                                        "RedHat"
                                ],
                                [
                                        "=",
                                        [
                                                "fact",
                                                "kernel"
                                        ],
                                        "Linux"
                                ]
                        ],
                        [
                                "=",
                                "name",
                                "ubuntu1404b.syd.puppetlabs.demo"
                        ],
                        [
                                "=",
                                "name",
                                "ubuntu1404a.pdx.puppetlabs.demo"
                        ],
                        ["=", "name", "mynode"]
        ]

    }
}
