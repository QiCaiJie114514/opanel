package net.opanel.bukkit_helper;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.handler.NBTHandlers;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import net.opanel.common.OPanelDifficulty;
import net.opanel.common.OPanelGameMode;
import net.opanel.common.OPanelServer;
import net.opanel.utils.Utils;
import org.bukkit.Difficulty;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Properties;

public abstract class BaseBukkitSave {
    protected final JavaPlugin plugin;
    protected final Server server;
    protected final Path savePath;
    protected ReadWriteNBT nbt;

    public BaseBukkitSave(JavaPlugin plugin, Server server, Path path) {
        this.plugin = plugin;
        this.server = server;
        savePath = path;
        try {
            nbt = NBT.readFile(savePath.resolve("level.dat").toFile()).getCompound("Data");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void saveNbt() throws IOException {
        ReadWriteNBT dataNbt = NBT.createNBTObject();
        dataNbt.set("Data", nbt, NBTHandlers.STORE_READWRITE_TAG);
        NBT.writeFile(savePath.resolve("level.dat").toFile(), dataNbt);
    }

    protected World getCurrentWorld() {
        return server.getWorlds().get(0);
    }

    public String getName() {
        return savePath.getFileName().toString();
    }

    public String getDisplayName() {
        return nbt.getString("LevelName").replaceAll("\u00C2", "");
    }

    public void setDisplayName(String displayName) throws IOException {
        nbt.setString("LevelName", displayName);
        saveNbt();
    }

    public Path getPath() {
        return savePath.toAbsolutePath();
    }

    public long getSize() throws IOException {
        return Utils.getDirectorySize(savePath);
    }

    public boolean isRunning() {
        return getCurrentWorld().getName().equals(getName());
    }

    public boolean isCurrent() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(OPanelServer.serverPropertiesPath.toFile()));
        return properties.getProperty("level-name").replaceAll("\u00c2", "").equals(getName());
    }

    public void setToCurrent() throws IOException {
        if(isCurrent()) return;
        OPanelServer.writePropertiesContent(OPanelServer.getPropertiesContent().replaceAll("level-name=.+", "level-name="+ getName()));
    }

    public OPanelGameMode getDefaultGameMode() {
        int gamemode = nbt.getInteger("GameType");
        return OPanelGameMode.fromId(gamemode);
    }

    public void setDefaultGameMode(OPanelGameMode gamemode) throws IOException {
        nbt.setInteger("GameType", gamemode.getId());
        saveNbt();
    }

    public OPanelDifficulty getDifficulty() throws IOException {
        if(isCurrent()) return OPanelDifficulty.fromId(getCurrentWorld().getDifficulty().getValue());

        byte difficulty = nbt.getByte("Difficulty");
        return OPanelDifficulty.fromId(difficulty);
    }

    public void setDifficulty(OPanelDifficulty difficulty) throws IOException {
        if(isCurrent()) {
            ((TaskRunner) plugin).runTask(() -> {
                switch(difficulty) {
                    case PEACEFUL -> getCurrentWorld().setDifficulty(Difficulty.PEACEFUL);
                    case EASY -> getCurrentWorld().setDifficulty(Difficulty.EASY);
                    case NORMAL -> getCurrentWorld().setDifficulty(Difficulty.NORMAL);
                    case HARD -> getCurrentWorld().setDifficulty(Difficulty.HARD);
                }
            });
        }

        nbt.setByte("Difficulty", (byte) difficulty.getId());
        saveNbt();
    }

    public boolean isDifficultyLocked() {
        return nbt.getByte("DifficultyLocked") == 1;
    }

    public void setDifficultyLocked(boolean locked) throws IOException {
        nbt.setByte("DifficultyLocked", (byte) (locked ? 1 : 0));
        saveNbt();
    }

    public boolean isHardcore() throws IOException {
        if(isCurrent()) return getCurrentWorld().isHardcore();

        return nbt.getByte("hardcore") == 1;
    }

    public void setHardcoreEnabled(boolean enabled) throws IOException {
        if(isCurrent()) {
            ((TaskRunner) plugin).runTask(() -> getCurrentWorld().setHardcore(enabled));
        }

        nbt.setByte("hardcore", (byte) (enabled ? 1 : 0));
        saveNbt();
    }

    public HashMap<String, Boolean> getDatapacks() {
        HashMap<String, Boolean> datapacks = new HashMap<>();
        ReadWriteNBT datapacksNbt = nbt.getCompound("DataPacks");
        datapacksNbt.getStringList("Enabled").forEach(name -> datapacks.put(name, true));
        datapacksNbt.getStringList("Disabled").forEach(name -> datapacks.put(name, false));
        return datapacks;
    }

    public void toggleDatapack(String id, boolean enabled) throws IOException {
        Boolean currentEnabled = getDatapacks().get(id);
        if(currentEnabled == null || currentEnabled == enabled) return;
        if(id.equals("vanilla")) return;

        if(isCurrent()) {
            ((TaskRunner) plugin).runTask(() -> {
                server.dispatchCommand(server.getConsoleSender(), "datapack "+ (enabled ? "enable" : "disable") +" \""+ id +"\"");
            });
        }

        ReadWriteNBT datapacksNbt = nbt.getCompound("DataPacks");
        if(enabled) {
            datapacksNbt.getStringList("Disabled").remove(id);
            datapacksNbt.getStringList("Enabled").add(id);
        } else {
            datapacksNbt.getStringList("Enabled").remove(id);
            datapacksNbt.getStringList("Disabled").add(id);
        }
        saveNbt();
    }

    public void delete() throws IOException {
        Utils.deleteDirectoryRecursively(savePath);
    }
}
