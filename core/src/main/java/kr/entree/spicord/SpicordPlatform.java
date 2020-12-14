package kr.entree.spicord;

import java.util.logging.Logger;

public interface SpicordPlatform {
    SpicordData getData();

    /**
     * Unsafely perform the effect: update the data.
     * This should affects the return value of [SpicordPlatform#getData].
     *
     * @param data the spicord data for update.
     */
    void setData(SpicordData data);

    SpicordPath getPath();

    Execution getExecution();

    Logger getLogger();
}
