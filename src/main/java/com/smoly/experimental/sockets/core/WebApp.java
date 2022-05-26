package com.smoly.experimental.sockets.core;

import com.google.inject.Injector;
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

@Singleton
public class WebApp {
    private final Set<Controller> registeredControllers;
    private final Executor executor;
    private final Dispatcher requestDispatcher;
    private final RouterMap routerMap;
    private final Config config;
    private Injector mainInjector;

    private BlockingQueue<Socket> clientRequestsQueue = new LinkedBlockingDeque<>(200);

    @Inject
    public WebApp(Config config, RouterMap routerMap, Set<Controller> registeredControllers,
                  Dispatcher requestDispatcher, Executor executor) {
        this.requestDispatcher = requestDispatcher;
        this.routerMap = routerMap;
        this.registeredControllers = registeredControllers;
        this.config = config;
        this.executor = executor;
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
        Thread daemonThread = new Thread(getClientRequestHandlerTask());
        daemonThread.setDaemon(true);
        daemonThread.start();

        System.out.println("Listening for connection on port " + config.getPort());
        ServerSocket server = new ServerSocket(config.getPort());
        while (true) {
            Socket clientSocket = server.accept();
            clientRequestsQueue.offer(clientSocket);
        }
    }


    private Runnable getRequestHandlerTask (Context ctx) {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println("Start handing request");
                try {
                    requestDispatcher.processRequest(ctx);
                } catch (Exception e) {
                    System.out.println("Error in request processing");
                }
            }
        };
    }

    private Runnable getClientRequestHandlerTask() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Socket clientSocket = clientRequestsQueue.take();
                        HttpRequest request = new HttpRequest(clientSocket);
                        Context ctx = new Context(clientSocket, request);
                        executor.execute(getRequestHandlerTask(ctx));
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException("Request processing fail due to interrupting exception");
                } catch (IOException e) {
                    throw new RuntimeException("Request processing fail");
                }
            }
        };
    }
}
