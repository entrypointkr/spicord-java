package kr.entree.spicord.config;

import io.vavr.Function1;
import io.vavr.collection.HashMap;
import io.vavr.control.Option;
import kr.entree.spicord.discord.contents.EmbedContents;
import kr.entree.spicord.discord.contents.MessageContents;
import kr.entree.spicord.discord.contents.TextContents;

import java.util.Map;

import static io.vavr.API.*;
import static kr.entree.spicord.config.Parser.*;
import static kr.entree.spicord.config.SpicordConfig.emptyConfig;

public class SpicordParser {
    public static Option<MessageContents> parseMessage(Map<String, ?> node, String type) {
        switch (type.toLowerCase()) {
            case "text":
                return parseTextMessage(node);
            case "embed":
                return Option((MessageContents) parseEmbedMessage(node));
            default:
                return None();
        }
    }

    public static Option<MessageContents> parseTextMessage(Map<String, ?> node) {
        return getOne(node, "value")
                .map(Parser::toStrings)
                .map(l -> String.join("\n", l))
                .map(TextContents::new);
    }

    public static EmbedContents parseEmbedMessage(Map<String, ?> node) {
        EmbedContents.Builder builder = EmbedContents.builder();
        firstOne(node, Seq("title", "value"))
                .map(Object::toString)
                .peek(builder::setTitle);
        firstOne(node, Seq("description", "desc"))
                .map(Object::toString)
                .peek(builder::setDescription);
        getOne(node, "color")
                .map(Object::toString)
                .flatMap(Parser::parseColor)
                .peek(builder::setColor);
        getOne(node, "thumbnail")
                .map(Object::toString)
                .peek(builder::setThumbnail);
        EmbedContents.Author.parseAuthor(node)
                .peek(builder::setAuthor);
        getOne(node, "footer")
                .map(Object::toString)
                .peek(builder::setFooter);
        builder.setFields(getOne(node, "fields").toList()
                .flatMap(Parser::toStrings)
                .flatMap(EmbedContents.Field::parseFieldFromString).toJavaList());
        return builder.build();
    }

    public static Map<String, Long> parseChannels(Map<String, ?> map) {
        return HashMap.ofAll(map)
                .flatMap((k, v) -> Parser.toLong(v)
                        .map(l -> Tuple(k, l)))
                .toJavaMap();
    }

    public static Map<String, Long> parseGuilds(Map<String, ?> map) {
        return HashMap.ofAll(map)
                .flatMap((k, v) -> Parser.toLong(v)
                        .map(l -> Tuple(k, l)))
                .toJavaMap();
    }

    public static <T> Option<T> parseNode(Map<String, ?> map, Function1<String, Option<T>> parser) {
        return getString(map, "type").flatMap(parser);
    }

    public static SpicordConfig parseConfig(Map<String, ?> map) {
        return new SpicordConfig(
                getString(map, "token").getOrElse(emptyConfig().getToken())
        );
    }

    public static Map<String, MessageContents> parseMessages(Map<String, ?> map) {
        return HashMap.ofAll(map)
                .flatMap((k, v) -> getType(map)
                        .flatMap(type -> parseMessage(map, type))
                        .map(c -> Tuple(k, c)))
                .toJavaMap();
    }
}
