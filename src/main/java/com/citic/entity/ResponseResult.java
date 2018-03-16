package com.citic.entity;

public class ResponseResult {
    private String action;
    private String message;

    public ResponseResult(String action, String message) {
        this.action = action;
        this.message = message;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }
}
