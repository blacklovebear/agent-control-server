package com.citic.entity;

import com.citic.AppConf;
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

        // 通过管理平台传过来的秘密为密文
        if (AppConf.isCanalPasswordEncrypt()) {
            // 配置文件中保持密文密码
            dbPassword = unit.getDbPassword();
        } else {
            // 转换为明文密码
            dbPassword = AesUtil.decForTd(unit.getDbPassword());
        }
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
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() == obj.getClass()) {
            return instance.equals(((CanalInstance) obj).getInstance());
        }
        return false;
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
