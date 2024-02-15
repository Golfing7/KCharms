package com.golfing8.kcharm;

import com.golfing8.kcommon.KPlugin;

/**
 * Main controlling plugin for kcharms.
 */
public class KCharms extends KPlugin {
    private static KCharms instance;

    @Override
    public void onEnableInner() {
        instance = this;
    }
}
