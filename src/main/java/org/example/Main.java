package org.example;
import org.example.JetStream;
import org.example.Server;


public class Main {
    public static void main(String[] args) {
        new Thread(Server::server).start();
        JetStream.start();
    }
}
