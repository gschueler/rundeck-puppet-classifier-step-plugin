package com.simplifyops.util.puppet.classifierapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by greg on 3/9/16.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Environment {
    private String name;
    private boolean sync_succeeded;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSync_succeeded() {
        return sync_succeeded;
    }

    public void setSync_succeeded(boolean sync_succeeded) {
        this.sync_succeeded = sync_succeeded;
    }
}
