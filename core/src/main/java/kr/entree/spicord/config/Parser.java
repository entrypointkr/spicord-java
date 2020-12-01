package kr.entree.spicord.config;

import io.vavr.collection.HashMap;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.vavr.API.*;
import static io.vavr.collection.List.ofAll;

public class Parser {
    public static Option<Color> parseColor(String color) {
        return color.startsWith("#")
                ? Try.of(() -> Color.decode(color)).toOption()
                : getColor(color);
    }

    public static Option<Long> parseLong(String string) {
        return Try.of(() -> Long.parseLong(string)).toOption();
    }

    public static Option<Color> getColor(String name) {
        return Try.of(() -> {
            Field field = Color.class.getDeclaredField(name.toUpperCase());
            field.setAccessible(true);
            return (Color) field.get(null);
        }).toOption();
    }

    public static <T> Option<T> getOne(Map<String, T> map, String key) {
        return Option.of(map.get(key));
    }

    public static <T> Option<T> getOne(List<T> list, int index) {
        return list.size() > index ? Some(list.get(index)) : None();
    }

    public static Option<String> getString(Map<String, ?> map, String key) {
        return getOne(map, key).map(Object::toString);
    }

    public static Option<String> getType(Map<String, ?> map) {
        return getString(map, "type");
    }

    public static <T> Option<T> firstOne(Map<String, T> node, Seq<String> keys) {
        return keys.flatMap(k -> getOne(node, k))
                .toOption();
    }

    public static Option<String> firstString(Map<String, ?> node, Seq<String> keys) {
        return firstOne(node, keys).map(Object::toString);
    }

    public static Seq<?> toCollection(Object value) {
        return value instanceof Collection
                ? ofAll((Collection<?>) value)
                : Seq();
    }

    public static io.vavr.collection.Map<?, ?> toMap(Object value) {
        return value instanceof Map
                ? HashMap.ofAll((Map<?, ?>) value)
                : HashMap.empty();
    }

    public static io.vavr.collection.Map<String, ?> toStringMap(Object value) {
        return toMap(value).map((k, v) -> Tuple(k.toString(), v));
    }

    public static Seq<String> toStrings(Object value) {
        return toCollection(value).map(Object::toString);
    }

    public static Option<Boolean> toBoolean(Object value) {
        return Try(() -> Boolean.parseBoolean(value.toString())).toOption();
    }

    public static Option<Long> toLong(Object value) {
        return value instanceof Number
                ? Some(((Number) value).longValue())
                : Try(() -> Long.parseLong(value.toString())).toOption();
    }
}
