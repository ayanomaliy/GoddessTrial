package com.example.goddesstrial.trial;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Starts the actual Trial of the Goddess gameplay.
 *
 * This class is intentionally independent from commands.
 * It can be called from:
 * - /trial accept for testing
 * - statue interaction later
 * - dialogue choice later
 * - item interaction later
 */
public final class TrialStarter {

    private TrialStarter() {
        // Utility class
    }

    public static TrialStartResult startTrial(
            TrialManager trialManager,
            String playerName,
            Player player,
            Ref<EntityStore> playerRef,
            Store<EntityStore> store
    ) {
        TrialManager.TrialResult result = trialManager.acceptTrial(playerName);

        if (!result.success()) {
            return new TrialStartResult(false, result.message());
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