package net.opanel.fabric_helper;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.*;
import net.opanel.common.OPanelServer;

import java.lang.reflect.Field;

public class FabricUtils {
    public static boolean forceUpdateProperties(MinecraftDedicatedServer server) {
        try {
            Field propertiesLoaderField = MinecraftDedicatedServer.class.getDeclaredField("propertiesLoader");
            propertiesLoaderField.setAccessible(true);
            ServerPropertiesLoader propertiesLoader = (ServerPropertiesLoader) propertiesLoaderField.get(server);
            propertiesLoader.apply(h -> ServerPropertiesHandler.load(OPanelServer.serverPropertiesPath));
        } catch (ReflectiveOperationException e) {
            return false;
        }
        return true;
    }
}
