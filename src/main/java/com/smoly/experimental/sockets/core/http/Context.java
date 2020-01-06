package com.smoly.experimental.sockets.core.http;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class Context {
    protected HttpRequest request;
    protected Socket clientSocket;
    public Context(Socket clientSocket, HttpRequest request) {
        this.clientSocket = clientSocket;
        this.request = request;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void respond(String response) throws IOException {
        String httpResponse = "HTTP/1.1 200 OK\r\n\r\n " + response  ;
        clientSocket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
        clientSocket.getOutputStream().flush();
        clientSocket.close();
    }

    public void respond(CompletableFuture<String> responseFeature) {
        responseFeature.thenAccept(respondResult -> {
            try {
                this.respond(respondResult);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
