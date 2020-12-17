package kr.entree.spicord.bungee;

import io.vavr.Lazy;
import kr.entree.spicord.*;
import kr.entree.spicord.discord.Discord;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

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
        // 2
        setData(loadAllSpicordData(this));
        // 4
        createJDADiscord(this)
                .peek(this::setDiscord)
                .onFailure(this::logError);
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

    public void setDiscord(@Nullable Discord discord) {
        SpicordRoutines.setDiscord(this, discord);
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
