package kr.entree.spicord.discord.contents;

import lombok.Data;

@Data
public class TextContents implements MessageContents {
    public static final TextContents EMPTY = new TextContents("");

    private final String text;
}
