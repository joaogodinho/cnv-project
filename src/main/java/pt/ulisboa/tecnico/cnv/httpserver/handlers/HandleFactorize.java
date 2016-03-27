package pt.ulisboa.tecnico.cnv.httpserver.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.math.BigInteger;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cnv.factorization.IntFactorization;

public class HandleFactorize implements HttpHandler {
    final static Logger logger = Logger.getLogger(HandleFactorize.class);

    final static String FORM = "<html><body><form method='post'>Number: <input type='text' name='number'></input><br><input type='submit'></input></form></body></html>";

    public HandleFactorize() {
        super();
        logger.info("Setting context for Factorize");
    }
    @Override
    public void handle(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("GET")) {
            logger.info("GET request from " + t.getRequestURI());
            t.sendResponseHeaders(200, FORM.length());
            OutputStream os = t.getResponseBody();
            os.write(FORM.getBytes());
            os.close();
        } else if (t.getRequestMethod().equals("POST")) {
            logger.info("POST request from " + t.getRequestURI());

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(t.getRequestBody()));
            String inputNumber = bufferedReader.readLine().split("=")[1];
            logger.info("Requested number: " + inputNumber);

            IntFactorization intFact = new IntFactorization();
            try {
                ArrayList<BigInteger> result = intFact.calcPrimeFactors(new BigInteger(inputNumber));
                t.sendResponseHeaders(200, result.toString().length());
                OutputStream os = t.getResponseBody();
                os.write(result.toString().getBytes());
                os.close();
            } catch (Exception e) {
                logger.fatal("Got exception when trying to factorize:");
                logger.fatal(e);
                t.sendResponseHeaders(500, 0);
                t.getResponseBody().close();
                return;
            }

            t.sendResponseHeaders(200, 0);
            t.getResponseBody().close();
        } else {
            logger.warn("Unsupported method");
            t.sendResponseHeaders(405, 0);
            t.getResponseBody().close();
        }
    }
}
