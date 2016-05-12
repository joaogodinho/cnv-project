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
import pt.ulisboa.tecnico.cnv.instrumentation.InstrumentationTool;

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
                String[] values = query.split("&");
                String inputNumber = values[0].split("n=")[1],
                       inputUniqId = values[1].split("id=")[1];

                IntFactorization intFact = new IntFactorization();
                try {
                    // TODO Update Dynamo
                    //HTTPServer.queue.add(inputUniqId,
                    //        Thread.currentThread().getId(),
                    //        DynamoMessenger.INCREMENT_THREADS);
                    logger.info("Starting factorization of " + inputNumber + "...");
                    long startTime = System.currentTimeMillis();
                    InstrumentationTool.insertUniqueId(inputUniqId);
                    ArrayList<BigInteger> result = intFact.calcPrimeFactors(new BigInteger(inputNumber));
                    long stopTime = System.currentTimeMillis();
                    logger.info("Factorization took " + (stopTime - startTime) + "ms");
                    logger.info("Factorization result: " + result);
                    t.sendResponseHeaders(200, result.toString().length());
                    OutputStream os = t.getResponseBody();
                    os.write(result.toString().getBytes());
                    os.close();
                } catch (Exception e) {
                    logger.fatal("Got exception when trying to factorize:");
                    logger.fatal(e);
                    t.sendResponseHeaders(500, 0);
                    t.getResponseBody().close();
                } finally {
                    // TODO Update Dynamo
                    //HTTPServer.queue.add(inputUniqId,
                    //        Thread.currentThread().getId(),
                    //        DynamoMessenger.INCREMENT_THREADS);
                }
            } else {
                t.sendResponseHeaders(200, FORM.length());
                OutputStream os = t.getResponseBody();
                os.write(FORM.getBytes());
                os.close();
            }
        } else {
            logger.warn("Unsupported method");
            t.sendResponseHeaders(405, 0); t.getResponseBody().close();
        }
    }
}
