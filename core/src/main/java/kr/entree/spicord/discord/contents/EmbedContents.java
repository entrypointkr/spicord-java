package kr.entree.spicord.discord.contents;

import io.vavr.control.Option;
import kr.entree.spicord.config.Parser;
import lombok.Builder;
import lombok.Data;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static io.vavr.API.*;
import static io.vavr.collection.HashMap.ofEntries;
import static kr.entree.spicord.config.Parser.getOne;
import static kr.entree.spicord.config.Parser.getString;


@Data
@Builder(
        builderClassName = "Builder",
        setterPrefix = "set"
)
public class EmbedContents {
    private final String title;
    private final String description;
    private final Color color;
    private final String thumbnail;
    private final Author author;
    private final String footer;
    private final List<Field> fields;

    @Data
    public static class Field {
        private final String name;
        private final String value;
        private final boolean inline;

        public static Option<Field> parseField(Map<String, ?> map) {
            return For(
                    getString(map, "name"),
                    getString(map, "value"),
                    Lazy(() -> getOne(map, "inline")
                            .flatMap(Parser::toBoolean)
                            .getOrElse(false))
            ).yield(Field::new).toOption();
        }

        public static Option<Field> parseFieldFromString(String serialized) {
            String[] sliced = serialized.split("\\|");
            if (sliced.length >= 2) {
                return parseField(ofEntries(
                        Tuple("name", sliced[0]),
                        Tuple("value", sliced[1])
                ).toJavaMap());
            } else {
                return None();
            }
        }
    }

    @Data
    @lombok.Builder
    public static class Author {
        private final String name;
        private final String linkUrl;
        private final String iconUrl;

        public static Option<Author> parseAuthor(Map<String, ?> map) {
            return getOne(map, "name")
                    .map(Object::toString)
                    .map(name -> new Author(
                            name,
                            getOne(map, "link")
                                    .map(Object::toString).getOrNull(),
                            getOne(map, "icon")
                                    .map(Object::toString).getOrNull()
                    ));
        }
    }
}
