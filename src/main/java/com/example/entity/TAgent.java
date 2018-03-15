package com.example.entity;

/*
* TAgent 配置获取实体类
*/
public class TAgent {
    private String sourceZkServers;
    private String sourceDestination;
    private String sinkServers;
    private String sinkTopicHeader;

    public String getSourceZkServers() {
        return sourceZkServers;
    }

    public void setSourceZkServers(String sourceZkServers) {
        this.sourceZkServers = sourceZkServers;
    }

    public String getSourceDestination() {
        return sourceDestination;
    }

    public void setSourceDestination(String sourceDestination) {
        this.sourceDestination = sourceDestination;
    }

    public String getSinkServers() {
        return sinkServers;
    }

    public void setSinkServers(String sinkServers) {
        this.sinkServers = sinkServers;
    }

    public String getSinkTopicHeader() {
        return sinkTopicHeader;
    }

    public void setSinkTopicHeader(String sinkTopicHeader) {
        this.sinkTopicHeader = sinkTopicHeader;
    }

    @Override
    public String toString() {
        return "TAgent{" +
                "sourceZkServers='" + sourceZkServers + '\'' +
                ", sourceDestination='" + sourceDestination + '\'' +
                ", sinkServers='" + sinkServers + '\'' +
                ", sinkTopicHeader='" + sinkTopicHeader + '\'' +
                '}';
    }
}
