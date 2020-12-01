package kr.entree.spicord;

import io.vavr.collection.List;
import io.vavr.control.Try;
import kr.entree.spicord.command.Commander;
import kr.entree.spicord.config.FileUtils;
import kr.entree.spicord.config.SpicordParser;
import kr.entree.spicord.discord.Discord;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static io.vavr.API.Option;
import static kr.entree.spicord.SpicordData.dataOf;
import static kr.entree.spicord.command.SpicordCommands.evaluateSpicordCommand;
import static kr.entree.spicord.jda.JDADiscord.createJDA;

/**
 * Utils for effect
 */
@UtilityClass
public class SpicordRoutines {
    public static SpicordData loadAllSpicordData(
            SpicordData from,
            Function<String, Map<String, Object>> parser,
            SpicordPath path
    ) {
        Function<File, Map<String, Object>> loader =
                file -> FileUtils.readFile(file)
                        .map(parser)
                        .getOrElse(Collections.emptyMap());
        return dataOf(
                from.getDiscord(),
                Option(path.getConfigFile()).map(loader)
                        .map(SpicordParser::parseConfig)
                        .getOrElse(from.getConfig()),
                Option(path.getGuildsFile()).map(loader)
                        .map(SpicordParser::parseGuilds)
                        .getOrElse(from.getGuilds()),
                Option(path.getChannelsFile()).map(loader)
                        .map(SpicordParser::parseChannels)
                        .getOrElse(from.getChannels()),
                Option(path.getMessagesFile()).map(loader)
                        .map(SpicordParser::parseMessages)
                        .getOrElse(from.getMessages())
        );
    }

    public static SpicordData loadAllSpicordData(SpicordPlatform sp) {
        return loadAllSpicordData(sp.getData(), sp::loadYaml, sp.getPath());
    }

    public static Try<Void> executeSpicordCommand(SpicordPlatform spicord, Commander commander, List<String> args) {
        return evaluateSpicordCommand(spicord, commander, args)
                .apply((runnable, message) -> Try.runRunnable(runnable)
                        .onSuccess(__ -> commander.sendMessage(message)));
    }

    public static Try<Discord> createJDADiscord(SpicordPlatform platform) {
        return createJDA(platform.getExecution().getAsync(), platform.getData().getConfig().getToken());
    }
}
