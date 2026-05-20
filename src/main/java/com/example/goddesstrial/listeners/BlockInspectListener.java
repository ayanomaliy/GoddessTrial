package com.example.goddesstrial.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.logging.Level;

/**
 * Temporary debug listener.
 *
 * Prints the block id of every block that enters the UseBlockEvent.Pre
 * interaction chain.
 *
 * This is only for testing whether the statue now triggers block-use events
 * after adding the Interactions section to its asset JSON.
 */
public class BlockInspectListener {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    /**
     * Registers the block inspect listener on the Hytale event bus.
     *
     * @param eventBus the plugin event registry
     */
    public void register(EventRegistry eventBus) {
        try {
            eventBus.register(UseBlockEvent.Pre.class, this::onUseBlockPre);
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered BlockInspectListener");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING)
                    .withCause(e)
                    .log("[GoddessTrial] Failed to register BlockInspectListener");
        }
    }

    private void onUseBlockPre(UseBlockEvent.Pre event) {
        String blockId = event.getBlockType().getId();
        Vector3i position = event.getTargetBlock();

        String message = "[GoddessTrial] UseBlockEvent.Pre: "
                + blockId
                + " at "
                + position;

        System.out.println(message);

        InteractionContext context = event.getContext();

        if (context == null) {
            return;
        }

        Ref<EntityStore> playerRef = context.getEntity();

        if (playerRef == null || !playerRef.isValid()) {
            return;
        }

        Store<EntityStore> store = playerRef.getStore();
        Player player = store.getComponent(playerRef, Player.getComponentType());

        if (player != null) {
            player.sendMessage(Message.raw(message));
        }
    }
}