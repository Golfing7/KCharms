package com.golfing8.kcharm.module.cmd;

import com.golfing8.kcharm.module.CharmModule;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.MCommand;

/**
 * Main controlling command for {@code /charm}.
 */
@Cmd(
        name = "charm"
)
public class CharmCommand extends MCommand<CharmModule> {
    @Override
    protected void onRegister() {

    }
}
