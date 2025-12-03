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
        return module.getCharmEffects().keySet();
    }, (ctx) -> {
        CharmModule module = CharmModule.get();
        return module.getCharmEffects().containsKey(ctx.getArgument());
    }, (ctx) -> {
        CharmModule module = CharmModule.get();
        return module.getCharms().get(ctx.getArgument());
    });

    @Override
    protected void onRegister() {
        addSubCommand(new CharmGiveCommand());
    }
}
