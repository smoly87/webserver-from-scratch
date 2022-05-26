package com.smoly.experimental.sockets.core.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class HttpRequest {
    private String path;

    public String getPath() {
        return path;
    }

    public HttpRequest(Socket clientSocket) throws IOException {
            InputStreamReader clientSocketInputStream = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader reader = new BufferedReader(clientSocketInputStream);
            String line = reader.readLine();
            while (!line.isEmpty()) {
                if(line.startsWith("GET")) {
                    this.path = line.split(" ")[1];
                }
                line = reader.readLine();
            }
    }
}
