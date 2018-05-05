package com.citic.entity;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;


/**
 * The type T agent.
 */
/*
 * TAgent 配置获取实体类
 */
public class TAgent {

    // 这个属性是个Source 使用的,但是当前的设计,所有source 共有一个zookeeper集群
    // 因此提取到上一层
    private String sourceZkServers;
    private String sinkServers;
    private String registryUrl;
    private boolean kafkaHighThroughput;
    private boolean useAvro;

    private Set<Source> sources = Sets.newHashSet();

    /**
     * Add or replace source.
     *
     * @param source the source
     */
    public void addOrReplaceSource(Source source) {
        sources.remove(source);
        sources.add(source);
    }

    /**
     * velocity 模板 sourceNames.
     *
     * @return the source names
     */
    public String getSourceNames() {
        List<String> sourceNames = Lists.newArrayList();
        sources.forEach(source -> sourceNames.add(source.getSourceName()));
        return Joiner.on(" ").skipNulls().join(sourceNames);
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
     * Is kafka high throughput boolean.
     *
     * @return the boolean
     */
    public boolean isKafkaHighThroughput() {
        return kafkaHighThroughput;
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
     * Gets sources.
     *
     * @return the sources
     */
    public Set<Source> getSources() {
        return sources;
    }

    /**
     * Sets sources.
     *
     * @param sources the sources
     */
    public void setSources(Set<Source> sources) {
        this.sources = sources;
    }

    /**
     * Gets sink servers.
     *
     * @return the sink servers
     */
    public String getSinkServers() {
        return sinkServers;
    }

    /**
     * Sets sink servers.
     *
     * @param sinkServers the sink servers
     */
    public void setSinkServers(String sinkServers) {
        this.sinkServers = sinkServers;
    }

    /**
     * Gets source zk servers.
     *
     * @return the source zk servers
     */
    public String getSourceZkServers() {
        return sourceZkServers;
    }

    /**
     * Sets source zk servers.
     *
     * @param sourceZkServers the source zk servers
     */
    public void setSourceZkServers(String sourceZkServers) {
        this.sourceZkServers = sourceZkServers;
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
     * sourceDestination 确保唯一 Source.
     */
    public static class Source {

        private String sourceDestination;
        private String tableToTopicMap;
        private String tableFieldsFilter;

        /**
         * Instantiates a new Source.
         *
         * @param unit the unit
         */
        public Source(UnionConfig.Unit unit) {
            sourceDestination = unit.getInstance();
            tableToTopicMap = unit.getTableTopicSchemaMap();
            tableFieldsFilter = unit.getTableFieldSchemaMap();
        }

        /**
         * 当前就将 destination 作为 sourceName.
         * velocity 模板 sourceName.
         * @return the source name
         */
        public String getSourceName() {
            return sourceDestination;
        }

        /**
         * Gets source destination.
         *
         * @return the source destination
         */
        public String getSourceDestination() {
            return sourceDestination;
        }

        /**
         * Sets source destination.
         *
         * @param sourceDestination the source destination
         */
        public void setSourceDestination(String sourceDestination) {
            this.sourceDestination = sourceDestination;
        }

        /**
         * Gets table to topic map.
         *
         * @return the table to topic map
         */
        public String getTableToTopicMap() {
            return tableToTopicMap;
        }

        /**
         * Sets table to topic map.
         *
         * @param tableToTopicMap the table to topic map
         */
        public void setTableToTopicMap(String tableToTopicMap) {
            this.tableToTopicMap = tableToTopicMap;
        }

        /**
         * Gets table fields filter.
         *
         * @return the table fields filter
         */
        public String getTableFieldsFilter() {
            return tableFieldsFilter;
        }

        /**
         * Sets table fields filter.
         *
         * @param tableFieldsFilter the table fields filter
         */
        public void setTableFieldsFilter(String tableFieldsFilter) {
            this.tableFieldsFilter = tableFieldsFilter;
        }

        @Override
        public int hashCode() {
            return sourceDestination.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Source && sourceDestination
                .equals(((Source) obj).sourceDestination);
        }

    }

}
