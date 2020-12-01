package kr.entree.spicord.bukkit;

import kr.entree.spicord.config.SpicordConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static kr.entree.spicord.bukkit.config.Yamls.loadYaml;
import static kr.entree.spicord.config.SpicordParser.parseConfig;

public class ConfigTest {
    @Test
    public void config() {
        SpicordConfig config = parseConfig(loadYaml("token: BOT_TOKEN"));
        Assertions.assertEquals("BOT_TOKEN", config.getToken());
    }
}
