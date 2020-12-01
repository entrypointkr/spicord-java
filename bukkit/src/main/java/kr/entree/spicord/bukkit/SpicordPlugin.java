package kr.entree.spicord.bukkit;

import io.vavr.Lazy;
import io.vavr.concurrent.Future;
import kr.entree.spicord.Execution;
import kr.entree.spicord.SpicordData;
import kr.entree.spicord.SpicordPath;
import kr.entree.spicord.SpicordPlatform;
import kr.entree.spicord.bukkit.config.Yamls;
import kr.entree.spicord.discord.Discord;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;

import static java.util.Objects.requireNonNull;
import static kr.entree.spicord.SpicordData.emptyData;
import static kr.entree.spicord.SpicordPath.pathOf;
import static kr.entree.spicord.SpicordRoutines.*;
import static kr.entree.spicord.bukkit.command.BukkitCommands.bukkitCommand;
import static kr.entree.spicord.bukkit.config.Yamls.saveYaml;
import static kr.entree.spicord.bukkit.task.BukkitExecutors.pluginAsyncExecutor;
import static kr.entree.spicord.bukkit.task.BukkitExecutors.pluginExecutor;
import static kr.entree.spicord.config.FileUtils.saveFile;
import static kr.entree.spicord.config.SpicordConfig.defaultConfig;
import static kr.entree.spicord.discord.Discords.emptyDiscord;
import static kr.entree.spicord.discord.Discords.preparingDiscord;

public class SpicordPlugin extends JavaPlugin implements SpicordPlatform {
    private final Executor sync = pluginExecutor(this);
    private final Executor async = pluginAsyncExecutor(this);
    private final Lazy<SpicordPath> path = Lazy.of(() -> pathOf(
            new File(requireNonNull(getDataFolder()), "config.yml"),
            new File(getDataFolder(), "guilds.yml"),
            new File(getDataFolder(), "channels.yml"),
            new File(getDataFolder(), "messages.yml")
    ));
    private @Getter @Setter SpicordData data = emptyData();

    public static SpicordPlugin get() {
        return (SpicordPlugin) Bukkit.getPluginManager().getPlugin("Spicord");
    }

    @Override
    public void onEnable() {
        File configFile = getPath().getConfigFile();
        if (!configFile.isFile()) {
            saveFile(configFile, saveYaml(defaultConfig().serialize()));
        }
        setData(loadAllSpicordData(this)
                .withDiscord(createJDADiscord(this)
                        .onFailure(this::logError)
                        .getOrElse(emptyDiscord())));
        long fiveSecs = 5L * 20L;
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!getData().getDiscord().isRunning()) {
                setDiscord(preparingDiscord());
                Future.of(async, () -> createJDADiscord(this).getOrElse(emptyDiscord()))
                        .peek(this::setDiscord);
            }
        }, fiveSecs, fiveSecs);
        getCommand("spicord").setExecutor(bukkitCommand((commander, args) ->
                executeSpicordCommand(this, commander, args)
                        .onFailure(this::logError)));
    }

    @Override
    public void onDisable() {
        setDiscord(null);
    }

    public void setDiscord(Discord discord) {
        setData(getData().withDiscord(discord != null ? discord : emptyDiscord()));
    }

    @Override
    public Map<String, Object> loadYaml(String contents) {
        return Yamls.loadYaml(contents);
    }

    public void logError(Throwable throwable) {
        getLogger().log(Level.WARNING, "Error!", throwable);
    }

    @Override
    public @NotNull PluginCommand getCommand(@NotNull String name) {
        return requireNonNull(super.getCommand(name), String.format("Command '%s' shouldn't be null, please report this to the developer!", name));
    }

    @Override
    public SpicordPath getPath() {
        return path.get();
    }

    @Override
    public Execution getExecution() {
        return new Execution(sync, async);
    }
}
