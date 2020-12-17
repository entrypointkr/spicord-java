package kr.entree.spicord.bungee;

import io.vavr.Lazy;
import kr.entree.spicord.*;
import kr.entree.spicord.discord.Discord;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static io.vavr.API.Seq;
import static kr.entree.spicord.SpicordData.emptyData;
import static kr.entree.spicord.SpicordPath.pathOf;
import static kr.entree.spicord.SpicordRoutines.*;
import static kr.entree.spicord.bungee.command.BungeeCommands.bungeeCommand;
import static kr.entree.spicord.bungee.task.BungeeExecutors.pluginAsyncExecutor;
import static kr.entree.spicord.bungee.task.BungeeExecutors.pluginExecutor;
import static kr.entree.spicord.discord.Discords.emptyDiscord;

public class SpicordBungeePlugin extends Plugin implements SpicordPlatform {
    private final Executor sync = pluginExecutor(this);
    private final Executor async = pluginAsyncExecutor(this);
    private final Lazy<SpicordPath> path = Lazy.of(() -> pathOf(
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
        createSpicordFiles(getPath());
        // 2, 4
        setData(loadAllSpicordData(this)
                .withDiscord(createJDADiscord(this)
                        .onFailure(this::logError)
                        .getOrElse(emptyDiscord())));
        // 3
        ProxyServer.getInstance().getPluginManager()
                .registerCommand(this, bungeeCommand("spicord", Seq(), (commander, args) ->
                        executeSpicordCommand(this, commander, args)
                                .onFailure(this::logError)));
        // 5
        ProxyServer.getInstance().getScheduler().schedule(this, () -> {
            if (!getData().getDiscord().isRunning()) {
                reconnectDiscord(this);
            }
        }, 5L, 5L, TimeUnit.SECONDS);
    }

    public void setDiscord(Discord discord) {
        setData(getData().withDiscord(discord != null ? discord : emptyDiscord()));
    }

    public void logError(Throwable throwable) {
        SpicordRoutines.logError(getLogger(), throwable);
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
