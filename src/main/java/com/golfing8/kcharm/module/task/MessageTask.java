package com.golfing8.kcharm.module.task;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerActionBar;
import com.golfing8.kcharm.module.CharmModule;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.module.ModuleTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.WeakHashMap;

/**
 * Helps messages be delivered in a pretty way to players holding multiple charm effects
 */
public class MessageTask extends ModuleTask<CharmModule> implements PacketListener {
    private final WeakHashMap<Player, MessageHolder> messageHolderMap = new WeakHashMap<>();

    public MessageTask(CharmModule module) {
        super(module);
    }

    @Override
    protected void run() {
        long currentTick = NMS.getTheNMS().getCurrentTick();
        for (var entry : messageHolderMap.entrySet()) {
            Player player = entry.getKey();
            MessageHolder messageHolder = entry.getValue();
            if (messageHolder.message == null || messageHolder.message.isEmpty())
                continue;

            messageHolder.pendingRealSend = true;
            if (currentTick - messageHolder.lastForeignSentTick >= getModule().getActionBarForeignCooldown()) {
                messageHolder.message.send(player);
            }
            messageHolder.message = null;
            messageHolder.lastSentTick = NMS.getTheNMS().getCurrentTick();
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.ACTION_BAR)
            return;

        long currentTick = NMS.getTheNMS().getCurrentTick();
        MessageHolder messageHolder = messageHolderMap.computeIfAbsent(event.getPlayer(), k -> new MessageHolder());
        if (messageHolder.pendingRealSend) {
            messageHolder.pendingRealSend = false;
            return;
        }
        messageHolder.lastForeignSentTick = currentTick;
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
        public long lastForeignSentTick = 0;
        public boolean pendingRealSend = false;
    }
}
