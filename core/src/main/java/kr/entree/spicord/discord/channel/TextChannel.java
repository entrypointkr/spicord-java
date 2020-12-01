package kr.entree.spicord.discord.channel;

import io.vavr.concurrent.Future;
import kr.entree.spicord.discord.contents.MessageContents;
import kr.entree.spicord.discord.message.Message;

public interface TextChannel {
    Future<Message> sendMessage(MessageContents contents);
}
