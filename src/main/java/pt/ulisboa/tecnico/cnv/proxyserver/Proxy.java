package pt.ulisboa.tecnico.cnv.proxyserver;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.Scanner;

import com.sun.net.httpserver.HttpServer;

import org.apache.log4j.Logger;

import pt.ulisboa.tecnico.cnv.httpserver.Path;
import pt.ulisboa.tecnico.cnv.proxyserver.BalancerRoundRobin;
import pt.ulisboa.tecnico.cnv.proxyserver.Scaler;
import pt.ulisboa.tecnico.cnv.proxyserver.handlers.HandleFactorize;

public class Proxy {
    final static Logger logger = Logger.getLogger(Proxy.class);
    private static int PORT = 8080;
    private static int POOL_SIZE = 10;
    private static HttpServer server = null;
    private static BalancerRoundRobin balancer = null;
    private static Scaler scaler = null;

    public Proxy() throws Exception {
        // logger.info("Setting hook for TERM signal.");
        // Runtime.getRuntime().addShutdownHook(new Thread() {
        //     public void run() {
        //         logger.info("Got TERM signal on Proxy.");
        //         //scaler.stopRunning();
        //     }
        // });

        balancer = new BalancerRoundRobin();
        scaler = new Scaler(balancer);
    }

    public static void main(String[] args) {
        try {
            Proxy proxy = new Proxy();
            proxy.start();

            // Hold here for input
            System.in.read();

            proxy.stop();
        } catch (Exception e) {
            logger.fatal("Exception raised while launching Proxy:");
            logger.fatal(e);
        }
    }

    private void stop() throws Exception {
        logger.info("Stopping Proxy...");
        scaler.stopRunning();
        scaler.join();
        server.stop(0);
        logger.info("Proxy Stopped.");
    }

    public void start() throws Exception {
        logger.info("Launching Scaler...");
        scaler.start();

        logger.info("Launching Proxy at port " + PORT + "...");
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext(Path.FACTORIZE, new HandleFactorize(balancer));
        server.setExecutor(Executors.newFixedThreadPool(POOL_SIZE));
        server.start();

        logger.info("Proxy started.");
    }
}
