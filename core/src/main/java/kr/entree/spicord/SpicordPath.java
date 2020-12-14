package kr.entree.spicord;

import lombok.Data;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data(staticConstructor = "pathOf")
public class SpicordPath {
    private final File configFile;
    private final File guildsFile;
    private final File channelsFile;
    private final File messagesFile;

    public static SpicordPath ofEmpty() {
        return new SpicordPath(null, null, null, null);
    }

    public SpicordPath withConfigFile(File configFile) {
        return new SpicordPath(configFile, this.guildsFile, this.channelsFile, this.messagesFile);
    }

    public SpicordPath withGuildsFile(File guildsFile) {
        return new SpicordPath(this.configFile, guildsFile, this.channelsFile, this.messagesFile);
    }

    public SpicordPath withChannelsFile(File channelsFile) {
        return new SpicordPath(channelsFile, this.guildsFile, channelsFile, this.messagesFile);
    }

    public SpicordPath withMessagesFile(File messagesFile) {
        return new SpicordPath(this.configFile, this.guildsFile, this.channelsFile, messagesFile);
    }
}
