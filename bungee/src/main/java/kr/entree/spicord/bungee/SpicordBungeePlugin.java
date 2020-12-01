package kr.entree.spicord.bungee;

import io.vavr.Lazy;
import io.vavr.concurrent.Future;
import kr.entree.spicord.Execution;
import kr.entree.spicord.SpicordData;
import kr.entree.spicord.SpicordPath;
import kr.entree.spicord.SpicordPlatform;
import kr.entree.spicord.bungee.config.Yamls;
import kr.entree.spicord.config.SpicordConfig;
import kr.entree.spicord.discord.Discord;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static io.vavr.API.Seq;
import static kr.entree.spicord.SpicordData.emptyData;
import static kr.entree.spicord.SpicordPath.pathOf;
import static kr.entree.spicord.SpicordRoutines.*;
import static kr.entree.spicord.bungee.command.BungeeCommands.bungeeCommand;
import static kr.entree.spicord.bungee.config.Yamls.saveYaml;
import static kr.entree.spicord.bungee.task.BungeeExecutors.pluginAsyncExecutor;
import static kr.entree.spicord.bungee.task.BungeeExecutors.pluginExecutor;
import static kr.entree.spicord.config.FileUtils.saveFile;
import static kr.entree.spicord.discord.Discords.emptyDiscord;
import static kr.entree.spicord.discord.Discords.preparingDiscord;

public class SpicordBungeePlugin extends Plugin implements SpicordPlatform {
    private final Executor sync = pluginExecutor(this);
    private final Executor async = pluginAsyncExecutor(this);
    private final Lazy<SpicordPath> paths = Lazy.of(() -> pathOf(
            new File(getDataFolder(), "config.yml"),
            new File(getDataFolder(), "guilds.yml"),
            new File(getDataFolder(), "channels.yml"),
            new File(getDataFolder(), "messages.yml")
    ));
    private @Getter @Setter SpicordData data = emptyData();

    /* TODO
    1. save default config file if absent
    2. load configs
    3. register command
    4. start discord (blocking)
    5. register discord reconnection task (non-blocking)
     */
    @Override
    public void onEnable() {
        // 1
        File configFile = getPath().getConfigFile();
        if (!configFile.isFile()) {
            saveFile(configFile, saveYaml(SpicordConfig.defaultConfig().serialize()));
        }
        // 2, 4
        setData(loadAllSpicordData(this)
                .withDiscord(createJDADiscord(this)
                        .onFailure(this::logError)
                        .getOrElse(emptyDiscord())));
        // 3
        ProxyServer.getInstance().getPluginManager()
                .registerCommand(this, bungeeCommand("spicord", Seq(),
                        (commander, args) -> executeSpicordCommand(this, commander, args)
                                .onFailure(this::logError))
                );
        // 5
        ProxyServer.getInstance().getScheduler().schedule(this, () -> {
            if (!getData().getDiscord().isRunning()) {
                setDiscord(preparingDiscord());
                Future.of(async, () -> createJDADiscord(this).getOrElse(emptyDiscord()))
                        .peek(this::setDiscord);
            }
        }, 5L, 5L, TimeUnit.SECONDS);
    }

    public void logError(Throwable throwable) {
        getLogger().log(Level.WARNING, throwable, () -> "Error!");
    }

    public void setDiscord(Discord discord) {
        setData(getData().withDiscord(discord != null ? discord : emptyDiscord()));
    }

    @Override
    public SpicordPath getPath() {
        return paths.get();
    }

    @Override
    public Execution getExecution() {
        return new Execution(sync, async);
    }

    @Override
    public Map<String, Object> loadYaml(String contents) {
        return Yamls.parseYaml(contents);
    }
}
