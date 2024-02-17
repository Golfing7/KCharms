package com.golfing8.kcharm.module.cmd;

import com.golfing8.kcharm.module.CharmModule;
import com.golfing8.kcharm.module.struct.Charm;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.MCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.config.lang.LangConf;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.util.PlayerUtil;
import org.bukkit.entity.Player;

/**
 * Lets admins give players charms
 */
@Cmd(
        name = "give",
        description = "Give a player a charm"
)
public class CharmGiveCommand extends MCommand<CharmModule> {
    @LangConf
    private Message giveCharmMessage = new Message("&aGave &e{PLAYER} &aa &e{CHARM} &acharm!");
    @LangConf
    private Message receivedCharmMessage = new Message("&aReceived a &e{CHARM} &acharm!");

    @Override
    protected void onRegister() {
        addArgument("player", CommandArguments.PLAYER);
        addArgument("charm", CharmCommand.CHARM_ARGUMENT);
    }

    @Override
    protected void execute(CommandContext context) {
        Player player = context.next();
        Charm charm = context.next();

        PlayerUtil.givePlayerItemSafe(player, charm.buildItem());

        giveCharmMessage.send(context.getSender(), "PLAYER", player.getName(), "CHARM", charm.getDisplayName());
        receivedCharmMessage.send(player, "CHARM", charm.getDisplayName());
    }
}
