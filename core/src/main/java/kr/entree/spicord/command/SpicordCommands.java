package kr.entree.spicord.command;

import io.vavr.Function1;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import kr.entree.spicord.SpicordData;
import kr.entree.spicord.SpicordPlatform;
import kr.entree.spicord.discord.contents.TextContents;
import kr.entree.spicord.lang.Lang;
import lombok.val;

import java.util.concurrent.Executor;
import java.util.function.Function;

import static io.vavr.API.*;
import static kr.entree.spicord.SpicordRoutines.loadAllSpicordData;
import static kr.entree.spicord.config.Parser.getOne;
import static kr.entree.spicord.config.Parser.parseLong;

public class SpicordCommands {
    public static Either<CommandFailure, CommandType> parseRootCommand(List<String> args) {
        String head = args.headOption().getOrElse("");
        return "spicord".equalsIgnoreCase(head)
                ? parseMainCommand(args.tail())
                : Left(new CommandFailure.Unknown(head));
    }

    public static <T> Function1<Integer, Option<T>> lift(List<T> list) {
        return i -> list.size() > i ? Some(list.get(i)) : None();
    }

    public static Either<CommandFailure, CommandType> parseMainCommand(List<String> args) {
        val head = args.headOption().getOrElse("");
        val picker = lift(args.tailOption().getOrElse(List.empty()));
        switch (head.toLowerCase()) {
            case "reload":
                return Right(new CommandType.Reload());
            case "send":
                return For(picker.apply(0), picker.apply(1))
                        .<CommandType>yield(CommandType.SendMessageRequest::new)
                        .toEither(CommandFailure.FewArguments::new);
            default:
                return Left(new CommandFailure.Unknown(head));
        }
    }

    public static Either<CommandFailure, CommandType> validateCommandWithSender(
            Commander sender, CommandType type
    ) {
        if (type instanceof CommandType.Permissible) {
            String permission = ((CommandType.Permissible) type).getPermission();
            return sender.hasPermission(permission)
                    ? Left(new CommandFailure.NoPermission(permission, sender))
                    : Right(((CommandType.Permissible) type).getResult());
        } else {
            return Right(type);
        }
    }

    public static Either<CommandFailure, CommandType> validateCommandWithSpicord(
            SpicordPlatform spicord, CommandType type
    ) {
        if (type instanceof CommandType.SendMessageRequest) {
            val req = (CommandType.SendMessageRequest) type;
            val channel = getOne(spicord.getData().getChannels(), req.getChannel())
                    .orElse(() -> parseLong(req.getChannel()))
                    .flatMap(spicord.getData().getDiscord()::findTextChannelById)
                    .getOrNull();
            return channel != null
                    ? Right(new CommandType.SendMessage(channel, req.getContents()))
                    : Left(new CommandFailure.InvalidChannel(req.getChannel()));
        }
        return Right(type);
    }

    public static String getMessageFromFailure(CommandFailure failure) {
        if (failure instanceof CommandFailure.Unknown
                || failure instanceof CommandFailure.FewArguments
                || failure instanceof CommandFailure.InvalidArguments) {
            return Lang.UNKNOWN_COMMAND.format();
        } else if (failure instanceof CommandFailure.Error) {
            return Lang.ERROR.format();
        } else if (failure instanceof CommandFailure.NoPermission) {
            val noPerm = (CommandFailure.NoPermission) failure;
            return Lang.PERMISSION_DENIED.format(noPerm.getPermission());
        } else if (failure instanceof CommandFailure.InvalidChannel) {
            val noChannel = (CommandFailure.InvalidChannel) failure;
            return Lang.UNKNOWN_CHANNEL.format(noChannel.getChannel());
        } else if (failure instanceof CommandFailure.Message) {
            val msg = (CommandFailure.Message) failure;
            return msg.getMessage();
        } else {
            throw new IllegalArgumentException("Unknown failure type: " + failure.getClass());
        }
    }

    public static Either<String, Tuple2<Runnable, String>> evaluateCommand(
            SpicordPlatform spicord, CommandType type) {
        if (type instanceof CommandType.Reload) {
            final Executor sync = spicord.getExecution().getSync();
            return Right(Tuple(
                    () -> sync.execute(() -> {
                        SpicordData data = loadAllSpicordData(spicord);
                        sync.execute(() -> spicord.setData(data));
                    }),
                    Lang.RELOADED.format()
            ));
        } else if (type instanceof CommandType.SendMessage) {
            val req = (CommandType.SendMessage) type;
            return Right(Tuple(
                    () -> req.getChannel().sendMessage(new TextContents(req.getContents())),
                    Lang.DISCORD_MESSAGE_SENT.format()
            ));
        }
        throw new IllegalArgumentException("Unknown command type: " + type.getClass());
    }

    public static Tuple2<Runnable, String> evaluateSpicordCommand(SpicordPlatform spicord, Commander commander, List<String> args) {
        return parseRootCommand(args)
                .flatMap(cmd -> validateCommandWithSender(commander, cmd))
                .flatMap(cmd -> validateCommandWithSpicord(spicord, cmd))
                .mapLeft(SpicordCommands::getMessageFromFailure)
                .flatMap(cmd -> evaluateCommand(spicord, cmd))
                .<Tuple2<Runnable, String>>fold(
                        msg -> Tuple(() -> { /* Nothing */ }, msg),
                        Function.identity()
                );
    }

    public static <T> T TODO() {
        throw new IllegalStateException("TODO");
    }
}
