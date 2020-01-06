package com.smoly.experimental.sockets.app;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.smoly.experimental.sockets.core.app.Config;
import com.smoly.experimental.sockets.user.UserModule;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new UserModule());
    }

    @Provides
    @Singleton
    Config provideConfig() {
        return new Config( 100, 7000 );
    }

    @Provides
    @Singleton
    Executor provideExecutor(Config config) {
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(config.getMaxThreads());
    }

}
