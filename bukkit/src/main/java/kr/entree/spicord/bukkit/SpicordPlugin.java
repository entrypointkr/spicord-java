package kr.entree.spicord.bukkit;

import io.vavr.Lazy;
import kr.entree.spicord.*;
import kr.entree.spicord.discord.Discord;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static kr.entree.spicord.SpicordData.emptyData;
import static kr.entree.spicord.SpicordPath.pathOf;
import static kr.entree.spicord.SpicordRoutines.*;
import static kr.entree.spicord.bukkit.command.BukkitCommands.bukkitCommand;
import static kr.entree.spicord.bukkit.task.BukkitExecutors.pluginAsyncExecutor;
import static kr.entree.spicord.bukkit.task.BukkitExecutors.pluginExecutor;
import static kr.entree.spicord.discord.Discords.emptyDiscord;

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
        createSpicordFiles(getPath());
        setData(loadAllSpicordData(this));
        createJDADiscord(this)
                .peek(discordSetter(this))
                .onFailure(errorLogger());
        long fiveSecs = 5L * 20L;
        getCommand("spicord").setExecutor(bukkitCommand((commander, args) ->
                executeSpicordCommand(this, commander, args)
                        .onFailure(errorLogger())));
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!getData().getDiscord().isRunning()) {
                reconnectDiscord(this);
            }
        }, fiveSecs, fiveSecs);
    }

    @Override
    public void onDisable() {
        setDiscord(null);
    }

    public Consumer<Throwable> errorLogger() {
        return SpicordRoutines.errorLogger(getLogger());
    }

    public void setDiscord(Discord discord) {
        setData(getData().withDiscord(discord != null ? discord : emptyDiscord()));
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
