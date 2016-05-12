package pt.ulisboa.tecnico.cnv.proxyserver.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import pt.ulisboa.tecnico.cnv.proxyserver.DynamoConnecter;
import pt.ulisboa.tecnico.cnv.proxyserver.Instance;
import pt.ulisboa.tecnico.cnv.proxyserver.NumberCrunchingEntry;
import pt.ulisboa.tecnico.cnv.proxyserver.balancer.Balancer;
import pt.ulisboa.tecnico.cnv.proxyserver.Scaler;

public class HandleFactorize implements HttpHandler {
    final static Logger logger = Logger.getLogger(HandleFactorize.class);

    final static String FORM = "<html><body><form method='get'>Number: <input type='text' name='n'></input><br><input type='submit'></input></form></body></html>";

    private Scaler scaler;
    private Balancer balancer;

    public HandleFactorize(Scaler scaler, Balancer balancer) {
        super();
        this.scaler = scaler;
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
                Instance target = balancer.requestInstance(inputNumber);
                String targetDns = target.getDns();
                logger.info("Got " + targetDns + " target from Balancer");
                NumberCrunchingEntry entry = null;
                String answer = null;
                try {
                    BigInteger number = new BigInteger(inputNumber);
                    scaler.incReq(number.bitLength());
                	entry = DynamoConnecter.createEntryGetID(target.getId(),number.bitLength());
                	target.insertTask(entry);
                	answer = doRequest(targetDns, inputNumber);
                } catch (Exception e) {
                	target.removeTask(entry);
                	DynamoConnecter.deleteEntry(entry.getID());
                    logger.fatal("Got exception when requesting answer from worker:");
                    logger.fatal(e);
                }

                if (answer != null) {
                    t.sendResponseHeaders(200, answer.length());
                    OutputStream os = t.getResponseBody();
                    os.write(answer.getBytes());
                    os.close();
                } else {
                    t.sendResponseHeaders(500, 0);
                    t.getResponseBody().close();
                }
            	target.removeTask(entry);
            	long number_instructions = DynamoConnecter.getNumberOfInstructions(String.valueOf(entry.getID()));
            	DynamoConnecter.addStatisticEntry(entry.getNumberBits(), number_instructions);
            	DynamoConnecter.deleteEntry(entry.getID());
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
        String url = "http://" + dns + ":8080/f.html?n=" + number;
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
