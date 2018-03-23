package com.citic.entity;

public class AllConfig {
    private CanalInstance canalInstance;
    private CanalServer canalServer;
    private TAgent tagent;

    public CanalInstance getCanalInstance() {
        return canalInstance;
    }

    public void setCanalInstance(CanalInstance canalInstance) {
        this.canalInstance = canalInstance;
    }

    public CanalServer getCanalServer() {
        return canalServer;
    }

    public void setCanalServer(CanalServer canalServer) {
        this.canalServer = canalServer;
    }

    public TAgent getTagent() {
        return tagent;
    }

    public void setTagent(TAgent tagent) {
        this.tagent = tagent;
    }

    @Override
    public String toString() {
        return "AllConfig{" +
                "canalInstance=" + canalInstance +
                ", canalServer=" + canalServer +
                ", tagent=" + tagent +
                '}';
    }
}
