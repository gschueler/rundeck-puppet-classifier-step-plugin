package com.simplifyops.util.puppet.classifierapi;

/**
 * Created by greg on 3/9/16.
 */
public class GroupCreate extends Group {
    private boolean environment_trumps;

    public boolean isEnvironment_trumps() {
        return environment_trumps;
    }

    public void setEnvironment_trumps(boolean environment_trumps) {
        this.environment_trumps = environment_trumps;
    }
}
