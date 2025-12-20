package net.opanel.bukkit_helper;

import com.mojang.brigadier.CommandDispatcher;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class BaseBukkitServer {
    protected Object getDedicatedServer() throws ReflectiveOperationException {
        Server craftServer = Bukkit.getServer();
        return craftServer.getClass().getMethod("getServer").invoke(craftServer);
    }

    protected CommandDispatcher<?> getCommandDispatcher() throws ReflectiveOperationException {
        Object dedicatedServer = getDedicatedServer();
        Object commands = dedicatedServer.getClass().getMethod("getCommands").invoke(dedicatedServer);
        return (CommandDispatcher<?>) commands.getClass().getMethod("getDispatcher").invoke(commands);
    }
}
