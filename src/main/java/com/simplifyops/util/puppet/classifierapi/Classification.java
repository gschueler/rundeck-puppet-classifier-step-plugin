package com.simplifyops.util.puppet.classifierapi;

import java.util.List;
import java.util.Map;

/**
 * Created by greg on 3/9/16.
 */
public class Classification {
    private String name;
    private List<String> groups;
    private String environment;
    private Map<String,Object> classes;
    private Map<String,Object> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Map<String, Object> getClasses() {
        return classes;
    }

    public void setClasses(Map<String, Object> classes) {
        this.classes = classes;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
