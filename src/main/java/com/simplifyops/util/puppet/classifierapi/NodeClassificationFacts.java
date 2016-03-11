package com.simplifyops.util.puppet.classifierapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Created by greg on 3/9/16.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class NodeClassificationFacts {
    private Map<String,Object> fact;
    private Map<String,Object> trusted;

    public Map<String, Object> getFact() {
        return fact;
    }

    public void setFact(Map<String, Object> fact) {
        this.fact = fact;
    }

    public Map<String, Object> getTrusted() {
        return trusted;
    }

    public void setTrusted(Map<String, Object> trusted) {
        this.trusted = trusted;
    }
}
