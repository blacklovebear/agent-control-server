package com.citic.entity;

import com.citic.helper.Utility;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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

    public void checkProperties() throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(zkServers), "zkServers is null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(kafkaServers), "kafkaServers is null or empty");

        Utility.isUrlsAddressListValid(zkServers, "zkServers");
        Utility.isUrlsAddressListValid(kafkaServers, "kafkaServers");

        Preconditions.checkArgument(units.size() > 0, "units is empty");

        for(Unit unit : units) {
            unit.checkProperties();
        }
    }

    public void configReleaseToCanalTAgent() {
        if (canalServer == null || tAgent == null) {
            canalServer = new CanalServer();
            tAgent = new TAgent();
        }
        canalServer.setZkServers(this.zkServers);
        tAgent.setSourceZkServers(this.zkServers);
        tAgent.setSinkServers(this.kafkaServers);

        units.forEach(unit -> {
            canalServer.addOrReplaceInstance(new CanalInstance(unit));
            tAgent.addOrReplaceSource(new TAgent.Source(unit));
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

    public void addOrReplaceUnit(Unit unit) {
        units.remove(unit);
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
        private String masterAddress;
        private String dbUsername;
        private String dbPassword;

        private String tableFilter;
        private String tableToTopicMap;
        private String tableFieldsFilter;


        public void checkProperties() throws Exception {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(masterAddress), "dbUsername is null or empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(dbUsername), "dbUsername is null or empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(dbPassword), "dbPassword is null or empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(tableFilter), "tableFilter is null or empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(tableToTopicMap), "tableToTopicMap is null or empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(tableFieldsFilter), "tableFieldsFilter is null or empty");

            Utility.isUrlAddressValid(masterAddress, "masterAddress");
        }

        /*
        * 关键: 当前默认将 masterAddress 作为 canal instance 名
        * */
        public String getInstance() {
            if (Strings.isNullOrEmpty(masterAddress))
                return null;
            else
                return masterAddress.replace(":", "-").replace(".", "_");
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
            return masterAddress.hashCode();
        }

        @Override
        public boolean equals(Object obj){
            return obj instanceof Unit && masterAddress.equals(((Unit)obj).masterAddress);
        }
    }
}
