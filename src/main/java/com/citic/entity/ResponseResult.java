package com.citic.entity;

public class ResponseResult {
    public final static int ERROR = -1;
    private final static int SUCCESS = 0;
    private final static String SUCCESS_MESSAGE = "success";

    private int code = SUCCESS;
    private String message = SUCCESS_MESSAGE;

    public ResponseResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseResult() { }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ResponseResult{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
