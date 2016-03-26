package pt.ulisboa.tecnico.cnv.httpserver.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandleIndex implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        // try { Thread.sleep(1000); } catch(Exception e) { }
        String response = "This was the query: " + t.getRequestURI().getQuery();
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
