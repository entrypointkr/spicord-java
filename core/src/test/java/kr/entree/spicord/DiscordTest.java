package kr.entree.spicord;

import kr.entree.spicord.discord.Discord;
import kr.entree.spicord.discord.channel.TextChannel;
import kr.entree.spicord.discord.contents.TextContents;
import kr.entree.spicord.discord.message.Message;
import kr.entree.spicord.jda.JDADiscord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class DiscordTest {
    @Test
    public void jda() {
        String token = System.getProperty("spicord.test.token", "");
        assumeTrue(token != null && !token.isEmpty());
        Discord discord = JDADiscord.createJDA(Runnable::run, token).get().awaitReady().get();
        TextChannel channel = discord.findTextChannelById(650292794715537419L).get();
        assertDoesNotThrow(() -> {
            TextContents contents = new TextContents("Hi!");
            Message msg = channel.sendMessage(contents).get();
            assertEquals(contents, msg.getContents());
        });
        discord.shutdown(true);
    }
}
