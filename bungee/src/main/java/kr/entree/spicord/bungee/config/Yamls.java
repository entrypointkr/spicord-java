package kr.entree.spicord.bungee.config;

import io.vavr.Tuple2;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.StringWriter;
import java.util.Map;
import java.util.stream.Collectors;

import static io.vavr.API.Tuple;

public class Yamls {
    public static Map<String, Object> parseYaml(String contents) {
        Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(contents);
        return config.getKeys().stream()
                .map(key -> Tuple(key, config.get(key)))
                .collect(Collectors.toMap(
                        Tuple2::_1,
                        Tuple2::_2
                ));
    }

    public static String saveYaml(Map<String, Object> map) {
        Configuration config = new Configuration();
        StringWriter writer = new StringWriter();
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, writer);
        return writer.toString();
    }
}
