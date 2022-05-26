package com.smoly.experimental.sockets.core;

import com.google.inject.Inject;
import com.smoly.experimental.sockets.core.http.Context;

import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class Dispatcher {
    protected final RouterMap routerMap;
    @Inject
    public Dispatcher(RouterMap routerMap) {
        this.routerMap = routerMap;
    }

    void processRequest(Context ctx) throws InterruptedException, IOException {
        System.out.println("Request was processed");
        if (routerMap.isHandlerForPathExists(ctx.getRequest().getPath())) {
            System.out.println("Router found: " + ctx.getRequest().getPath() );
            routerMap.routersMap.get(ctx.getRequest().getPath()).accept(ctx);
        } else {
           throw new IllegalArgumentException("Unknown route!" + ctx.getRequest().getPath());
        }
    }
}
