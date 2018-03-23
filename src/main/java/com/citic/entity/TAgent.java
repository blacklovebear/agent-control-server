package com.citic.entity;

/*
* TAgent 配置获取实体类
*/
public class TAgent {
    private String sourceZkServers;
    private String sourceDestination;
    private String sinkServers;
    private String tableToTopicMap;
    private String tableFieldsFilter;

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

    public String getTableToTopicMap() {
        return tableToTopicMap;
    }

    public void setTableToTopicMap(String tableToTopicMap) {
        this.tableToTopicMap = tableToTopicMap;
    }

    public String getTableFieldsFilter() {
        return tableFieldsFilter;
    }

    public void setTableFieldsFilter(String tableFieldsFilter) {
        this.tableFieldsFilter = tableFieldsFilter;
    }

    @Override
    public String toString() {
        return "TAgent{" +
                "sourceZkServers='" + sourceZkServers + '\'' +
                ", sourceDestination='" + sourceDestination + '\'' +
                ", sinkServers='" + sinkServers + '\'' +
                ", tableToTopicMap='" + tableToTopicMap + '\'' +
                ", tableFieldsFilter='" + tableFieldsFilter + '\'' +
                '}';
    }
}
