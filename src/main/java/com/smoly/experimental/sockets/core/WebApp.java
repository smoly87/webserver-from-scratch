package com.smoly.experimental.sockets.core;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.smoly.experimental.sockets.core.app.Config;
import com.smoly.experimental.sockets.core.common.Controller;
import com.smoly.experimental.sockets.core.common.annotations.RouterAction;
import com.smoly.experimental.sockets.core.http.Context;
import com.smoly.experimental.sockets.core.http.HandlerType;
import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.*;

@Singleton
public class WebApp {

    protected final Set<Controller> registeredControllers;
    protected final Executor executor;

    protected final RouterMap routerMap;
    protected final Config config;
    protected Injector mainInjector;
    protected final WebRequestProcessor requestProcessor;

    @Inject
    public WebApp(Config config, RouterMap routerMap, Set<Controller> registeredControllers,
                   Executor executor, WebRequestProcessor requestProcessor) {
        this.routerMap = routerMap;
        this.registeredControllers = registeredControllers;
        this.config = config;
        this.executor = executor;
        this.requestProcessor = requestProcessor;
    }

    public void boot(String[] args, Injector mainInjector) {
        this.mainInjector = mainInjector;
    }

    public void registerControllerRoutes(Class controllerClass) {
        Arrays.stream(controllerClass
                .getMethods())
                .filter(method -> method.isAnnotationPresent(RouterAction.class)).forEach(method -> {
            RouterAction routerAnnotation = method.getAnnotation(RouterAction.class);
            routerMap.addHandler(HandlerType.GET, routerAnnotation.path(), (Context ctx) -> {
                try {
                    method.invoke(mainInjector.getInstance(controllerClass), ctx);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public void start() throws IOException {
        System.out.println("Listening for connection on port " + config.getPort());
        executor.execute(requestProcessor);
        ServerSocket server = new ServerSocket(config.getPort());
        while (true) {
            Socket clientSocket = server.accept();
            requestProcessor.addRequest(clientSocket);
        }
    }


}
