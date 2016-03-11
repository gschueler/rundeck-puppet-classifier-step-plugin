package com.simplifyops.util.puppet.classifierapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Created by greg on 3/9/16.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ClassifierClass {
    private String name;
    private String environment;
    private Map<String,Object> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
