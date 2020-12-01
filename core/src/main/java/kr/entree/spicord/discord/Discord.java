package kr.entree.spicord.discord;

import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import io.vavr.control.Try;
import kr.entree.spicord.discord.channel.TextChannel;

public interface Discord {
    Try<Void> shutdown(boolean force);

    Option<TextChannel> findTextChannelById(long id);

    boolean isRunning();

    Future<Discord> awaitReady();
}
