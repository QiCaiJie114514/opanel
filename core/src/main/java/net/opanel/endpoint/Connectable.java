package net.opanel.endpoint;

import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsErrorContext;
import io.javalin.websocket.WsMessageContext;

public interface Connectable {
    void onConnect(WsMessageContext ctx);
    void onClose(WsCloseContext ctx);
    void onError(WsErrorContext ctx);
}
