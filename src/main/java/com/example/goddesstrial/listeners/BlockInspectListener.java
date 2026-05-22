package com.example.goddesstrial.listeners;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.trial.TrialFlowerConstants;
import com.example.goddesstrial.trial.TrialFlowerSpawner;
import com.example.goddesstrial.trial.TrialPhase;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * Handles Sacred Flower collection and still prints debug block-use info.
 */
public class BlockInspectListener {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

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

        System.out.println("[GoddessTrial] UseBlockEvent.Pre: " + blockId + " at " + position);

        InteractionContext context = event.getContext();

        if (context == null) {
            return;
        }

        Ref<EntityStore> playerEntityRef = context.getEntity();

        if (playerEntityRef == null || !playerEntityRef.isValid()) {
            return;
        }

        Store<EntityStore> store = playerEntityRef.getStore();

        Player player = store.getComponent(playerEntityRef, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(playerEntityRef, PlayerRef.getComponentType());

        if (player == null || playerRef == null) {
            return;
        }

        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null) {
            return;
        }

        String playerName = playerRef.getUsername();

        if (plugin.getTrialManager().getPhase(playerName) != TrialPhase.ACTIVE) {
            return;
        }

        if (!TrialFlowerSpawner.FLOWER_BLOCK_ID.equals(blockId)) {
            return;
        }

        Vector3d sacredFlowerPosition =
                plugin.getTrialManager().getSacredFlowerPosition(playerName);

        if (sacredFlowerPosition == null) {
            return;
        }

        if (!isSameBlock(position, sacredFlowerPosition)) {
            player.sendMessage(Message.raw("This is only an ordinary flower."));
            return;
        }

        World world = store.getExternalData().getWorld();

        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();

        /*
         * Important:
         * Do not give the player the Sacred Flower unless the world block was
         * actually removed.
         *
         * Previously, removeFlowerBlock(...) could fail because "Air" is not a
         * valid block key, but the player still received the quest item and the
         * TrialManager forgot the flower position. That created stale world flowers.
         */
        boolean removed = TrialFlowerSpawner.removeFlowerBlock(world, x, y, z);

        if (!removed) {
            tryCancel(event);

            player.sendMessage(Message.raw(""));
            player.sendMessage(Message.raw("The Sacred Flower resists the veil."));
            player.sendMessage(Message.raw("Its roots remain bound to this place."));

            System.out.println(
                    "[GoddessTrial] "
                            + playerName
                            + " tried to collect the Sacred Flower at "
                            + x + ", " + y + ", " + z
                            + ", but the block could not be removed."
            );

            return;
        }

        ItemStack sacredFlower = new ItemStack(
                TrialFlowerConstants.SACRED_FLOWER_ITEM_ID,
                1
        );

        player.giveItem(sacredFlower, playerEntityRef, store);

        plugin.getTrialManager().clearSacredFlowerPosition(playerName);

        tryCancel(event);

        player.sendMessage(Message.raw(""));
        player.sendMessage(Message.raw("You pick the Sacred Flower."));
        player.sendMessage(Message.raw("It is warm, though no sunlight touches it."));

        System.out.println(
                "[GoddessTrial] "
                        + playerName
                        + " collected the Sacred Flower at "
                        + x + ", " + y + ", " + z
        );
    }

    private static boolean isSameBlock(Vector3i clickedPosition, Vector3d storedPosition) {
        int clickedX = clickedPosition.getX();
        int clickedY = clickedPosition.getY();
        int clickedZ = clickedPosition.getZ();

        int storedX = (int) Math.floor(storedPosition.getX());
        int storedY = (int) Math.floor(storedPosition.getY());
        int storedZ = (int) Math.floor(storedPosition.getZ());

        return clickedX == storedX
                && clickedY == storedY
                && clickedZ == storedZ;
    }

    private static void tryCancel(Object event) {
        try {
            Method method = event.getClass().getMethod("setCancelled", boolean.class);
            method.invoke(event, true);
        } catch (Exception ignored) {
            // Some Hytale event variants may not expose setCancelled.
        }
    }
}