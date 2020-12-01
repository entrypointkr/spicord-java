package kr.entree.spicord.config;

import io.vavr.collection.HashMap;

import java.util.Map;

import static io.vavr.API.Tuple;
import static kr.entree.spicord.config.Parser.getString;

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

    public String getToken() {
        return token;
    }

    public Map<String, Object> serialize() {
        return HashMap.<String, Object>ofEntries(
                Tuple("token", getToken())
        ).toJavaMap();
    }
}
