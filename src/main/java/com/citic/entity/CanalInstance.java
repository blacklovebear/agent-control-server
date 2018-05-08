package com.citic.entity;

import com.citic.helper.AesUtil;
import java.util.Random;

/**
 * The type Canal instance.
 */
/*
 * Canal Instance 配置获取实体类
 * instance 区别唯一 Instance
 */
public class CanalInstance {
    private final String slaveId = String.format("%05d", new Random().nextInt(100000));

    // canal instance
    private final String instance;
    private final String masterAddress;
    private final String dbUsername;
    private final String dbPassword;

    /**
     * Instantiates a new Canal instance.
     *
     * @param unit the unit
     */
    public CanalInstance(UnionConfig.Unit unit) {
        instance = unit.getInstance();
        masterAddress = unit.getMasterAddress();
        dbUsername = unit.getDbUsername();

        // 通过管理平台传过来的秘密为密文，在canal的配置文件中密码为密文，需要转换
        dbPassword = AesUtil.decForTd(unit.getDbPassword());
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
     * Gets db username.
     *
     * @return the db username
     */
    public String getDbUsername() {
        return dbUsername;
    }

    /**
     * Gets db password.
     *
     * @return the db password
     */
    public String getDbPassword() {
        return dbPassword;
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
