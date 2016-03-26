package pt.ulisboa.tecnico.cnv.httpserver;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import pt.ulisboa.tecnico.cnv.httpserver.Path;
import pt.ulisboa.tecnico.cnv.httpserver.handlers.HandleIndex;

public class HTTPServer {
    private static int PORT = 8080;
    private static int POOL_SIZE = 10;

    public HTTPServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext(Path.INDEX, new HandleIndex());
        server.setExecutor(Executors.newFixedThreadPool(POOL_SIZE));
        server.start();
    }

    public static void main(String[] args) throws Exception {
        new HTTPServer();
    }
}
