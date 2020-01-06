package com.smoly.experimental.sockets.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smoly.experimental.sockets.core.http.Context;
import com.smoly.experimental.sockets.core.http.HandlerType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Singleton
public class RouterMap {
    protected Map<String, Consumer<Context>> routersMap;
    @Inject
    public RouterMap() {
        routersMap = new HashMap<>();
    }

    public void addHandler(HandlerType requestType, String path, Consumer<Context> action) {
        routersMap.put(path, action);
    }

    public boolean isHandlerForPathExists(String path) {
        return routersMap.containsKey(path);
    }

    public Consumer<Context> getHandlerForPath(String path) {
        return routersMap.get(path);
    }
}
