package net.opanel.endpoint;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import net.opanel.OPanel;
import net.opanel.web.JwtManager;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class BaseEndpoint {
    protected final WsConfig ws;
    protected final OPanel plugin;

    protected static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private final ConcurrentHashMap<Session, Set<Consumer<WsMessageContext>>> sessionListeners = new ConcurrentHashMap<>();

    public BaseEndpoint(WsConfig ws, OPanel plugin) {
        this.ws = ws;
        this.plugin = plugin;

        init();
    }

    private void init() {
        ws.onConnect(ctx -> {
            Session session = ctx.session;

            subscribe(session, Packet.AUTH, (WsMessageContext msgCtx, String token) -> {
                final String hashedRealKey = plugin.getConfig().accessKey; // hashed 2
                if(token != null && JwtManager.verifyToken(token, hashedRealKey, plugin.getConfig().salt)) {
                    // Register session
                    sessions.add(session);
                    msgCtx.send(new Packet<>(Packet.CONNECT));
                    onConnect(msgCtx);
                } else {
                    msgCtx.closeSession(1008, "Unauthorized.");
                }
            });

            subscribe(session, Packet.PING, msgCtx -> {
                msgCtx.send(new Packet<>(Packet.PONG));
            });
        });

        ws.onMessage(ctx -> {
            for(Consumer<WsMessageContext> listener : sessionListeners.get(ctx.session)) {
                try {
                    listener.accept(ctx);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        ws.onClose(ctx -> {
            sessions.remove(ctx.session);
            onClose(ctx);
        });
    }

    protected void subscribe(Session session, String type, Consumer<WsMessageContext> cb) {
        subscribe(session, type, (ctx, data) -> {
            cb.accept(ctx);
        });
    }

    @SuppressWarnings("unchecked")
    protected <D> void subscribe(Session session, String type, BiConsumer<WsMessageContext, D> cb) {
        Set<Consumer<WsMessageContext>> listeners = sessionListeners.computeIfAbsent(session, k -> new CopyOnWriteArraySet<>());
        listeners.add(ctx -> {
            if(ctx.session != session) return;
            if(!sessions.contains(session) && !type.equals(Packet.AUTH)) {
                ctx.closeSession(1008, "Unauthorized.");
                return;
            }

            Packet<?> packet = ctx.messageAsClass(Packet.class);
            if(packet.type.equals(type)) {
                cb.accept(ctx, (D) packet.data);
            }
        });
    }

    protected abstract void onConnect(WsMessageContext ctx);
    protected abstract void onClose(WsCloseContext ctx);
    protected abstract void onError(WsErrorContext ctx);

    protected void sendErrorMessage(WsMessageContext ctx, String err) {
        ctx.send(new Packet<>(Packet.ERROR, err));
    }

    protected <D> void broadcast(Packet<D> packet) {
        String message = new Gson().toJson(packet);
        synchronized(sessions) {
            sessions.removeIf(session -> !session.isOpen());
            for(Session session : sessions) {
                try {
                    session.getRemote().sendString(message);
                } catch(Exception e) {
                    // Use System.err to avoid recursive logging through LogListenerAppender
                    System.err.println("[OPanel] Failed to broadcast message to session: " + e.getMessage());
                }
            }
        }
    }

    public static void closeAllSessions() {
        synchronized(sessions) {
            for(Session session : sessions) {
                session.close(1000, "Server is stopping.");
            }
            sessions.clear();
        }
    }
}
