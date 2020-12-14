package kr.entree.spicord;

import io.vavr.collection.LinkedHashMap;
import kr.entree.spicord.config.Yamls;
import kr.entree.spicord.discord.contents.EmbedContents;
import kr.entree.spicord.discord.contents.MessageContents;
import kr.entree.spicord.discord.contents.TextContents;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.*;
import java.io.File;

import static kr.entree.spicord.SpicordData.emptyData;
import static kr.entree.spicord.SpicordPath.pathOf;
import static kr.entree.spicord.SpicordRoutines.createSpicordFiles;
import static kr.entree.spicord.SpicordRoutines.loadAllSpicordData;
import static kr.entree.spicord.config.SpicordConfig.*;
import static kr.entree.spicord.config.SpicordParser.*;
import static kr.entree.spicord.config.Yamls.saveYaml;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigTest {
    final java.util.LinkedHashMap<String, MessageContents> messages = LinkedHashMap.of(
            "simple", new TextContents("simple msg"),
            "multi", new TextContents("multiline 1\n2"),
            "embed", EmbedContents.builder()
                    .title("사유: %reason%")
                    .url("https://ko.namemc.com/search?q=%player%")
                    .description("%player%(%uuid%)")
                    .thumbnail("https://crafatar.com/avatars/%uuid%?overlay=true")
                    .color(Color.ORANGE)
                    .author(EmbedContents.Author.builder()
                            .name("서버에서 추방되었습니다.")
                            .iconUrl("https://crafatar.com/avatars/%uuid%?overlay=true")
                            .build())
                    .build()
    ).toJavaMap();
    final String messagesText = "simple: simple msg\n" +
            "multi: |-\n" +
            "  multiline 1\n" +
            "  2\n" +
            "embed:\n" +
            "  title: '사유: %reason%'\n" +
            "  url: https://ko.namemc.com/search?q=%player%\n" +
            "  desc: '%player%(%uuid%)'\n" +
            "  color: '#ffc800'\n" +
            "  thumbnail: https://crafatar.com/avatars/%uuid%?overlay=true\n" +
            "  author:\n" +
            "    name: 서버에서 추방되었습니다.\n" +
            "    icon: https://crafatar.com/avatars/%uuid%?overlay=true\n";

    @Test
    public void deserializeMessages() {
        // TODO
        assertEquals(messagesText, saveYaml(simplifyMessages(saveMessages(messages))));
    }

    @Test
    public void serializeMessages() {
        assertEquals(
                messagesText,
                saveYaml(simplifyMessages(saveMessages(defaultMessages())))
        );
    }

    @Test
    public void adjustMessages() {
        assertEquals(
                LinkedHashMap.of(
                        "simple", LinkedHashMap.of(
                                "type", "text",
                                "value", "simple msg"
                        ),
                        "multi", LinkedHashMap.of(
                                "type", "text",
                                "value", "multiline 1\n2"
                        ),
                        "embed", LinkedHashMap.of(
                                "type", "embed",
                                "title", "사유: %reason%",
                                "url", "https://ko.namemc.com/search?q=%player%",
                                "desc", "%player%(%uuid%)",
                                "color", "#ffc800",
                                "thumbnail", "https://crafatar.com/avatars/%uuid%?overlay=true",
                                "author", LinkedHashMap.of(
                                        "name", "서버에서 추방되었습니다.",
                                        "icon", "https://crafatar.com/avatars/%uuid%?overlay=true"
                                ).toJavaMap()
                        )
                ).mapValues(LinkedHashMap::toJavaMap).toJavaMap().entrySet(),
                complicateMessages(saveMessages(messages)).entrySet()
        );
    }

    @Test
    public void loadAll(@TempDir File tempDir) {
        SpicordPath path = pathOf(
                new File(tempDir, "config.yml"),
                new File(tempDir, "guilds.yml"),
                new File(tempDir, "channels.yml"),
                new File(tempDir, "messages.yml")
        );
        createSpicordFiles(path);
        SpicordData data = loadAllSpicordData(emptyData(), Yamls::loadYaml, path);
        assertEquals(defaultConfig(), data.getConfig());
        assertEquals(defaultGuilds(), data.getGuilds());
        assertEquals(defaultChannels(), data.getChannels());
        assertEquals(defaultMessages(), data.getMessages());
    }
}
