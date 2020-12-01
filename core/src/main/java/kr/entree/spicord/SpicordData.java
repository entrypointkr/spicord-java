package kr.entree.spicord;

import kr.entree.spicord.config.SpicordConfig;
import kr.entree.spicord.discord.Discord;
import kr.entree.spicord.discord.contents.MessageContents;
import lombok.Data;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static kr.entree.spicord.config.SpicordConfig.emptyConfig;
import static kr.entree.spicord.config.SpicordParser.*;
import static kr.entree.spicord.discord.Discords.emptyDiscord;

@Data
public class SpicordData {
    private final Discord discord;
    private final SpicordConfig config;
    private final Map<String, Long> guilds;
    private final Map<String, Long> channels;
    private final Map<String, MessageContents> messages;

    public static SpicordData emptyData() {
        return dataOf(emptyDiscord(), emptyConfig(), emptyMap(), emptyMap(), emptyMap());
    }

    public static SpicordData dataOf(Discord discord, SpicordConfig config,
                                     Map<String, Long> guilds, Map<String, Long> channels, Map<String, MessageContents> messages) {
        return new SpicordData(discord, config, guilds, channels, messages);
    }

    // This is why the `optional param` is matter
    public SpicordData withDiscord(Discord newDiscord) {
        return dataOf(newDiscord, config, guilds, channels, messages);
    }

    public SpicordData withConfig(SpicordConfig newConfig) {
        return dataOf(discord, newConfig, guilds, channels, messages);
    }

    public SpicordData withGuilds(Map<String, Long> newGuilds) {
        return dataOf(discord, config, newGuilds, channels, messages);
    }

    public SpicordData withChannels(Map<String, Long> newChannels) {
        return dataOf(discord, config, guilds, newChannels, messages);
    }

    public SpicordData withMessages(Map<String, MessageContents> newMessages) {
        return dataOf(discord, config, guilds, channels, newMessages);
    }

    public SpicordData loadConfig(Map<String, Object> raw) {
        return withConfig(parseConfig(raw));
    }

    public SpicordData loadGuilds(Map<String, Object> raw) {
        return withGuilds(parseGuilds(raw));
    }

    public SpicordData loadChannels(Map<String, Object> raw) {
        return withChannels(parseChannels(raw));
    }

    public SpicordData loadMessages(Map<String, Object> raw) {
        return withMessages(parseMessages(raw));
    }
}
