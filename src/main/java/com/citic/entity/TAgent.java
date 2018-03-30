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
    // 这个属性是个Source 使用的,但是当前的设计,所有source 共有一个zookeeper集群
    // 因此提取到上一层
    private String sourceZkServers;
    private String sinkServers;
    private Set<Source> sources = Sets.newHashSet();

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
        // 有作用,velocity 模板生成的时候通过反射获取字段名
        private String sourceName;


        private String tableFilter;
        private String tableToTopicMap;
        private String tableFieldsFilter;
        private Object obj;

        public Source() {}

        public Source(UnionConfig.Unit unit) {
            sourceDestination = unit.getInstance();
            tableFilter = unit.getTableFilter();
            tableToTopicMap = unit.getTableToTopicMap();
            tableFieldsFilter = unit.getTableFieldsFilter();
        }

        /*
        * 当前就将 destination 作为 sourceName
        * */
        public String getSourceName() {
            // 因为 sourceDestination 在上层赋值的时候会将 ip:port 作为值
            // 但是 sourceName 不能出现 "." 符号,因此在此替换
            if (sourceDestination == null)
                return null;
            else
                return sourceDestination.replace(":", "-").replace(".", "_");

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
