package com.smoly.experimental.sockets.user;
import com.google.inject.Inject;
import com.smoly.experimental.sockets.core.http.Context;
import com.smoly.experimental.sockets.core.common.Controller;
import com.smoly.experimental.sockets.core.common.annotations.RouterAction;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Singleton
public class UserController implements Controller {
    @Inject
    public UserController() {
    }

    @RouterAction(path="/hello")
    public void index(Context ctx) throws IOException {
        ctx.respond("Hello World");
    }

    @RouterAction(path="/async")
    public void asyncAction(Context ctx) throws IOException {
        final CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Async respond with delay 2500 ms";
        });
        ctx.respond(completableFuture);
    }

}
