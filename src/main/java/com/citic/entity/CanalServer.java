package com.citic.entity;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;


/*
* Canal Server 配置获取实体类
*/
public class CanalServer {
    // canal server
    private String zkServers;

    private Set<CanalInstance> instances = Sets.newHashSet();

    /*
    * 在已有的Canal Server中增加instance
    * */
    public void addOrReplaceInstance(CanalInstance instance) {
        instances.remove(instance);
        instances.add(instance);
    }

    public Set<CanalInstance> getInstances() {
        return instances;
    }

    public void setInstances(Set<CanalInstance> instanceSet) {
        this.instances = instanceSet;
    }

    public String getZkServers() {
        return zkServers;
    }

    public void setZkServers(String zkServers) {
        this.zkServers = zkServers;
    }

    /*
    * velocity 模板 destinations
    * */
    public String getDestinations() {
        List<String> instancesNames = Lists.newArrayList();
        instances.forEach(instance -> instancesNames.add(instance.getInstance()));
        return Joiner.on(",").skipNulls().join(instancesNames);
    }

    @Override
    public String toString() {
        return "CanalServer{" +
                "zkServers='" + zkServers + '\'' +
                '}';
    }
}
