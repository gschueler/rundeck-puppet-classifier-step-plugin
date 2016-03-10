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

    def "merge update group rule op"() {
        given:
        def rules = ["=", "name", "mynode"]
        def group = new Group()
        group.rule = ["=", "name", "othernode"]

        when:
        def result = ClassifierAPI.updateGroupRulesMerge(group, rules, oper == "and")

        then:
        result.rule == [oper,
                        ["=", "name", "othernode"],
                        ["=", "name", "mynode"]
        ]

        where:
        oper  | _
        "and" | _
        "or"  | _
    }

    def "merge update group rule existing op"() {
        given:
        def rules = [["=", "name", "mynode"]]
        def group = new Group()
        group.rule = [oper,
                      ["=", "name", "othernode"],
                      ["=", "name", "othernode2"]
        ]
        when:
        def result = ClassifierAPI.updateGroupRulesMerge(group, rules, oper == "and")

        then:
        result.rule == [oper,
                        ["=", "name", "othernode"],
                        ["=", "name", "othernode2"],
                        ["=", "name", "mynode"]
        ]

        where:
        oper  | _
        "and" | _
        "or"  | _
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
        def result = ClassifierAPI.updateGroupRulesMerge(group, rules, false)

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
