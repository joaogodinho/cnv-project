package pt.ulisboa.tecnico.cnv.balancerserver.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.math.BigInteger;
import java.util.ArrayList;

public class HandleFactorize implements HttpHandler {
    final static Logger logger = Logger.getLogger(HandleFactorize.class);

    final static String FORM = "<html><body><form method='get'>Number: <input type='text' name='n'></input><br><input type='submit'></input></form></body></html>";

    public HandleFactorize() {
        super();
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
                // TODO Pick a httpserver to send the request
                // and wait for the reply, then send as a response
                String tempReply = "IT'S BEING DEVELOPED, CHILL BRO";
                t.sendResponseHeaders(200, tempReply.length());
                OutputStream os = t.getResponseBody();
                os.write(tempReply.getBytes());
                os.close();
                try {

                } catch (Exception e) {
                    logger.fatal("Got exception when trying to factorize:");
                    logger.fatal(e);
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
}
