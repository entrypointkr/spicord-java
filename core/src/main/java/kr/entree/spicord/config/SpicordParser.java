package kr.entree.spicord.config;

import io.vavr.Function1;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Option;
import kr.entree.spicord.discord.contents.EmbedContents;
import kr.entree.spicord.discord.contents.MessageContents;
import kr.entree.spicord.discord.contents.TextContents;
import lombok.val;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static io.vavr.API.*;
import static kr.entree.spicord.config.Parser.*;
import static kr.entree.spicord.config.SpicordConfig.emptyConfig;

public class SpicordParser {
    public static Option<MessageContents> parseMessage(Map<String, ?> node, String type) {
        switch (type.toLowerCase()) {
            case "text":
                return parseTextMessage(node);
            case "embed":
                return Option(parseEmbedMessage(node));
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
                .peek(builder::title);
        getOne(node, "url")
                .map(Object::toString)
                .peek(builder::url);
        firstOne(node, Seq("description", "desc"))
                .map(Object::toString)
                .peek(builder::description);
        getOne(node, "color")
                .map(Object::toString)
                .flatMap(Parser::parseColor)
                .peek(builder::color);
        getOne(node, "thumbnail")
                .map(Object::toString)
                .peek(builder::thumbnail);
        getOne(node, "author")
                .map(Parser::toStringMap)
                .flatMap(EmbedContents.Author::parseAuthor)
                .peek(builder::author);
        getOne(node, "footer")
                .map(Object::toString)
                .peek(builder::footer);
        getOne(node, "fields")
                .map(Parser::toStrings)
                .map(lines -> lines.flatMap(EmbedContents.Field::parseFieldFromString)
                        .toJavaList())
                .peek(builder::fields);
        return builder.build();
    }

    public static Map<String, Long> parseChannels(Map<String, ?> map) {
        return io.vavr.collection.LinkedHashMap.ofAll(map)
                .flatMap((k, v) -> Parser.toLong(v)
                        .map(l -> Tuple(k, l)))
                .toJavaMap();
    }

    public static Map<String, Long> parseGuilds(Map<String, ?> map) {
        return io.vavr.collection.LinkedHashMap.ofAll(map)
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

    public static Map<String, MessageContents> parseMessages(Map<String, Object> map) {
        return io.vavr.collection.LinkedHashMap.ofAll(map)
                .mapValues(Parser::toStringMap)
                .flatMap((k, v) -> getType(v)
                        .flatMap(type -> parseMessage(v, type))
                        .map(c -> Tuple(k, c)))
                .toJavaMap();
    }

    public static Map<String, Object> complicateMessages(Map<String, Object> map) {
        return io.vavr.collection.LinkedHashMap.ofAll(map)
                .<Object>mapValues(SpicordParser::complicateMessage)
                .toJavaMap();
    }

    public static Map<String, Object> complicateMessage(Object value) {
        if (value instanceof Collection) {
            return io.vavr.collection.LinkedHashMap.<String, Object>of(
                    "type", "text",
                    "value", ((Collection<?>) value).stream()
                            .map(Object::toString)
                            .collect(Collectors.joining("\n"))
            ).toJavaMap();
        } else if (value instanceof Map) {
            Map<String, Object> ret = new LinkedHashMap<>();
            ret.put("type", "embed");
            ret.putAll(toStringMap(value));
            return ret;
        } else {
            return io.vavr.collection.LinkedHashMap.<String, Object>of(
                    "type", "text",
                    "value", value.toString()
            ).toJavaMap();
        }
    }

    public static Map<String, Object> simplifyMessages(Map<String, Object> messages) {
        return io.vavr.collection.LinkedHashMap.ofAll(messages)
                .mapValues(Parser::toStringMap)
                .flatMap((k, msg) -> getType(msg)
                        .flatMap(type -> simplifyMessage(msg, type))
                        .map(v -> Tuple(k, v)))
                .toJavaMap();
    }

    public static Option<Object> simplifyMessage(Map<String, Object> message, String type) {
        switch (type) {
            case "text":
                return getOne(message, "value")
                        .map(Object::toString);
            case "embed":
                return Some(io.vavr.collection.LinkedHashMap.ofAll(message)
                        .filterKeys(k -> !k.equalsIgnoreCase("type"))
                        .toJavaMap());
            default:
                return Some(message);
        }
    }

    public static Map<String, Object> saveMessage(MessageContents contents) {
        if (contents instanceof TextContents) {
            return io.vavr.collection.LinkedHashMap.<String, Object>of(
                    "type", "text",
                    "value", ((TextContents) contents).getText()
            ).toJavaMap();
        } else if (contents instanceof EmbedContents) {
            val embed = (EmbedContents) contents;
            return io.vavr.collection.LinkedHashMap.ofEntries(List.<Tuple2<String, Option<Object>>>of(
                    Tuple("type", Some("embed")),
                    Tuple("title", Option(embed.getTitle())),
                    Tuple("url", Option(embed.getUrl())),
                    Tuple("desc", Option(embed.getDescription())),
                    Tuple("color", Option(embed.getColor())
                            .map(SpicordParser::saveColor)),
                    Tuple("thumbnail", Option(embed.getThumbnail())),
                    Tuple("author", Option(embed.getAuthor())
                            .map(EmbedContents.Author::serialize)),
                    Tuple("footer", Option(embed.getFooter())),
                    Tuple("fields", Option(embed.getFields())
                            .map(fields -> List.ofAll(fields)
                                    .map(EmbedContents.Field::serialize)
                                    .toJavaList()))
            ).flatMap(pair ->
                    pair._2.map(v -> Tuple(pair._1, v))
            )).toJavaMap();
        }
        return Collections.emptyMap();
    }

    public static String saveColor(Color color) {
        return "#" + Integer.toHexString(color.getRGB()).substring(2);
    }

    public static Map<String, Object> saveMessages(Map<String, MessageContents> messages) {
        return io.vavr.collection.LinkedHashMap.ofAll(messages)
                .<String, Object>map((k, v) -> Tuple(k, saveMessage(v)))
                .toJavaMap();
    }
}
