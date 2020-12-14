package kr.entree.spicord.config;

import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import kr.entree.spicord.discord.contents.EmbedContents;
import kr.entree.spicord.discord.contents.MessageContents;
import kr.entree.spicord.discord.contents.TextContents;
import lombok.Data;

import java.awt.*;
import java.util.Collections;
import java.util.Map;

import static io.vavr.API.Tuple;
import static io.vavr.collection.HashMap.ofEntries;
import static java.util.Collections.emptyList;

@Data
public class SpicordConfig {
    private final String token;

    public SpicordConfig(String token) {
        this.token = token;
    }

    public static SpicordConfig emptyConfig() {
        return new SpicordConfig("");
    }

    public static SpicordConfig defaultConfig() {
        return new SpicordConfig("your-bot-token");
    }

    public static Map<String, Long> defaultGuilds() {
        return ofEntries(
                Tuple("main", 1234L)
        ).toJavaMap();
    }

    public static Map<String, Long> defaultChannels() {
        return ofEntries(
                Tuple("chat", 1234L),
                Tuple("announce", 1234L)
        ).toJavaMap();
    }

    public static Map<String, MessageContents> defaultMessages() {
        return LinkedHashMap.of(
                "simple", new TextContents("simple msg"),
                "multi", new TextContents("multiline 1\n2"),
                "embed", EmbedContents.builder()
                        .title("사유: %reason%")
                        .description("%player%(%uuid%)")
                        .url("https://ko.namemc.com/search?q=%player%")
                        .thumbnail("https://crafatar.com/avatars/%uuid%?overlay=true")
                        .color(Color.ORANGE)
                        .author(EmbedContents.Author.builder()
                                .name("서버에서 추방되었습니다.")
                                .iconUrl("https://crafatar.com/avatars/%uuid%?overlay=true")
                                .build())
                        .build()
        ).toJavaMap();
    }

    public Map<String, Object> serialize() {
        return HashMap.<String, Object>ofEntries(
                Tuple("token", getToken())
        ).toJavaMap();
    }
}
