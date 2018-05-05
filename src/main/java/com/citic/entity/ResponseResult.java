package com.citic.entity;

public class ResponseResult {

    public static final int ERROR = -1;
    private static final int SUCCESS = 0;
    private static final String SUCCESS_MESSAGE = "success";

    private int code = SUCCESS;
    private String message = SUCCESS_MESSAGE;

    public ResponseResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseResult() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ResponseResult{"
            + "code=" + code
            + ", message='" + message + '\''
            + '}';
    }
}
