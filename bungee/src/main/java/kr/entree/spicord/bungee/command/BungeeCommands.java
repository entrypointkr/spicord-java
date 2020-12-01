package kr.entree.spicord.bungee.command;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import kr.entree.spicord.command.Commander;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.function.BiConsumer;

public class BungeeCommands {
    public static Command bungeeCommand(String name, Seq<String> aliases, BiConsumer<Commander, List<String>> executor) {
        return new Command(name, null, aliases.toJavaArray(String[]::new)) {
            @Override
            public void execute(CommandSender sender, String[] args) {
                // TODO: check where the label
                executor.accept(
                        bungeeCommander(sender),
                        List.of(args).prepend(name)
                );
            }
        };
    }

    public static Commander bungeeCommander(CommandSender sender) {
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
                sender.sendMessage(new TextComponent(message));
            }
        };
    }
}
