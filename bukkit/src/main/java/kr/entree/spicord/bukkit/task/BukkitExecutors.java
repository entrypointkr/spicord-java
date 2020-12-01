package kr.entree.spicord.bukkit.task;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.concurrent.Executor;

public class BukkitExecutors {
    public static Executor pluginExecutor(Plugin plugin) {
        return r -> Bukkit.getScheduler().runTask(plugin, r);
    }

    public static Executor pluginAsyncExecutor(Plugin plugin) {
        return r -> Bukkit.getScheduler().runTaskAsynchronously(plugin, r);
    }

    public static Executor pluginDelayExecutor(Plugin plugin, Duration duration) {
        return r -> Bukkit.getScheduler().runTaskLater(plugin, r, duration.toMillis() / 50L);
    }

    public static Executor pluginTimerExecutor(Plugin plugin, Duration duration) {
        return r -> pluginDelayExecutor(plugin, duration)
                .execute(() -> {
                    r.run();
                    pluginTimerExecutor(plugin, duration).execute(r);
                });
    }
}
