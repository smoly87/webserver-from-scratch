package com.smoly.experimental.sockets.app;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.smoly.experimental.sockets.core.app.Config;
import com.smoly.experimental.sockets.user.UserModule;

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
}
