package dev.nachwahl.btemap.utils;

import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;

import static java.util.Collections.singletonMap;

public class SocketIO {

    Socket socket;
    public SocketIO(String host, int port, String token){
        IO.Options options = IO.Options.builder()
                .setAuth(singletonMap("token", token))
                .build();


        try {
            this.socket = IO.socket(host+":"+port, options);
            this.socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void sendPlayerLocationUpdate(String data) {
        System.out.println("Update: "+data);
        this.socket.emit("playerLocationUpdate", data);
    }

    public void closeSocket() {
        this.socket.close();
    }




}