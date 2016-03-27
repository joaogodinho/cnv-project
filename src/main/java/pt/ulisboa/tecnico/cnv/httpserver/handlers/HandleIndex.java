package pt.ulisboa.tecnico.cnv.httpserver.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandleIndex implements HttpHandler {
    final static Logger logger = Logger.getLogger(HandleIndex.class);

    public HandleIndex() {
        super();
        logger.info("Setting context for Index");
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        logger.info("Got request from " +t.getRequestURI());
        String response = "This was the query: " + t.getRequestURI().getQuery();
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
