package com.citic.entity;

import java.util.Random;

/**
 * The type Canal instance.
 */
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

    /**
     * Instantiates a new Canal instance.
     *
     * @param unit the unit
     */
    public CanalInstance(UnionConfig.Unit unit) {
        instance = unit.getInstance();
        masterAddress = unit.getMasterAddress();
        dbUsername = unit.getDbUsername();
        dbPassword = unit.getDbPassword();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public String getInstance() {
        return instance;
    }

    /**
     * Sets instance.
     *
     * @param instance the instance
     */
    public void setInstance(String instance) {
        this.instance = instance;
    }

    /**
     * Gets slave id.
     *
     * @return the slave id
     */
    public String getSlaveId() {
        return slaveId;
    }

    /**
     * Gets master address.
     *
     * @return the master address
     */
    public String getMasterAddress() {
        return masterAddress;
    }

    /**
     * Sets master address.
     *
     * @param masterAddress the master address
     */
    public void setMasterAddress(String masterAddress) {
        this.masterAddress = masterAddress;
    }

    /**
     * Gets db username.
     *
     * @return the db username
     */
    public String getDbUsername() {
        return dbUsername;
    }

    /**
     * Sets db username.
     *
     * @param dbUsername the db username
     */
    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    /**
     * Gets db password.
     *
     * @return the db password
     */
    public String getDbPassword() {
        return dbPassword;
    }

    /**
     * Sets db password.
     *
     * @param dbPassword the db password
     */
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
        return (o instanceof CanalInstance)
            && (((CanalInstance) o).getInstance()).equals(this.getInstance());
    }

    @Override
    public String toString() {
        return "CanalInstance{"
            + "slaveId='" + slaveId + '\''
            + ", masterAddress='" + masterAddress + '\''
            + ", dbUsername='" + dbUsername + '\''
            + ", dbPassword='" + dbPassword + '\''
            + '}';
    }
}
