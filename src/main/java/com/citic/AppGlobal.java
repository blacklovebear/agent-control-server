package com.citic;

import com.citic.entity.CanalServer;
import com.citic.entity.TAgent;


public class AppGlobal {
    private static TAgent tAgent;
    private static CanalServer canalServer;

    public static TAgent getTAgent() {
        return tAgent;
    }

    public static void setTAgent(TAgent tAgent) {
        AppGlobal.tAgent = tAgent;
    }

    public static CanalServer getCanalServer() {
        return canalServer;
    }

    public static void setCanalServer(CanalServer canalServer) {
        AppGlobal.canalServer = canalServer;
    }
}
