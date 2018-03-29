package com.citic.entity;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/*
* TAgent 配置获取实体类
*/
public class TAgent {
    private Set<Source> sources;
    private Sink sink;

    private String sourceNames;

    public void addSource(Source source) {
        sources.add(source);
    }

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

    public Sink getSink() {
        return sink;
    }

    public void setSink(Sink sink) {
        this.sink = sink;
    }

    /*
    * sourceDestination 确保唯一 Source
    * */
    public static class Source {
        private String sourceZkServers;
        private String sourceDestination;
        // 有作用,velocity 模板生成的时候通过反射获取字段名
        private String sourceName;


        private String tableFilter;
        private String tableToTopicMap;
        private String tableFieldsFilter;

        public Source() {}

        /*
        * 当前就将 destination 作为 sourceName
        * */
        public String getSourceName() {
            return sourceDestination;
        }

        public String getSourceZkServers() {
            return sourceZkServers;
        }

        public void setSourceZkServers(String sourceZkServers) {
            this.sourceZkServers = sourceZkServers;
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
        public boolean equals(Object obj){
            return sourceDestination.equals(((Source)obj).sourceDestination);
        }

    }

    public static class Sink {
        public Sink() {}

        private String sinkServers;

        public String getSinkServers() {
            return sinkServers;
        }

        public void setSinkServers(String sinkServers) {
            this.sinkServers = sinkServers;
        }
    }
}
