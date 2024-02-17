package com.golfing8.kcharm.module.cmd;

import com.golfing8.kcharm.module.CharmModule;
import com.golfing8.kcharm.module.struct.Charm;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.MCommand;
import com.golfing8.kcommon.command.argument.CommandArgument;

/**
 * Main controlling command for {@code /charm}.
 */
@Cmd(
        name = "charm"
)
public class CharmCommand extends MCommand<CharmModule> {
    static final CommandArgument<Charm> CHARM_ARGUMENT = new CommandArgument<>("A charm", (ctx) -> {
        CharmModule module = CharmModule.get();
        return module.getCharms().keySet();
    }, (ctx) -> {
        CharmModule module = CharmModule.get();
        return module.getCharms().containsKey(ctx.getArgument());
    }, (str) -> {
        CharmModule module = CharmModule.get();
        return module.getCharms().get(str);
    });

    @Override
    protected void onRegister() {
        addSubCommand(new CharmGiveCommand());
    }
}
