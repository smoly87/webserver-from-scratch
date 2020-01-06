package com.smoly.experimental.sockets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.smoly.experimental.sockets.app.AppModule;
import com.smoly.experimental.sockets.core.WebApp;
import com.smoly.experimental.sockets.user.UserController;

import java.io.IOException;

public class MainApp {
    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new AppModule());
        WebApp app = injector.getInstance(WebApp.class);
        app.boot(args, injector);
        app.registerControllerRoutes(UserController.class);
        app.start();
    }

}
