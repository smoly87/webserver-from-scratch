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
        respond(response, 200, "OK");
    }
    public void respond(String response, int code, String codeMessage){
        String httpResponse = String.format("HTTP/1.1 %d %s\r\n\r\n ",code, codeMessage ) + response  ;
        try {
            clientSocket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
            clientSocket.getOutputStream().flush();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
