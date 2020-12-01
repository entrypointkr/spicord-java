package kr.entree.spicord.jda;

import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import io.vavr.control.Try;
import kr.entree.spicord.discord.Discord;
import kr.entree.spicord.discord.channel.TextChannel;
import kr.entree.spicord.discord.contents.EmbedContents;
import kr.entree.spicord.discord.contents.MessageContents;
import kr.entree.spicord.discord.contents.TextContents;
import kr.entree.spicord.discord.message.Message;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.concurrent.Executor;

import static io.vavr.API.Option;
import static io.vavr.API.Try;

@Data
public class JDADiscord implements Discord {
    private final JDA jda;
    private final Executor executor;

    public JDADiscord(JDA jda, Executor executor) {
        this.jda = jda;
        this.executor = executor;
    }

    @Override
    public Try<Void> shutdown(boolean force) {
        jda.shutdown();
        return Try.run(() -> jda.awaitStatus(JDA.Status.SHUTDOWN));
    }

    @Override
    public Option<TextChannel> findTextChannelById(long id) {
        return Option(jda.getTextChannelById(id))
                .map(channel -> textChannelFromJDA(this.executor, channel));
    }

    @Override
    public boolean isRunning() {
        switch (jda.getStatus()) {
            case SHUTTING_DOWN:
            case SHUTDOWN:
            case FAILED_TO_LOGIN:
                return false;
            default:
                return true;
        }
    }

    @Override
    public Future<Discord> awaitReady() {
        return Future.of(executor, () -> {
            jda.awaitReady();
            return this;
        });
    }

    public static Try<Discord> createJDA(Executor executor, String token) {
        return Try(() -> JDABuilder.createDefault(token).build())
                .mapTry(jda -> new JDADiscord(jda, executor));
    }

    public static <T> Future<T> actionToFuture(RestAction<T> action, Executor executor) {
        return Future.of(executor, action::complete);
    }

    public static TextChannel textChannelFromJDA(Executor executor, net.dv8tion.jda.api.entities.TextChannel channel) {
        return new TextChannel() {
            @Override
            public Future<Message> sendMessage(MessageContents contents) {
                MessageAction action;
                if (contents instanceof EmbedContents) {
                    action = channel.sendMessage(new EmbedBuilder().build());
                } else {
                    String msg = contents instanceof TextContents
                            ? ((TextContents) contents).getText()
                            : contents.toString();
                    action = channel.sendMessage(msg);
                }
                return actionToFuture(action, executor)
                        .map(JDADiscord::messageFromJDA);
            }
        };
    }

    public static MessageEmbed createJDAEmbedFrom(EmbedContents data) {
        EmbedBuilder builder = new EmbedBuilder();
        Option(data.getTitle()).peek(builder::setTitle);
        Option(data.getDescription()).peek(builder::setDescription);
        Option(data.getTitle()).peek(builder::setTitle);
        Option(data.getColor()).peek(builder::setColor);
        Option(data.getThumbnail()).peek(builder::setThumbnail);
        Option(data.getAuthor()).peek(a ->
                builder.setAuthor(a.getName(), a.getLinkUrl(), a.getIconUrl()));
        Option(data.getFooter()).peek(builder::setFooter);
        data.getFields().forEach(f ->
                builder.addField(f.getName(), f.getValue(), f.isInline()));
        return builder.build();
    }

    public static Message messageFromJDA(net.dv8tion.jda.api.entities.Message jdaMessage) {
        return new Message() {
            @Override
            public MessageContents getContents() {
                return new TextContents(jdaMessage.getContentRaw());
            }
        };
    }
}
