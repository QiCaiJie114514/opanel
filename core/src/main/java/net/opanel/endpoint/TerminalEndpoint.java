package net.opanel.endpoint;

import io.javalin.Javalin;
import io.javalin.websocket.*;
import net.opanel.OPanel;
import net.opanel.common.OPanelPlayer;
import net.opanel.terminal.LogListenerManager;
import org.eclipse.jetty.websocket.api.Session;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TerminalEndpoint extends BaseEndpoint {
    private static class TerminalPacket<D> extends Packet<D> {
        public static final String INIT = "init";
        public static final String LOG = "log";
        public static final String AUTOCOMPLETE = "autocomplete";
        public static final String COMMAND = "command";

        public TerminalPacket(String type, D data) {
            super(type, data);
        }
    }

    private final LogListenerManager logListenerManager;

    // To avoid duplicated log listener from registering,
    // which can lead to plenty duplicated logs in the frontend terminal
    private static final AtomicBoolean hasLogListenerRegistered = new AtomicBoolean(false);

    public TerminalEndpoint(Javalin app, WsConfig ws, OPanel plugin) {
        super(app, ws, plugin);

        logListenerManager = plugin.getLogListenerManager();
        if(hasLogListenerRegistered.compareAndSet(false, true)) {
            logListenerManager.addListener(log -> {
                broadcast(new TerminalPacket<>(TerminalPacket.LOG, log));
            });
        }
    }

    @Override
    public void onConnect(WsMessageContext ctx) {
        Session session = ctx.session;

        ctx.send(new TerminalPacket<>(TerminalPacket.INIT, logListenerManager.getRecentLogs()));

        subscribe(session, TerminalPacket.COMMAND, (WsMessageContext msgCtx, String command) -> {
            if(command.startsWith("/")) {
                command = command.replace("/", "");
            }
            plugin.getServer().sendServerCommand(command);
        });

        subscribe(session, TerminalPacket.AUTOCOMPLETE, (WsMessageContext msgCtx, Number arg) -> {
            if(arg.equals(1.0)) {
                ctx.send(new TerminalPacket<>(TerminalPacket.AUTOCOMPLETE, plugin.getServer().getCommands()));
                return;
            }
            List<OPanelPlayer> players = plugin.getServer().getOnlinePlayers();
            List<String> nameList = new ArrayList<>();
            for(OPanelPlayer player : players) {
                nameList.add(player.getName());
            }
            ctx.send(new TerminalPacket<>(TerminalPacket.AUTOCOMPLETE, nameList));
        });
    }
}
