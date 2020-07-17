package advisor.spotify;

import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class CodeAwaiterServer implements AutoCloseable {

    private final HttpServer server;

    @SneakyThrows
    public CodeAwaiterServer() {
        server = HttpServer.create();
    }

    @SneakyThrows
    public Future<String> startAndWaitForCode() {
        server.bind(new InetSocketAddress(8080), 0);
        CompletableFuture<String> code = new CompletableFuture<>();
        server.createContext("/", exchange -> {
            String res = exchange.getRequestURI().getQuery();
            String msg;
            if (res != null && res.startsWith("code=")) {
                res = res.substring(5);
                msg = "Got the code. Return back to your program.";
            } else {
                res = null;
                msg = "Not found authorization code. Try again.";
            }

            try {
                exchange.sendResponseHeaders(200, msg.length());
                exchange.getResponseBody().write(msg.getBytes());
                exchange.getResponseBody().close();
                code.complete(res);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        server.start();
        return code;
    }

    @Override
    public void close() {
        server.stop(1);
    }
}
