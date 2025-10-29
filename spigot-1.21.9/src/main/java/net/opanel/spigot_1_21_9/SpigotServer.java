package net.opanel.spigot_1_21_9;

import net.opanel.ServerType;
import net.opanel.common.*;
import net.opanel.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SpigotServer implements OPanelServerExtended {
    private static final Path serverIconPath = Paths.get("").resolve("server-icon.png");
    private static final Path codeOfConductPath = Paths.get("").resolve("codeofconduct");

    private final Main plugin;
    private final Server server;

    public SpigotServer(Main plugin, Server server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public ServerType getServerType() {
        if(Utils.hasClass("com.destroystokyo.paper.PaperConfig")) {
            return ServerType.PAPER;
        }
        if(Utils.hasClass("org.bukkit.entity.Player$Spigot")) {
            return ServerType.SPIGOT;
        }
        return ServerType.BUKKIT;
    }

    @Override
    public byte[] getFavicon() {
        if(!Files.exists(serverIconPath)) return null;
        try {
            return Files.readAllBytes(serverIconPath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getMotd() {
        return server.getMotd();
    }

    @Override
    public void setMotd(String motd) throws IOException {
        // Call setMotd() first
        server.setMotd(motd);
        // Directly modify motd in server.properties
        String formatted = motd.replaceAll("\n", Matcher.quoteReplacement("\\n"));
        OPanelServer.writePropertiesContent(OPanelServer.getPropertiesContent().replaceAll("motd=.+", Matcher.quoteReplacement("motd="+ formatted)));
    }

    @Override
    public String getVersion() {
        // getBukkitVersion() -> "<MinecraftVersion>-R0.x-SNAPSHOT"
        return server.getBukkitVersion().split("-")[0];
    }

    @Override
    public int getPort() {
        return server.getPort();
    }

    @Override
    public List<OPanelSave> getSaves() {
        List<OPanelSave> list = new ArrayList<>();
        try(Stream<Path> stream = Files.list(Paths.get(""))) {
            stream.filter(path -> (
                            !path.toString().endsWith("_nether")
                            && !path.toString().endsWith("_the_end")
                            && Files.exists(path.resolve("level.dat"))
                            && !Files.isDirectory(path.resolve("level.dat"))
                    ))
                    .map(Path::toAbsolutePath)
                    .forEach(path -> {
                        SpigotSave save = new SpigotSave(server, path);
                        list.add(save);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public OPanelSave getSave(String saveName) {
        final Path savePath = Paths.get("").resolve(saveName);
        if(
                !Files.exists(savePath)
                || savePath.toString().endsWith("_nether")
                || savePath.toString().endsWith("_the_end")
                || !Files.exists(savePath.resolve("level.dat"))
        ) {
            return null;
        }
        return new SpigotSave(server, savePath.toAbsolutePath());
    }

    @Override
    public void saveAll() {
        plugin.runTask(() -> {
            for(World world : server.getWorlds()) {
                world.save();
            }
            server.savePlayers();
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OPanelPlayer> getOnlinePlayers() {
        List<OPanelPlayer> list = new ArrayList<>();
        Collection<Player> players = (Collection<Player>) server.getOnlinePlayers();
        for(Player serverPlayer : players) {
            SpigotPlayer player = new SpigotPlayer(plugin, serverPlayer);
            list.add(player);
        }
        return list;
    }

    @Override
    public List<OPanelPlayer> getPlayers() {
        List<OPanelPlayer> list = new ArrayList<>();
        OfflinePlayer[] players = server.getOfflinePlayers();
        for(OfflinePlayer offlinePlayer : players) {
            if(offlinePlayer.isOnline()) {
                Player serverPlayer = offlinePlayer.getPlayer();
                if(serverPlayer == null) continue;
                list.add(new SpigotPlayer(plugin, serverPlayer));
            } else {
                list.add(new SpigotOfflinePlayer(plugin, server, offlinePlayer));
            }
        }
        return list;
    }

    @Override
    public int getMaxPlayerCount() {
        return server.getMaxPlayers();
    }

    @Override
    public OPanelPlayer getPlayer(String uuid) {
        for(OPanelPlayer player : getPlayers()) {
            if(player.getUUID().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    @Override
    public boolean isWhitelistEnabled() {
        return server.hasWhitelist();
    }

    @Override
    public void setWhitelistEnabled(boolean enabled) {
        plugin.runTask(() -> server.setWhitelist(enabled));
    }

    @Override
    public OPanelWhitelist getWhitelist() {
        return new SpigotWhitelist(plugin, server, server.getWhitelistedPlayers());
    }

    @Override
    public void sendServerCommand(String command) {
        plugin.runTask(() -> Bukkit.dispatchCommand(server.getConsoleSender(), command));
    }

    @Override
    public List<String> getCommands() {
        List<String> commands = new ArrayList<>();
        for(HelpTopic topic : server.getHelpMap().getHelpTopics()) {
            commands.add(topic.getName().toLowerCase().replaceFirst("/", ""));
        }
        return commands;
    }

    @Override
    public HashMap<String, Object> getGamerules() {
        final World world = server.getWorlds().getFirst();
        HashMap<String, Object> gamerules = new HashMap<>();
        for(String key : world.getGameRules()) {
            GameRule<?> rule = GameRule.getByName(key);
            if(rule == null) continue;
            gamerules.put(key, world.getGameRuleValue(rule));
        }
        return gamerules;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setGamerules(HashMap<String, Object> gamerules) {
        plugin.runTask(() -> {
            final World world = server.getWorlds().getFirst();
            gamerules.forEach((key, value) -> {
                if(value == null) return;
                GameRule<?> rule = GameRule.getByName(key);
                if(rule == null) return;
                if(value instanceof Boolean) {
                    world.setGameRule((GameRule<Boolean>) rule, (Boolean) value);
                } else if(value instanceof Number) {
                    world.setGameRule((GameRule<Integer>) rule, Double.valueOf((double) value).intValue());
                } else if(value instanceof String) {
                    world.setGameRule((GameRule<String>) rule, (String) value);
                }
            });
        });
    }

    @Override
    public void reload() {
        if(getServerType() == ServerType.PAPER) {
            sendServerCommand("reload confirm");
        } else {
            sendServerCommand("reload");
        }
    }

    @Override
    public void stop() {
        server.shutdown();
    }

    @Override
    public long getIngameTime() {
        return server.getWorlds().getFirst().getTime();
    }

    @Override
    public HashMap<String, String> getCodeOfConducts() throws IOException {
        HashMap<String, String> list = new HashMap<>();
        if(!Files.exists(codeOfConductPath)) {
            Files.createDirectory(codeOfConductPath);
        }

        try(Stream<Path> stream = Files.list(codeOfConductPath)) {
            stream.filter(item -> (
                    !Files.isDirectory(item)
                    && item.toString().endsWith(".txt")
                    && Utils.validateLocaleCode(item.getFileName().toString().replace(".txt", ""))
            ))
                    .forEach(item -> {
                        try {
                            final String lang = item.getFileName().toString().replace(".txt", "");
                            final String content = Utils.readTextFile(item);
                            list.put(lang, content);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
        return list;
    }

    @Override
    public void updateOrCreateCodeOfConduct(String lang, String content) throws IOException {
        Path filePath = codeOfConductPath.resolve(lang +".txt");
        if(!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
        Utils.writeTextFile(filePath, content);
    }

    @Override
    public void removeCodeOfConduct(String lang) throws IOException {
        Path filePath = codeOfConductPath.resolve(lang +".txt");
        Files.deleteIfExists(filePath);
    }
}
