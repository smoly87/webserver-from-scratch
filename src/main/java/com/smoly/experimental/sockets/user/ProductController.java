package com.smoly.experimental.sockets.user;

import com.google.inject.Inject;
import com.smoly.experimental.sockets.core.common.Controller;
import com.smoly.experimental.sockets.core.common.annotations.RouterAction;
import com.smoly.experimental.sockets.core.http.Context;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Singleton
public class ProductController implements Controller {
    @Inject
    public ProductController() {
    }

    //@GetMapping("/products")
    @RouterAction(path="/products")
    public void productsList(Context ctx) throws IOException {
        ctx.respond("Products list");
    }

    @RouterAction(path="/products/exception")
    public void productsWithException(Context ctx) throws IOException {
        throw new RuntimeException("Something goes wrong");
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
