package com.golfing8.kcharm.module.cmd;

import com.golfing8.kcharm.module.CharmModule;
import com.golfing8.kcharm.module.data.PlayerCharmSettings;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.MCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.config.lang.LangConf;
import com.golfing8.kcommon.config.lang.Message;
import org.bukkit.entity.Player;

/**
 * Lets players toggle whether they receive action bar messages from charms.
 */
@Cmd(
        name = "actionbar",
        description = "Toggle action bar messages from charms",
        forPlayers = true
)
public class ActionBarToggleCommand extends MCommand<CharmModule> {
    @LangConf
    private Message toggledMessage = new Message("&aCharm action bar messages are now &e{STATE}&a.");

    @Override
    protected void onRegister() {
        // If the player doesn't specify on/off, flip whatever their current state is.
        addArgument("on/off", CommandArguments.BOOLEAN_STATE, sender ->
                getModule().isActionBarEnabled((Player) sender) ? "off" : "on")
                .setAutoFillPlayersOnly(true);
    }

    @Override
    protected void execute(CommandContext context) {
        Player player = context.getPlayer();
        boolean newState = context.next();

        PlayerCharmSettings settings = getModule().getSettings(player);
        settings.setActionBarEnabled(newState);

        toggledMessage.send(player, "STATE", newState ? "enabled" : "disabled");
    }
}
