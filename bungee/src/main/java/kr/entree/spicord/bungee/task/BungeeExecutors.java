package kr.entree.spicord.bungee.task;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class BungeeExecutors {
    public static Executor pluginExecutor(Plugin plugin) {
        return runnable -> ProxyServer.getInstance().getScheduler()
                .schedule(plugin, runnable, 0L, TimeUnit.MILLISECONDS);
    }

    public static Executor pluginAsyncExecutor(Plugin plugin) {
        return runnable -> ProxyServer.getInstance().getScheduler()
                .runAsync(plugin, runnable);
    }
}
