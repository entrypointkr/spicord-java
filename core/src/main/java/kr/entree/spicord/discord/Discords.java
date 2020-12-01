package kr.entree.spicord.discord;

import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import io.vavr.control.Try;
import kr.entree.spicord.discord.channel.TextChannel;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Discords {
    private static final Discord EMPTY = new Discord() {
        @Override
        public Try<Void> shutdown(boolean force) {
            return Try.success(null);
        }

        @Override
        public Option<TextChannel> findTextChannelById(long id) {
            return Option.none();
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public Future<Discord> awaitReady() {
            return Future.successful(null);
        }
    };

    private static final Discord PREPARE = new Discord() {
        @Override
        public Try<Void> shutdown(boolean force) {
            return emptyDiscord().shutdown(force);
        }

        @Override
        public Option<TextChannel> findTextChannelById(long id) {
            return emptyDiscord().findTextChannelById(id);
        }

        @Override
        public boolean isRunning() {
            return true;
        }

        @Override
        public Future<Discord> awaitReady() {
            return emptyDiscord().awaitReady();
        }
    };

    public static Discord emptyDiscord() {
        return EMPTY;
    }

    public static Discord preparingDiscord() {
        return PREPARE;
    }
}
