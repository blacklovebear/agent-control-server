package com.citic.entity;

/*
* Canal Server 配置获取实体类
*/
public class CanalServer {
    // canal server
    private String zkServers;
    private String destinations;

    public String getZkServers() {
        return zkServers;
    }

    public void setZkServers(String zkServers) {
        this.zkServers = zkServers;
    }

    public String getDestinations() {
        return destinations;
    }

    public void setDestinations(String destinations) {
        this.destinations = destinations;
    }

    @Override
    public String toString() {
        return "CanalServer{" +
                "zkServers='" + zkServers + '\'' +
                ", destinations='" + destinations + '\'' +
                '}';
    }
}
