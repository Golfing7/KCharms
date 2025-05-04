package com.golfing8.kcharm.module.effect.selection;

import lombok.Getter;
import lombok.Setter;

/**
 * A manager for controlling charm effect selection.
 */
@Getter @Setter
public class CharmEffectSelectionManager {
    private CharmEffectSelector selector;
    public CharmEffectSelectionManager() {
        // Set default functionality.
        this.selector = new CharmEffectSelectorDefault();
    }
}
