package com.smoly.experimental.sockets.core;

import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.smoly.experimental.sockets.core.app.Config;
import com.smoly.experimental.sockets.core.common.Controller;
import com.smoly.experimental.sockets.core.common.annotations.RouterAction;
import com.smoly.experimental.sockets.core.http.Context;
import com.smoly.experimental.sockets.core.http.HandlerType;
import com.smoly.experimental.sockets.core.http.HttpRequest;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class WebApp {

    protected final Provider<Dispatcher> dispatcherProvider;
    final Set<Controller> registeredControllers;
    final Executor executor;
    AtomicInteger createdDispatchersCount;
    BlockingQueue<Dispatcher> dispatchersPool;
    protected final RouterMap routerMap;
    protected Config config;
    protected Injector mainInjector;

    @Inject
    public WebApp(Config config, RouterMap routerMap, Set<Controller> registeredControllers,
                  Provider<Dispatcher> dispatcherProvider, Executor executor) {
        this.dispatcherProvider = dispatcherProvider;
        this.routerMap = routerMap;
        this.registeredControllers = registeredControllers;
        this.config = config;
        this.executor = executor;
        createdDispatchersCount = new AtomicInteger();
        dispatchersPool = new ArrayBlockingQueue<>(config.getMaxThreads());
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
        ServerSocket server = new ServerSocket(config.getPort());
        while (true) {
            Socket clientSocket = server.accept();
            HttpRequest request = new HttpRequest(clientSocket);
            Context ctx = new Context(clientSocket, request);
            executor.execute(
                    getRequestHandlerTask(ctx)
            );
        }
    }

    Runnable getRequestHandlerTask (Context ctx) {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println("Start handing request");
                Dispatcher requestDispatcher = null;
                try {
                    if (dispatchersPool.size() == 0 && createdDispatchersCount.get() < config.getMaxThreads()) {
                        requestDispatcher = dispatcherProvider.get();
                        createdDispatchersCount.getAndIncrement();
                    } else {
                        requestDispatcher = dispatchersPool.take();
                    }
                    requestDispatcher.processRequest(ctx);
                } catch (Exception e) {
                    System.out.println("Error in request processing");
                } finally {
                    dispatchersPool.add(requestDispatcher);
                }
            }
        };
    }
}
