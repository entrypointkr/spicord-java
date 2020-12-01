package kr.entree.spicord.bukkit.command;

import io.vavr.collection.List;
import kr.entree.spicord.command.Commander;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.function.BiConsumer;

public class BukkitCommands {
    public static CommandExecutor bukkitCommand(BiConsumer<Commander, List<String>> executor) {
        return (sender, command, label, args) -> {
            executor.accept(
                    bukkitCommander(sender),
                    List.of(args).prepend(label)
            );
            return true;
        };
    }

    public static Commander bukkitCommander(CommandSender sender) {
        return new Commander() {
            @Override
            public String getName() {
                return sender.getName();
            }

            @Override
            public boolean hasPermission(String permission) {
                return sender.hasPermission(permission);
            }

            @Override
            public void sendMessage(String message) {
                sender.sendMessage(message);
            }
        };
    }
}
