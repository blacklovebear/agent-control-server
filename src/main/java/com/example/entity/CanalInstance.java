package com.example.entity;

/*
* Canal Instance 配置获取实体类
*/
public class CanalInstance {
    // canal instance
    private String instance;
    private String slaveId;
    private String masterAddress;
    private String dbUsername;
    private String dbPassword;
    private String filterRegex;

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(String slaveId) {
        this.slaveId = slaveId;
    }

    public String getMasterAddress() {
        return masterAddress;
    }

    public void setMasterAddress(String masterAddress) {
        this.masterAddress = masterAddress;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getFilterRegex() {
        return filterRegex;
    }

    public void setFilterRegex(String filterRegex) {
        this.filterRegex = filterRegex;
    }

    @Override
    public String toString() {
        return "CanalInstance{" +
                "slaveId='" + slaveId + '\'' +
                ", masterAddress='" + masterAddress + '\'' +
                ", dbUsername='" + dbUsername + '\'' +
                ", dbPassword='" + dbPassword + '\'' +
                ", filterRegex='" + filterRegex + '\'' +
                '}';
    }
}
