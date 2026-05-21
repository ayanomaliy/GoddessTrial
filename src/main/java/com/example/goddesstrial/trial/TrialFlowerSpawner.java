package com.example.goddesstrial.trial;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Spawns the Sacred Flower objective for the trial.
 *
 * TEMPORARY VERSION:
 * Gives the Sacred Flower directly to the player.
 *
 * Next step:
 * Replace this with actual block/item world spawning once we inspect the
 * Hytale block placement API.
 */
public final class TrialFlowerSpawner {

    private TrialFlowerSpawner() {
    }

    public static void spawnSacredFlower(
            String playerName,
            Store<EntityStore> store,
            Ref<EntityStore> playerRef
    ) {
        Player player = store.getComponent(playerRef, Player.getComponentType());

        if (player == null) {
            System.out.println("[GoddessTrial] Could not give Sacred Flower: player component was null.");
            return;
        }

        ItemStack flower = new ItemStack(TrialFlowerConstants.SACRED_FLOWER_ITEM_ID, 1);
        player.giveItem(flower, playerRef, store);

        player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "A Sacred Flower has appeared in your inventory for testing."
        ));

        System.out.println(
                "[GoddessTrial] TEMP: Gave Sacred Flower to "
                        + playerName
                        + " using item id "
                        + TrialFlowerConstants.SACRED_FLOWER_ITEM_ID
        );
    }
}
