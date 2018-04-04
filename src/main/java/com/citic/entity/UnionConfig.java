package com.citic.entity;

import com.citic.helper.Utility;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.util.List;
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

        private String tableTopicSchemaMap;
        private String tableFieldSchemaMap;


        // 给 TAgent 生成配置文件使用的中间变量
        private List<String> sourceTableToTopicMapList = Lists.newArrayList();
        private List<String> sourceTableFieldsFilterList = Lists.newArrayList();
        private List<String> sinkTableToSchemaMapList = Lists.newArrayList();


        public void checkProperties() throws Exception {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(masterAddress), "dbUsername is null or empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(dbUsername), "dbUsername is null or empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(dbPassword), "dbPassword is null or empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(tableTopicSchemaMap), "tableTopicSchemaMap is null or empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(tableFieldSchemaMap), "tableFieldSchemaMap is null or empty");

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


        public void setTableTopicSchemaMap(String tableTopicSchemaMap) {
            this.tableTopicSchemaMap = tableTopicSchemaMap;
            // test.test:test123:scheme_name;test.test1:test234:schema_name
            // db.table:topic:schema;db.table:topic:schema
            Splitter.on(";")
                    .omitEmptyStrings()
                    .trimResults()
                    .split(tableTopicSchemaMap)
                    .forEach(item -> {
                        String[] temp = item.split(":");
                        Preconditions.checkArgument(temp.length == 3,
                                "tableTopicSchemaMap format error " +
                                        "eg: db.table1:topic1:schema1;db.table2:topic2:schema2");

                        sourceTableToTopicMapList.add(temp[0] + ":" + temp[1]);
                        sinkTableToSchemaMapList.add(temp[0] + ":" + temp[2]);
                    });

        }

        public void setTableFieldSchemaMap(String tableFieldSchemaMap) {
            this.tableFieldSchemaMap = tableFieldSchemaMap;
            // uid,name|uid1,name1;id,name|id1,name1;name|name2
            Splitter.on(";")
                    .omitEmptyStrings()
                    .trimResults()
                    .split(tableFieldSchemaMap)
                    .forEach(item -> {
                        String[] temp = item.split("|");
                        Preconditions.checkArgument(temp.length == 2,
                                "tableFieldSchemaMap format error " +
                                        "eg: uid,name|uid1,name1;id,name|id1,name1");

                        Preconditions.checkArgument(StringUtils.countMatches(temp[0], ",")
                                == StringUtils.countMatches(temp[1], ","),
                                "tableFieldSchemaMap format error " +
                                "eg: uid,name|uid1,name1;id,name|id1,name1");

                        sourceTableFieldsFilterList.add(temp[0]);
                    });
        }

        // TAgent source
        public String getSourceTableToTopicMap() { return Joiner.on(";").join(sourceTableToTopicMapList); }
        public String getSourceTableFieldsFilter() {
            return Joiner.on(";").join(sourceTableFieldsFilterList);
        }

        // TAgent sink
        public String getSinkTableToSchemaMap() {
            return Joiner.on(";").join(sinkTableToSchemaMapList);
        }
        public String getSinkTableFieldSchemaMap() {
            return tableFieldSchemaMap;
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
