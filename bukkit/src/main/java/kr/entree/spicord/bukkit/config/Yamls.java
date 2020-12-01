package kr.entree.spicord.bukkit.config;

import io.vavr.control.Try;
import kr.entree.spicord.config.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.intellij.lang.annotations.Language;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;

public class Yamls {
    public static Map<String, Object> loadYaml(@Language("yaml") String contents) {
        YamlConfiguration yaml = new YamlConfiguration();
        return Try.run(() -> yaml.loadFromString(contents))
                .map(__ -> yaml.getValues(false))
                .getOrElse(Collections.emptyMap());
    }

    public static Map<String, Object> loadYaml(BufferedReader reader) {
        return FileUtils.read(reader)
                .map(Yamls::loadYaml)
                .getOrElse(Collections.emptyMap());
    }

    public static Map<String, Object> loadYaml(File file) {
        return Try.withResources(() -> Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8))
                .of(Yamls::loadYaml)
                .getOrElse(Collections.emptyMap());
    }

    public static String saveYaml(Map<String, Object> map) {
        YamlConfiguration config = new YamlConfiguration();
        map.forEach(config::set);
        return config.saveToString();
    }
}
