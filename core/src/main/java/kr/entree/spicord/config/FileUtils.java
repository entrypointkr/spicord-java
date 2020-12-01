package kr.entree.spicord.config;

import io.vavr.control.Try;

import java.io.BufferedReader;
import java.io.File;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.newBufferedWriter;

public class FileUtils {
    public static Try<Void> save(Writer writer, String contents) {
        return Try.run(() -> writer.write(contents));
    }

    public static Try<Void> saveFile(File file, String contents) {
        return Try.withResources(() -> newBufferedWriter(file.toPath(), StandardCharsets.UTF_8))
                .of(w -> save(w, contents).get());
    }

    public static Try<String> read(BufferedReader reader) {
        StringBuilder builder = new StringBuilder();
        while (true) {
            try {
                String line = reader.readLine();
                if (line == null) break;
                builder.append(line);
            } catch (Exception ex) {
                return Try.failure(ex);
            }
        }
        return Try.success(builder.toString());
    }

    public static Try<String> readFile(File file) {
        return Try.withResources(() -> newBufferedReader(file.toPath(), StandardCharsets.UTF_8))
                .of(reader -> read(reader).get());
    }
}
