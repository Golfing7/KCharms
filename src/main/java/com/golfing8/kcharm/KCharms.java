package com.golfing8.kcharm;

import com.golfing8.kcommon.KPlugin;
import lombok.Getter;

/**
 * Main controlling plugin for kcharms.
 */
public class KCharms extends KPlugin {
    @Getter
    private static KCharms instance;

    @Override
    public void onEnableInner() {
        instance = this;
    }
}
