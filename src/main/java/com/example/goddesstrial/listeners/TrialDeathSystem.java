package com.example.goddesstrial.listeners;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.trial.TrialFlowerSpawner;
import com.example.goddesstrial.trial.TrialInventoryUtil;
import com.example.goddesstrial.trial.TrialManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.logging.Level;

/**
 * Handles trial cleanup when the player dies.
 *
 * Important:
 * Do not force HP restoration here. Hytale's own death/respawn system should
 * handle death state. We only remove trial items and schedule monster cleanup.
 */
public class TrialDeathSystem extends DeathSystems.OnDeathSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType());
    }

    @Override
    public void onComponentAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull DeathComponent component,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (player == null || playerRef == null) {
            return;
        }

        String playerName = playerRef.getUsername();

        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null) {
            LOGGER.at(Level.WARNING).log("[GoddessTrial] Could not handle death: plugin instance is null.");
            return;
        }

        TrialManager.TrialResult result =
                plugin.getTrialManager().failTrialBecauseOfDeath(playerName);

        if (!result.success()) {
            LOGGER.at(Level.INFO).log(
                    "[GoddessTrial] Death ignored for %s: %s",
                    playerName,
                    result.message()
            );
            return;
        }

        try {
            TrialFlowerSpawner.removeStoredSacredFlower(playerName, store);
            TrialFlowerSpawner.removeAllConfiguredSacredFlowers(store);

            TrialMonsterCleanupSystem.requestCleanup(playerName);

            List<Ref<EntityStore>> spawnedMonsters =
                    plugin.getTrialManager().consumeSpawnedTrialMonsters(playerName);

            int trackedCount = spawnedMonsters.size();
            int scheduledRemovalCount = 0;
            int invalidCount = 0;

            for (Ref<EntityStore> monsterRef : spawnedMonsters) {
                if (monsterRef == null || !monsterRef.isValid()) {
                    invalidCount++;
                    continue;
                }

                commandBuffer.tryRemoveEntity(monsterRef, RemoveReason.REMOVE);
                scheduledRemovalCount++;
            }

            TrialInventoryUtil.removeBladeOfBalance(player, store, ref);
            TrialInventoryUtil.removeSacredFlower(player, store, ref);

            DamageSystem.clearBladeEnergy(playerName);

            LOGGER.at(Level.INFO).log(
                    "[GoddessTrial] Player %s died. Trial failed. Tracked monsters: %s, scheduled removals: %s, invalid refs: %s.",
                    playerName,
                    trackedCount,
                    scheduledRemovalCount,
                    invalidCount
            );

            LOGGER.at(Level.INFO).log(
                    "[GoddessTrial] Trial items removed and Blade energy cleared for %s.",
                    playerName
            );
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log(
                    "[GoddessTrial] Failed to clean up death trial state for %s",
                    playerName
            );
        }
    }
}