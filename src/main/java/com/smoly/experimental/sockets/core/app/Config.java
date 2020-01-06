package com.smoly.experimental.sockets.core.app;

public class Config {
    protected int maxThreads;
    protected int port;

    public Config(int maxThreads, int port) {
        this.maxThreads = maxThreads;
        this.port = port;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getPort() {
        return port;
    }

}
