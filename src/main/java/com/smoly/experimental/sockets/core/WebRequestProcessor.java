package com.smoly.experimental.sockets.core;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.smoly.experimental.sockets.core.app.Config;
import com.smoly.experimental.sockets.core.http.Context;
import com.smoly.experimental.sockets.core.http.HttpRequest;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class WebRequestProcessor implements Runnable{
    protected final AtomicInteger createdDispatchersCount;
    protected final BlockingQueue<Dispatcher> dispatchersPool;
    protected final BlockingQueue<Socket> clientSocketsQueue;
    protected Provider<Dispatcher> dispatcherProvider;
    protected final Executor executor;
    protected final Config config;

    @Inject
    public WebRequestProcessor(Config config, Provider<Dispatcher> dispatcherProvider, Executor executor) {
        this.config = config;
        this.dispatcherProvider = dispatcherProvider;
        this.executor = executor;
        createdDispatchersCount = new AtomicInteger();
        dispatchersPool = new ArrayBlockingQueue<>(config.getMaxThreads());
        clientSocketsQueue = new LinkedBlockingQueue<>();
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

    @Override
    public void run() {
        while(true) {
            try {
                Socket socket = clientSocketsQueue.take();
                HttpRequest request = new HttpRequest(socket);
                Context ctx = new Context(socket, request);
                executor.execute(getRequestHandlerTask(ctx));
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addRequest(Socket clientSocket) {
        clientSocketsQueue.offer(clientSocket);
    }
}
