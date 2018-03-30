package com.citic.entity;

import com.google.common.collect.Sets;

import java.util.Set;

/*
* 联合配置Canal, TAgent
* */
public class UnionConfig {
    private String zkServers;
    private String kafkaServers;
    private Set<Unit> units = Sets.newHashSet();

    private CanalServer canalServer;
    private TAgent tAgent;

    public void configReleaseToCanalTAgent() {
        if (canalServer == null || tAgent == null) {
            canalServer = new CanalServer();
            tAgent = new TAgent();
        }
        canalServer.setZkServers(this.zkServers);
        tAgent.setSourceZkServers(this.zkServers);
        tAgent.setSinkServers(this.kafkaServers);

        units.forEach(unit -> {
            canalServer.addInstance(new CanalInstance(unit));
            tAgent.addSource(new TAgent.Source(unit));
        });
    }

    public CanalServer getCanalServer() {
        configReleaseToCanalTAgent();
        return canalServer;
    }

    public TAgent getTAgent() {
        configReleaseToCanalTAgent();
        return tAgent;
    }

    public void addUnit(Unit unit) {
        units.add(unit);
    }

    public String getZkServers() {
        return zkServers;
    }

    public void setZkServers(String zkServers) {
        this.zkServers = zkServers;
    }

    public String getKafkaServers() {
        return kafkaServers;
    }

    public void setKafkaServers(String kafkaServers) {
        this.kafkaServers = kafkaServers;
    }

    public Set<Unit> getUnits() {
        return units;
    }

    public void setUnits(Set<Unit> units) {
        this.units = units;
    }

    /*
        * 一个配置单元,就对应一个 Canal instance 并且带上 TAgent 的一些配置信息
        * */
    public static class Unit {
        // 当前 instance的值,默认使用 masterAddress
        private String instance;
        private String masterAddress;
        private String dbUsername;
        private String dbPassword;

        private String tableFilter;
        private String tableToTopicMap;
        private String tableFieldsFilter;

        /*
        * 关键: 当前默认将 masterAddress 作为 canal instance 名
        * */
        public String getInstance() {
            return masterAddress;
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

        public String getTableFilter() {
            return tableFilter;
        }

        public void setTableFilter(String tableFilter) {
            this.tableFilter = tableFilter;
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
        public int hashCode(){
            return instance.hashCode();
        }

        @Override
        public boolean equals(Object obj){
            return obj instanceof Unit && instance.equals(((Unit)obj).instance);
        }
    }
}
