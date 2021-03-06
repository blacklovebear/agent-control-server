package com.citic.entity;

import com.citic.helper.AesUtil;
import com.citic.helper.Utility;
import com.citic.helper.Utility.ServerUrlsFormatException;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 * 联合配置Canal, TAgent.
 */
public class UnionConfig {

    private boolean multiTopicJob = true;
    private String zkServers;
    private String kafkaServers;
    private String registryUrl;
    private boolean kafkaHighThroughput = true;
    private boolean useAvro = true;
    private Set<Unit> units = Sets.newHashSet();

    private CanalServer canalServer;
    private TAgent tagent;

    /**
     * Check properties.
     *
     * @throws Exception the exception
     */
    public void checkProperties() throws ServerUrlsFormatException {
        Preconditions
            .checkArgument(!Strings.isNullOrEmpty(zkServers), "zkServers is null or empty");
        Preconditions
            .checkArgument(!Strings.isNullOrEmpty(kafkaServers), "kafkaServers is null or empty");

        Utility.isUrlsAddressListValid(zkServers, "zkServers");
        Utility.isUrlsAddressListValid(kafkaServers, "kafkaServers");

        if (useAvro) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(registryUrl),
                "kafkaServers is null or empty");
            // 正确格式 http://localhost:8081
            String[] temp = registryUrl.split("//");
            Preconditions
                .checkArgument(temp.length == 2, "registryUrl 格式错误, eg: http://localhost:8081");
            Preconditions.checkArgument(temp[0].equals("http:"),
                "registryUrl 格式错误, eg: http://localhost:8081");
            Utility.isUrlAddressValid(temp[1], "registryUrl");
        }

        Preconditions.checkArgument(!units.isEmpty(), "units is empty");

        for (Unit unit : units) {
            unit.checkProperties(useAvro, multiTopicJob);
        }
    }

    /**
     * Config release to canal t agent.
     */
    public void configReleaseToCanalTAgent() {
        if (canalServer == null || tagent == null) {
            canalServer = new CanalServer();
            tagent = new TAgent();
        }
        canalServer.setZkServers(this.zkServers);
        tagent.setMultiTopicJob(this.multiTopicJob);
        tagent.setSourceZkServers(this.zkServers);
        tagent.setSinkServers(this.kafkaServers);
        tagent.setRegistryUrl(this.registryUrl);
        tagent.setKafkaHighThroughput(this.kafkaHighThroughput);
        tagent.setUseAvro(this.useAvro);

        units.forEach(unit -> {
            canalServer.addOrReplaceInstance(new CanalInstance(unit));
            tagent.addOrReplaceSource(new TAgent.Source(unit));
        });
    }

    public boolean isMultiTopicJob() {
        return multiTopicJob;
    }

    public void setMultiTopicJob(boolean multiTopicJob) {
        this.multiTopicJob = multiTopicJob;
    }

    /**
     * Gets canal server.
     *
     * @return the canal server
     */
    public CanalServer getCanalServer() {
        configReleaseToCanalTAgent();
        return canalServer;
    }

    /**
     * Gets t agent.
     *
     * @return the t agent
     */
    public TAgent getTAgent() {
        configReleaseToCanalTAgent();
        return tagent;
    }

    /**
     * Add or replace unit.
     *
     * @param unit the unit
     */
    public void addOrReplaceUnit(Unit unit) {
        units.remove(unit);
        units.add(unit);
    }

    /**
     * Is use avro boolean.
     *
     * @return the boolean
     */
    public boolean isUseAvro() {
        return useAvro;
    }

    /**
     * Sets use avro.
     *
     * @param useAvro the use avro
     */
    public void setUseAvro(boolean useAvro) {
        this.useAvro = useAvro;
    }

    /**
     * Sets kafka high throughput.
     *
     * @param kafkaHighThroughput the kafka high throughput
     */
    public void setKafkaHighThroughput(boolean kafkaHighThroughput) {
        this.kafkaHighThroughput = kafkaHighThroughput;
    }

    /**
     * Gets zk servers.
     *
     * @return the zk servers
     */
    public String getZkServers() {
        return zkServers;
    }

    /**
     * Sets zk servers.
     *
     * @param zkServers the zk servers
     */
    public void setZkServers(String zkServers) {
        this.zkServers = zkServers;
    }

    /**
     * Gets kafka servers.
     *
     * @return the kafka servers
     */
    public String getKafkaServers() {
        return kafkaServers;
    }

    /**
     * Sets kafka servers.
     *
     * @param kafkaServers the kafka servers
     */
    public void setKafkaServers(String kafkaServers) {
        this.kafkaServers = kafkaServers;
    }

    /**
     * Gets registry url.
     *
     * @return the registry url
     */
    public String getRegistryUrl() {
        return registryUrl;
    }

    /**
     * Sets registry url.
     *
     * @param registryUrl the registry url
     */
    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }

    /**
     * Gets units.
     *
     * @return the units
     */
    public Set<Unit> getUnits() {
        return units;
    }

    /**
     * Sets units.
     *
     * @param units the units
     */
    public void setUnits(Set<Unit> units) {
        this.units = units;
    }

    /**
     * The type Unit.
     */
    /*
     * 一个配置单元,就对应一个 Canal instance 并且带上 TAgent 的一些配置信息
     * */
    public static class Unit {

        private String masterAddress;
        private String dbUsername;
        private String dbPassword;

        private String tableToTopicMap;
        private String tableFieldsFilter;

        /**
         * Check properties.
         *
         * @param useAvro the use avro
         */
        public void checkProperties(boolean useAvro, boolean multiTopicJob) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(masterAddress),
                "dbUsername is null or empty");
            Preconditions
                .checkArgument(!Strings.isNullOrEmpty(dbUsername), "dbUsername is null or empty");
            Preconditions
                .checkArgument(!Strings.isNullOrEmpty(dbPassword), "dbPassword is null or empty");

            Preconditions.checkArgument(AesUtil.decForTd(dbPassword) != null,
                "dbPassword decrypt error");

            Preconditions.checkArgument(!Strings.isNullOrEmpty(tableToTopicMap),
                "tableToTopicMap is null or empty");

            Utility.isUrlAddressValid(masterAddress, "masterAddress");

            if (useAvro) {
                // test.test:test123:schema1;test.test1:test234:schema2
                Splitter.on(';')
                    .omitEmptyStrings()
                    .trimResults()
                    .split(tableToTopicMap)
                    .forEach(item -> {
                        String[] result = item.split(":");
                        Preconditions.checkArgument(result.length == 3,
                            "tableToTopicMap format incorrect eg: db.tbl1:topic1:schema1");

                        Preconditions.checkArgument(!Strings.isNullOrEmpty(result[0].trim()),
                            "db.table cannot empty");
                        Preconditions.checkArgument(!Strings.isNullOrEmpty(result[1].trim()),
                            "topic cannot empty");
                        Preconditions.checkArgument(!Strings.isNullOrEmpty(result[2].trim()),
                            "schema cannot empty");
                    });
            } else {
                // test.test:test123;test.test1:test234
                Splitter.on(';')
                    .omitEmptyStrings()
                    .trimResults()
                    .split(tableToTopicMap)
                    .forEach(item -> {
                        String[] result = item.split(":");
                        Preconditions.checkArgument(result.length == 2,
                            "tableToTopicMap format "
                                + "incorrect eg:db.tbl1:topic1;db.tbl2:topic2");

                        Preconditions.checkArgument(!Strings.isNullOrEmpty(result[0].trim()),
                            "db.table cannot empty");
                        Preconditions.checkArgument(!Strings.isNullOrEmpty(result[1].trim()),
                            "topic cannot empty");
                    });

            }

            if (useAvro && multiTopicJob) {
                Preconditions.checkArgument(!Strings.isNullOrEmpty(tableFieldsFilter),
                    "tableFieldsFilter is null or empty");

                Preconditions.checkArgument(StringUtils.countMatches(tableToTopicMap, ";")
                        == StringUtils.countMatches(tableFieldsFilter, ";"),
                    "tableToTopicMap 和 tableFieldSchemaMap参数个数配置不一致");
                Splitter.on(';')
                    .omitEmptyStrings()
                    .trimResults()
                    .split(tableFieldsFilter)
                    .forEach(item -> Splitter.on(",")
                        .omitEmptyStrings()
                        .trimResults()
                        .split(item)
                        .forEach(field -> {
                            String[] fieldTableSchema = field.split("\\|");
                            Preconditions.checkArgument(fieldTableSchema.length == 2,
                                "tableFieldsFilter 格式错误 eg: id|id1,name|name1");

                            Preconditions.checkArgument(
                                !Strings.isNullOrEmpty(fieldTableSchema[0].trim()),
                                "table field cannot empty");
                            Preconditions.checkArgument(
                                !Strings.isNullOrEmpty(fieldTableSchema[1].trim()),
                                "schema field cannot empty");
                        }));
            }

        }

        /**
         * 关键: 当前默认将 masterAddress 作为 canal instance 名.
         *
         * @return the instance
         */
        public String getInstance() {
            if (Strings.isNullOrEmpty(masterAddress)) {
                return null;
            } else {
                return masterAddress.replace(":", "-").replace(".", "_");
            }
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

        /**
         * Gets table topic schema map.
         *
         * @return the table topic schema map
         */
        public String getTableToTopicMap() {
            return tableToTopicMap;
        }

        /**
         * Sets table topic schema map.
         *
         * @param tableToTopicMap the table topic schema map
         */
        public void setTableToTopicMap(String tableToTopicMap) {
            this.tableToTopicMap = tableToTopicMap;
        }

        /**
         * Gets table field schema map.
         *
         * @return the table field schema map
         */
        public String getTableFieldsFilter() {
            return tableFieldsFilter;
        }

        /**
         * Sets table field schema map.
         *
         * @param tableFieldsFilter the table field schema map
         */
        public void setTableFieldsFilter(String tableFieldsFilter) {
            this.tableFieldsFilter = tableFieldsFilter;
        }

        @Override
        public int hashCode() {
            return masterAddress.hashCode();
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
                return masterAddress.equals(((Unit) obj).getMasterAddress());
            }
            return false;
        }
    }
}
