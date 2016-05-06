package pt.ulisboa.tecnico.cnv.proxyserver;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import org.apache.log4j.Logger;

import pt.ulisboa.tecnico.cnv.httpserver.Path;
import pt.ulisboa.tecnico.cnv.proxyserver.Balancer;
import pt.ulisboa.tecnico.cnv.proxyserver.Scaler;

public class Proxy {
    final static Logger logger = Logger.getLogger(Proxy.class);
    private static int PORT = 80;
    private static int POOL_SIZE = 10;
    private static HttpServer server = null;
    private static Proxy proxy = null;
    private static Balancer balancer = null;
    private static Scaler scaler = null;

    public Proxy() throws Exception {
        logger.info("Setting hook for TERM signal.");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info("Got TERM signal");
                proxy.stop();
            }
        });

        balancer = new Balancer();
        scaler = new Scaler();

        this.start();
    }

    public static void main(String[] args) {
        try {
            proxy = new Proxy();
        } catch (Exception e) {
            logger.fatal("Exception raised while launching Proxy:");
            logger.fatal(e);
        }
    }

    private void stop() {
        server.stop(0);
    }

    private void start() throws Exception {
        logger.info("Launching Scaler and Balancer...");
        // TODO launch scaler and balancer threads

        logger.info("Launching Proxy at port " + PORT + "...");
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        // TODO set factorize context
        // server.createContext(Path.FACTORIZE, new HandleFactorize());
        server.setExecutor(Executors.newFixedThreadPool(POOL_SIZE));
        server.start();
        logger.info("Proxy started.");
    }
}
