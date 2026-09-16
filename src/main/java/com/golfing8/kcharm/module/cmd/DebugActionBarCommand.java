package com.golfing8.kcharm.module.cmd;

import com.golfing8.kcharm.module.CharmModule;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.MCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.util.MS;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Debug-only command that spams the sender's action bar on a repeating timer, simulating a
 * "foreign" action bar sender (e.g. an item cooldown) so {@link com.golfing8.kcharm.module.task.MessageTask}'s
 * pausing behavior can be tested in-game.
 */
@Cmd(
        name = "debugactionbar",
        description = "Spams your action bar on a toggle to test charm message pausing",
        forPlayers = true
)
public class DebugActionBarCommand extends MCommand<CharmModule> {
    /** The running spam task per player, so it can be toggled off with --stop */
    private static final Map<UUID, BukkitTask> RUNNING_TASKS = new ConcurrentHashMap<>();

    @Override
    protected void onRegister() {
        addArgument("frequency|--stop", CommandArguments.ANYTHING);
    }

    @Override
    protected void execute(CommandContext context) {
        Player player = context.getPlayer();
        String arg = context.next();

        if (arg.equalsIgnoreCase("--stop")) {
            BukkitTask task = RUNNING_TASKS.remove(player.getUniqueId());
            if (task != null) {
                task.cancel();
                player.sendMessage(MS.parseSingle("&cStopped spamming your action bar."));
            } else {
                player.sendMessage(MS.parseSingle("&cYou don't have an action bar spam running."));
            }
            return;
        }

        int frequency;
        try {
            frequency = Integer.parseInt(arg);
        } catch (NumberFormatException exc) {
            player.sendMessage(MS.parseSingle("&cFrequency must be a whole number of ticks, or --stop."));
            return;
        }

        if (frequency <= 0) {
            player.sendMessage(MS.parseSingle("&cFrequency must be greater than 0."));
            return;
        }

        BukkitTask existing = RUNNING_TASKS.remove(player.getUniqueId());
        if (existing != null) {
            existing.cancel();
        }

        UUID playerID = player.getUniqueId();
        BukkitTask[] taskHolder = new BukkitTask[1];
        taskHolder[0] = Bukkit.getScheduler().runTaskTimer(getModule().getPlugin(), () -> {
            if (!player.isOnline()) {
                taskHolder[0].cancel();
                RUNNING_TASKS.remove(playerID);
                return;
            }

            NMS.getTheNMS().sendActionBar(player, MS.parseSingle("&cDEBUG &7" + (System.currentTimeMillis() % 100000)));
        }, 0, frequency);
        RUNNING_TASKS.put(playerID, taskHolder[0]);

        player.sendMessage(MS.parseSingle("&aSpamming your action bar every &e" + frequency + " &atick(s). Use &e/charm debugactionbar --stop &ato stop."));
    }
}
