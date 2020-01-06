package com.smoly.experimental.sockets.user;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.smoly.experimental.sockets.core.common.Controller;

public class UserModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), Controller.class).addBinding().to(UserController.class);
    }
}
