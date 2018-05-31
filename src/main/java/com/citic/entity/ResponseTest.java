package com.citic.entity;

public class ResponseTest {

    private String jobId;
    private String endTime;
    private String inputNum;
    private String outputNum;
    private String execCode;
    private String execMessage;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getInputNum() {
        return inputNum;
    }

    public void setInputNum(String inputNum) {
        this.inputNum = inputNum;
    }

    public String getOutputNum() {
        return outputNum;
    }

    public void setOutputNum(String outputNum) {
        this.outputNum = outputNum;
    }

    public String getExecCode() {
        return execCode;
    }

    public void setExecCode(String execCode) {
        this.execCode = execCode;
    }

    public String getExecMessage() {
        return execMessage;
    }

    public void setExecMessage(String execMessage) {
        this.execMessage = execMessage;
    }

    @Override
    public String toString() {
        return "ResponseTest{"
            + "jobId='" + jobId + '\''
            + ", endTime='" + endTime + '\''
            + ", inputNum='" + inputNum + '\''
            + ", outputNum='" + outputNum + '\''
            + ", execCode='" + execCode + '\''
            + ", execMessage='" + execMessage + '\''
            + '}';
    }
}
