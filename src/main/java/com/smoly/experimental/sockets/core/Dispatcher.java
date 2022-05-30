package com.smoly.experimental.sockets.core;

import com.google.inject.Inject;
import com.smoly.experimental.sockets.core.http.Context;

import javax.inject.Singleton;
import java.util.function.Consumer;

@Singleton
public class Dispatcher {
    protected final RouterMap routerMap;
    @Inject
    public Dispatcher(RouterMap routerMap) {
        this.routerMap = routerMap;
    }

    void processRequest(Context ctx) {
        if (routerMap.isHandlerForPathExists(ctx.getRequest().getPath())) {
            System.out.println("Router found: " + ctx.getRequest().getPath() );
            Consumer<Context> handlerFunction = routerMap.getHandlerForPath(ctx.getRequest().getPath());
            handlerFunction.accept(ctx);
        } else {
           throw new PageNotFoundException("Unknown route: " + ctx.getRequest().getPath());
        }
    }
}
