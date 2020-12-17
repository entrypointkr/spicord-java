package kr.entree.spicord;

import io.vavr.collection.List;
import io.vavr.concurrent.Future;
import io.vavr.control.Try;
import kr.entree.spicord.command.Commander;
import kr.entree.spicord.config.SpicordParser;
import kr.entree.spicord.config.Yamls;
import kr.entree.spicord.discord.Discord;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.vavr.API.Option;
import static io.vavr.Predicates.not;
import static kr.entree.spicord.SpicordData.dataOf;
import static kr.entree.spicord.command.SpicordCommands.evaluateSpicordCommand;
import static kr.entree.spicord.config.FileUtils.readFileContents;
import static kr.entree.spicord.config.FileUtils.saveFile;
import static kr.entree.spicord.config.SpicordConfig.*;
import static kr.entree.spicord.config.SpicordParser.saveMessages;
import static kr.entree.spicord.config.Yamls.saveYaml;
import static kr.entree.spicord.discord.Discords.preparingDiscord;
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
                file -> readFileContents(file)
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
                        .map(SpicordParser::complicateMessages)
                        .map(SpicordParser::parseMessages)
                        .getOrElse(from.getMessages())
        );
    }

    public static SpicordData loadAllSpicordData(SpicordPlatform sp) {
        return loadAllSpicordData(sp.getData(), Yamls::loadYaml, sp.getPath());
    }

    public static void createSpicordFiles(SpicordPath paths) {
        Option(paths.getConfigFile())
                .filter(not(File::isFile))
                .peek(file -> saveFile(file, saveYaml(defaultConfig().serialize())));
        Option(paths.getGuildsFile())
                .filter(not(File::isFile))
                .peek(file -> saveFile(file, saveYaml(defaultGuilds())));
        Option(paths.getChannelsFile())
                .filter(not(File::isFile))
                .peek(file -> saveFile(file, saveYaml(defaultChannels())));
        Option(paths.getMessagesFile())
                .filter(not(File::isFile))
                .peek(file -> saveFile(file, saveYaml(saveMessages(defaultMessages()))));
    }

    public static Try<Void> executeSpicordCommand(SpicordPlatform sp, Commander commander, List<String> args) {
        return evaluateSpicordCommand(sp, commander, args)
                .apply((runnable, message) -> Try.runRunnable(runnable)
                        .onSuccess(__ -> commander.sendMessage(message))
                        .onFailure(th -> logError(sp.getLogger(), th)));
    }

    public static Try<Discord> createJDADiscord(SpicordPlatform platform) {
        return createJDA(platform.getExecution().getAsync(), platform.getData().getConfig().getToken());
    }

    public static void logError(Logger logger, Throwable throwable) {
        logger.log(Level.WARNING, throwable, () -> "Error!");
    }

    public static Consumer<Discord> discordSetter(SpicordPlatform sp) {
        return newDiscord -> sp.setData(sp.getData().withDiscord(newDiscord));
    }

    public static void reconnectDiscord(SpicordPlatform sp) {
        sp.setData(sp.getData().withDiscord(preparingDiscord()));
        Future.of(sp.getExecution().getAsync(), () ->
                createJDADiscord(sp).peek(discordSetter(sp)));
    }
}
