package com.golfing8.kcharm.module.task;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.golfing8.kcharm.module.CharmModule;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.module.ModuleTask;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Helps messages be delivered in a pretty way to players holding multiple charm effects.
 * <p>
 * PacketEvents fires {@link #onPacketSend(PacketSendEvent)} on the Netty I/O thread while
 * {@link #run()} and {@link #queue(Message, Player)} run on the main thread, so every read/write
 * of a {@link MessageHolder}'s fields (and the backing map) is synchronized to avoid the foreign-packet
 * detection racing/going stale across threads.
 */
public class MessageTask extends ModuleTask<CharmModule> implements PacketListener {
    private final Map<Player, MessageHolder> messageHolderMap = new WeakHashMap<>();

    public MessageTask(CharmModule module) {
        super(module);
    }

    @Override
    protected void run() {
        long currentTick = NMS.getTheNMS().getCurrentTick();
        List<Map.Entry<Player, MessageHolder>> entries;
        synchronized (messageHolderMap) {
            entries = new ArrayList<>(messageHolderMap.entrySet());
        }

        for (var entry : entries) {
            Player player = entry.getKey();
            MessageHolder messageHolder = entry.getValue();
            Message toSend = null;
            synchronized (messageHolder) {
                if (messageHolder.message == null || messageHolder.message.isEmpty())
                    continue;

                if (currentTick - messageHolder.lastForeignSentTick >= getModule().getActionBarForeignCooldown()) {
                    messageHolder.pendingRealSend = true;
                    toSend = messageHolder.message;
                }
                messageHolder.message = null;
                messageHolder.lastSentTick = currentTick;
            }

            // Send outside the lock so we never hold it during packet dispatch.
            if (toSend != null) {
                toSend.send(player);
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.ACTION_BAR)
            return;

        Player player = event.getPlayer();
        long currentTick = NMS.getTheNMS().getCurrentTick();
        MessageHolder messageHolder;
        synchronized (messageHolderMap) {
            messageHolder = messageHolderMap.computeIfAbsent(player, k -> new MessageHolder());
        }

        synchronized (messageHolder) {
            if (messageHolder.pendingRealSend) {
                messageHolder.pendingRealSend = false;
                return;
            }
            messageHolder.lastForeignSentTick = currentTick;
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

        MessageHolder messageHolder;
        synchronized (messageHolderMap) {
            messageHolder = messageHolderMap.computeIfAbsent(player, k -> new MessageHolder());
        }

        synchronized (messageHolder) {
            if (messageHolder.message != null) {
                messageHolder.message = messageHolder.message.append(message, " &f&l|&r ");
            } else {
                messageHolder.message = message;
            }
        }
    }

    static class MessageHolder {
        public @Nullable Message message;
        public long lastSentTick = 0;
        public long lastForeignSentTick = 0;
        public boolean pendingRealSend = false;
    }
}
