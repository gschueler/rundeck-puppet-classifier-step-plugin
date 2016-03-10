package com.simplifyops.util.puppet.classifierapi;

import java.util.List;
import java.util.Map;

/**
 * Created by greg on 3/9/16.
 */
public class Group {
    private String name;
    private String id;
    private String description;
    private String environment;
    private String parent;
    private List rule;
    private Map classes;
    private Map deleted;
    private Map variables;
    private boolean environment_trumps;

    public boolean isEnvironment_trumps() {
        return environment_trumps;
    }

    public void setEnvironment_trumps(boolean environment_trumps) {
        this.environment_trumps = environment_trumps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public List getRule() {
        return rule;
    }

    public void setRule(List rule) {
        this.rule = rule;
    }

    public Map getClasses() {
        return classes;
    }

    public void setClasses(Map classes) {
        this.classes = classes;
    }

    public Map getVariables() {
        return variables;
    }

    public void setVariables(Map variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return "com.simplifyops.util.puppet.classifierapi.Group{" +
               "name='" + name + '\'' +
               ", id='" + id + '\'' +
               ", description='" + description + '\'' +
               ", environment='" + environment + '\'' +
               ", parent='" + parent + '\'' +
               ", rule=" + rule +
               ", classes=" + classes +
               ", deleted=" + deleted +
               ", variables=" + variables +
               '}';
    }

    public Map getDeleted() {
        return deleted;
    }

    public void setDeleted(Map deleted) {
        this.deleted = deleted;
    }
}
