package kr.entree.spicord.command;

import kr.entree.spicord.discord.channel.TextChannel;
import lombok.Data;

public interface CommandType {
    class Reload implements CommandType {
    }

    @Data
    class SendMessageRequest implements CommandType {
        private final String channel;
        private final String contents;
    }

    @Data
    class SendMessage implements CommandType {
        private final TextChannel channel;
        private final String contents;
    }

    @Data
    class Permissible implements CommandType {
        private final String permission;
        private final CommandType result;
    }
}
