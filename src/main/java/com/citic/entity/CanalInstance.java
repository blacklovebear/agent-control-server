package com.citic.entity;

import java.util.*;

/*
* Canal Instance 配置获取实体类
* instance 区别唯一 Instance
*/
public class CanalInstance {
    // canal instance
    private String instance;
    private String slaveId = String.format("%05d", new Random().nextInt(100000));
    private String masterAddress;
    private String dbUsername;
    private String dbPassword;

    public CanalInstance(UnionConfig.Unit unit) {
        instance = unit.getInstance();
        masterAddress = unit.getMasterAddress();
        dbUsername = unit.getDbUsername();
        dbPassword = unit.getDbPassword();
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getSlaveId() {
        return slaveId;
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

    /*
    * instance 是判断的唯一值
    * */
    @Override
    public int hashCode() {
        return instance.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof CanalInstance) &&
                (((CanalInstance)o).getInstance()).equals(this.getInstance());
    }

    @Override
    public String toString() {
        return "CanalInstance{" +
                "slaveId='" + slaveId + '\'' +
                ", masterAddress='" + masterAddress + '\'' +
                ", dbUsername='" + dbUsername + '\'' +
                ", dbPassword='" + dbPassword + '\'' +
                '}';
    }
}
