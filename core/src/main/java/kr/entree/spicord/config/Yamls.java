package kr.entree.spicord.config;

import io.vavr.control.Try;
import org.intellij.lang.annotations.Language;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;

public class Yamls {
    private static final Yaml yaml = createYaml();

    private static Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    }

    public static Map<String, Object> loadYaml(@Language("yaml") String contents) {
        return Try.of(() -> yaml.<Map<String, Object>>load(contents))
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

    public static String saveYaml(Map<String, ?> map) {
        return yaml.dump(map);
    }
}
