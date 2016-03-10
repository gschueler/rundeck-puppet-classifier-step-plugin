package com.simplifyops.util.puppet.classifierapi;

import java.util.List;

/**
 * Created by greg on 3/9/16.
 */
public class UpdateGroupRules extends UpdateGroup {
    private List rule;


    public List getRule() {
        return rule;
    }

    public void setRule(List rule) {
        this.rule = rule;
    }
}
