package com.citic.entity;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;


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

    // 有作用,velocity 模板生成的时候通过反射获取字段名

    public void addOrReplaceSource(Source source) {
        sources.remove(source);
        sources.add(source);
    }

    /*
     * velocity 模板 sourceNames
     * */
    public String getSourceNames() {
        List<String> sourceNames = Lists.newArrayList();
        sources.forEach(source -> sourceNames.add(source.getSourceName()));
        return Joiner.on(" ").skipNulls().join(sourceNames);
    }

    public boolean isUseAvro() {
        return useAvro;
    }

    public void setUseAvro(boolean useAvro) {
        this.useAvro = useAvro;
    }

    public boolean isKafkaHighThroughput() {
        return kafkaHighThroughput;
    }

    public void setKafkaHighThroughput(boolean kafkaHighThroughput) {
        this.kafkaHighThroughput = kafkaHighThroughput;
    }

    public Set<Source> getSources() {
        return sources;
    }

    public void setSources(Set<Source> sources) {
        this.sources = sources;
    }

    public String getSinkServers() {
        return sinkServers;
    }

    public void setSinkServers(String sinkServers) {
        this.sinkServers = sinkServers;
    }

    public String getSourceZkServers() {
        return sourceZkServers;
    }

    public void setSourceZkServers(String sourceZkServers) {
        this.sourceZkServers = sourceZkServers;
    }

    public String getRegistryUrl() {
        return registryUrl;
    }

    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }

    /*
     * sourceDestination 确保唯一 Source
     * */
    public static class Source {

        private String sourceDestination;
        private String tableToTopicMap;
        private String tableFieldsFilter;

        public Source(UnionConfig.Unit unit) {
            sourceDestination = unit.getInstance();
            tableToTopicMap = unit.getTableTopicSchemaMap();
            tableFieldsFilter = unit.getTableFieldSchemaMap();
        }

        /*
         * 当前就将 destination 作为 sourceName
         * velocity 模板 sourceName
         * */
        public String getSourceName() {
            return sourceDestination;
        }

        public String getSourceDestination() {
            return sourceDestination;
        }

        public void setSourceDestination(String sourceDestination) {
            this.sourceDestination = sourceDestination;
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
