package com.example.goddesstrial.listeners;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.trial.TrialEffects;
import com.example.goddesstrial.trial.TrialManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.logging.Level;

/**
 * Proper death listener for the Trial of the Goddess.
 *
 * Hytale does not expose this as a simple PlayerDeathEvent.
 * Death is represented by DeathComponent being added to an entity.
 */
public class TrialDeathSystem extends DeathSystems.OnDeathSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        // Only run this death system for player entities.
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
            // Remove trial effects.
            // For now, use full inventory clear because exact Blade removal is unreliable.
            TrialEffects.clearPlayerInventory(player);
            TrialEffects.restorePlayerHealthCap(store, ref);

            LOGGER.at(Level.INFO).log(
                    "[GoddessTrial] Player %s died. Trial reset, inventory cleared, HP restored.",
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