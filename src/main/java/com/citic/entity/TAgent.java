package com.citic.entity;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;


/*
* TAgent 配置获取实体类
*/
public class TAgent {
    // 这个属性是个Source 使用的,但是当前的设计,所有source 共有一个zookeeper集群
    // 因此提取到上一层
    private String sourceZkServers;
    private String sinkServers;
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
        return Joiner.on(",").skipNulls().join(sourceNames);
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

    /*
        * sourceDestination 确保唯一 Source
        * */
    public static class Source {
        private String sourceDestination;

        private String tableFilter;
        private String tableToTopicMap;
        private String tableFieldsFilter;

        public Source(UnionConfig.Unit unit) {
            sourceDestination = unit.getInstance();
            tableFilter = unit.getTableFilter();
            tableToTopicMap = unit.getTableToTopicMap();
            tableFieldsFilter = unit.getTableFieldsFilter();
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
            return sourceDestination.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Source && sourceDestination.equals(((Source) obj).sourceDestination);
        }

    }

}
