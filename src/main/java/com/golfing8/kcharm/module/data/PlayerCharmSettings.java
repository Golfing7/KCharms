package com.golfing8.kcharm.module.data;

import com.golfing8.kcommon.data.SenderSerializable;
import lombok.Getter;

/**
 * Stores per-player settings related to charms.
 */
public class PlayerCharmSettings extends SenderSerializable {
    /** If this player should receive action bar messages from charms. */
    @Getter
    private boolean actionBarEnabled = true;

    public void setActionBarEnabled(boolean actionBarEnabled) {
        this.actionBarEnabled = actionBarEnabled;
        change();
    }
}
