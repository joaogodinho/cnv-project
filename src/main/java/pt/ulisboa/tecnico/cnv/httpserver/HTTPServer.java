package pt.ulisboa.tecnico.cnv.httpserver;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import org.apache.log4j.Logger;

import pt.ulisboa.tecnico.cnv.httpserver.Path;
import pt.ulisboa.tecnico.cnv.httpserver.handlers.HandleIndex;
import pt.ulisboa.tecnico.cnv.httpserver.handlers.HandleFactorize;

public class HTTPServer {
    final static Logger logger = Logger.getLogger(HTTPServer.class);
    private static int PORT = 8080;
    private static int POOL_SIZE = 10;

    public HTTPServer() throws Exception {
        logger.info("Launching HTTPServer at port " + PORT + "...");
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext(Path.INDEX, new HandleIndex());
        server.createContext(Path.FACTORIZE, new HandleFactorize());

        server.setExecutor(Executors.newFixedThreadPool(POOL_SIZE));
        server.start();
        logger.info("HTTPServer started.");
    }

    public static void main(String[] args) {
        try {
            new HTTPServer();
        } catch (Exception e) {
            logger.fatal("Exception raised while launching HTTPServer:");
            logger.fatal(e);
        }
    }
}
