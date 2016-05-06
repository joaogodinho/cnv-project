package pt.ulisboa.tecnico.cnv.proxyserver.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.math.BigInteger;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cnv.proxyserver.balancer.Balancer;

public class HandleFactorize implements HttpHandler {
    final static Logger logger = Logger.getLogger(HandleFactorize.class);

    final static String FORM = "<html><body><form method='get'>Number: <input type='text' name='n'></input><br><input type='submit'></input></form></body></html>";

    private Balancer balancer;

    public HandleFactorize(Balancer balancer) {
        super();
        this.balancer = balancer;
        logger.info("Setting context for Factorize");
    }
    @Override
    public void handle(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("GET")) {
            logger.info("GET request from " + t.getRequestURI());
            String query = t.getRequestURI().getQuery();
            logger.info("Params are: " + query);

            if (query != null) {
                String inputNumber = query.split("n=")[1];
                String targetDns = balancer.requestInstance(inputNumber);
                logger.info("Got " + targetDns + " target from Balancer");

                String answer = null;
                try {
                    answer = doRequest(targetDns, inputNumber);
                } catch (Exception e) {
                    logger.fatal("Got exception when requesting answer from worker:");
                    logger.fatal(e);
                }

                if (answer != null) {
                    t.sendResponseHeaders(200, targetDns.length());
                    OutputStream os = t.getResponseBody();
                    os.write(targetDns.getBytes());
                    os.close();
                } else {
                    t.sendResponseHeaders(500, 0);
                    t.getResponseBody().close();
                }
            } else {
                t.sendResponseHeaders(200, FORM.length());
                OutputStream os = t.getResponseBody();
                os.write(FORM.getBytes());
                os.close();
            }
        } else {
            logger.warn("Unsupported method");
            t.sendResponseHeaders(405, 0);
            t.getResponseBody().close();
        }
    }

    private String doRequest(String dns, String number) throws Exception {
        String url = "http://" + dns + "/f.html?n=" + number;
        logger.info("Doing request to: " + url);

        URL worker = new URL(url);
        URLConnection wc = worker.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(wc.getInputStream()));

        String answer = in.readLine();
        in.close();
        return answer;
    }
}
