package app;

import io.javalin.Javalin;
import io.javalin.websocket.WsContext;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    // Trådsikker samling af aktive websocket-klienter
    private static final Set<WsContext> clients = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.routes.get("/api/health", ctx -> ctx.result("OK"));
            config.routes.ws("/ws/dice", ws -> {

                ws.onConnect(ctx -> {
                    clients.add(ctx);
                    System.out.println("Client connected: " + ctx.sessionId());
                    broadcastPlayerCount();
                });

                ws.onClose(ctx -> {
                    clients.remove(ctx);
                    System.out.println("Client disconnected: " + ctx.sessionId());
                    broadcastPlayerCount();
                });

                ws.onMessage(ctx -> {
                    String message = ctx.message();

                    if ("roll".equals(message)) {
                        int dice = ThreadLocalRandom.current().nextInt(1, 7);

                        String json = """
                        {
                          "type": "roll",
                          "value": %d,
                          "playerCount": %d
                        }
                        """.formatted(dice, clients.size());

                        broadcast(json);
                    }
                });
            });
        }).start(7070);
    }

    private static void broadcast(String message) {
        clients.removeIf(ctx -> !ctx.session.isOpen());
        clients.forEach(client -> client.send(message));
    }

    private static void broadcastPlayerCount() {
        String json = """
            {
              "type": "players",
              "count": %d
            }
            """.formatted(clients.size());

        broadcast(json);
    }
}