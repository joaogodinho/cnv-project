package pt.ulisboa.tecnico.cnv.proxyserver;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Scanner;

import com.sun.net.httpserver.HttpServer;

import org.apache.log4j.Logger;

import pt.ulisboa.tecnico.cnv.httpserver.Path;
import pt.ulisboa.tecnico.cnv.proxyserver.balancer.Balancer;
import pt.ulisboa.tecnico.cnv.proxyserver.balancer.SmartBalancer;
import pt.ulisboa.tecnico.cnv.proxyserver.balancer.RoundRobinBalancer;
import pt.ulisboa.tecnico.cnv.proxyserver.Scaler;
import pt.ulisboa.tecnico.cnv.proxyserver.handlers.HandleFactorize;

public class Proxy {
    final static Logger logger = Logger.getLogger(Proxy.class);
    private static int PORT = 8080;
    private static int POOL_SIZE = 100;
    private HttpServer server = null;
    private ExecutorService serverPool = null;

    private static Balancer balancer = null;
    private static Scaler scaler = null;

    public Proxy() throws Exception {
        //balancer = new RoundRobinBalancer();
        balancer = new SmartBalancer();
        scaler = new Scaler(balancer);
        balancer.setScaler(scaler);
        serverPool = Executors.newFixedThreadPool(POOL_SIZE);
    }

    public static void main(String[] args) {
        try {
            Proxy proxy = new Proxy();
            DynamoConnecter.createTables();
            proxy.start();

            // Hold here for input
            System.in.read();

            proxy.terminate();
            System.exit(0);
        } catch (Exception e) {
            logger.fatal("Exception raised while running Proxy:");
            logger.fatal(e);
        }
    }

    private void terminate() throws Exception {
        logger.info("Stopping Proxy...");
        scaler.stopRunning();
        scaler.join();
        server.stop(1);
        serverPool.shutdown();
        logger.info("Proxy Stopped.");
    }

    public void start() throws Exception {
        // logger.info("Launching Scaler...");
        scaler.start();

        logger.info("Launching Proxy at port " + PORT + "...");
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext(Path.FACTORIZE, new HandleFactorize(scaler, balancer));
        server.setExecutor(Executors.newFixedThreadPool(POOL_SIZE));
        server.start();

        logger.info("Proxy started.");
    }
}
