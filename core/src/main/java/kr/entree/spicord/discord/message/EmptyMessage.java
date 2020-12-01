package kr.entree.spicord.discord.message;

import kr.entree.spicord.discord.contents.MessageContents;
import kr.entree.spicord.discord.contents.TextContents;

public class EmptyMessage implements Message {
    @Override
    public MessageContents getContents() {
        return TextContents.EMPTY;
    }
}
