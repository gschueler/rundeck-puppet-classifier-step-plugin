package com.simplifyops.util.puppet.classifierapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Created by greg on 3/10/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {
    private String kind;
    private String msg;
    private Map details;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map getDetails() {
        return details;
    }

    public void setDetails(Map details) {
        this.details = details;
    }

    @Override
    public String toString() {
        if (null != msg) {
            return String.format("API Error \"%s\" {kind=%s, details=%s}", msg, kind, details);
        } else {
            return String.format("API Error {kind=%s, details=%s}", kind, details);
        }
    }
}
