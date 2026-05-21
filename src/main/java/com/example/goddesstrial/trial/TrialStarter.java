package com.example.goddesstrial.trial;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Starts the actual Trial of the Goddess gameplay.
 */
public final class TrialStarter {

    private TrialStarter() {
    }

    public static TrialStartResult startTrial(
            TrialManager trialManager,
            String playerName,
            Player player,
            Ref<EntityStore> playerRef,
            Store<EntityStore> store
    ) {
        if (trialManager.isMonsterCleanupPending(playerName)) {
            return new TrialStartResult(
                    false,
                    "The remains of your previous trial are still fading. Wait a moment, then try again."
            );
        }

        TrialManager.TrialResult result = trialManager.acceptTrial(playerName);

        if (!result.success()) {
            return new TrialStartResult(false, result.message());
        }

        TrialInventoryUtil.removeBladeOfBalance(player, store, playerRef);
        TrialInventoryUtil.removeSacredFlower(player, store, playerRef);

        boolean flowerSpawned = TrialFlowerSpawner.spawnSacredFlower(
                playerName,
                store,
                playerRef
        );

        if (!flowerSpawned) {
            trialManager.resetTrial(playerName);

            return new TrialStartResult(
                    false,
                    "The trial could not begin because the Sacred Flower could not bloom."
            );
        }

        TrialEffects.grantBladeOfBalance(player, playerRef, store);
        GoddessSoundUtil.playBladeReceived(playerRef, store);

        TrialEffects.reducePlayerToOneHp(store, playerRef);
        TrialMonsterSpawner.spawnTrialWave(playerName, store, playerRef);

        return new TrialStartResult(
                true,
                "You accepted the Trial of the Goddess. The Blade of Balance has been granted."
        );
    }

    public record TrialStartResult(boolean success, String message) {}
}