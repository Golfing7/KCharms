package com.golfing8.kcharm.module.task;

import com.golfing8.kcharm.module.CharmModule;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.module.ModuleTask;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.WeakHashMap;

/**
 * Helps messages be delivered in a pretty way to players holding multiple charm effects
 */
public class MessageTask extends ModuleTask<CharmModule> {
    private final WeakHashMap<Player, MessageHolder> messageHolderMap = new WeakHashMap<>();

    public MessageTask(CharmModule module) {
        super(module);
    }

    @Override
    protected void run() {
        for (var entry : messageHolderMap.entrySet()) {
            Player player = entry.getKey();
            MessageHolder messageHolder = entry.getValue();
            if (messageHolder.message == null || messageHolder.message.isEmpty())
                continue;

            messageHolder.message.send(player);
            messageHolder.message = null;
            messageHolder.lastSentTick = NMS.getTheNMS().getCurrentTick();
        }
    }

    /**
     * Queues the message type
     *
     * @param message the message
     * @param player the player
     */
    public void queue(Message message, Player player) {
        if (message.isEmpty())
            return;

        MessageHolder messageHolder = messageHolderMap.computeIfAbsent(player, k -> new MessageHolder());
        if (messageHolder.message != null) {
            messageHolder.message = messageHolder.message.append(message, " &f&l|&r ");
        } else {
            messageHolder.message = message;
        }
    }

    static class MessageHolder {
        public @Nullable Message message;
        public long lastSentTick = 0;
    }
}
