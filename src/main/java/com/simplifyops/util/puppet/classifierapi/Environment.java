package com.simplifyops.util.puppet.classifierapi;

/**
 * Created by greg on 3/9/16.
 */
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
