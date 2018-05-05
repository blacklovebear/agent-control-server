package com.citic;

import com.citic.entity.CanalServer;
import com.citic.entity.TAgent;
import com.citic.entity.UnionConfig;


public class AppGlobal {

    private static TAgent tAgent;
    private static CanalServer canalServer;

    private static UnionConfig unionConfig;

    public static UnionConfig getUnionConfig() {
        return unionConfig;
    }

    public static void setUnionConfig(UnionConfig unionConfig) {
        AppGlobal.unionConfig = unionConfig;
    }
}
