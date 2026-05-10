package com.example.goddesstrial.listeners;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.trial.TrialEffects;
import com.example.goddesstrial.trial.TrialManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.logging.Level;

/**
 * Listener for normal player lifecycle events.
 *
 * Current behavior:
 * - On connect: reset trial state, restore HP cap, clear inventory for prototype cleanup
 * - On disconnect: log disconnect
 *
 * Death cleanup is handled separately by TrialDeathSystem.
 */
public class PlayerListener {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public void register(EventRegistry eventBus) {
        try {
            eventBus.register(PlayerConnectEvent.class, this::onPlayerConnect);
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered PlayerConnectEvent listener");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register PlayerConnectEvent");
        }

        try {
            eventBus.register(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered PlayerDisconnectEvent listener");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register PlayerDisconnectEvent");
        }
    }

    private void onPlayerConnect(PlayerConnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();

        String playerName = playerRef != null
                ? playerRef.getUsername()
                : "Unknown";

        String worldName = event.getWorld() != null
                ? event.getWorld().getName()
                : "unknown";

        LOGGER.at(Level.INFO).log(
                "[GoddessTrial] Player %s connected to world %s",
                playerName,
                worldName
        );

        if (playerRef == null) {
            return;
        }

        cleanupTrialOnJoin(playerRef);
    }

    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        String playerName = event.getPlayerRef() != null
                ? event.getPlayerRef().getUsername()
                : "Unknown";

        LOGGER.at(Level.INFO).log("[GoddessTrial] Player %s disconnected", playerName);
    }

    /**
     * Cleanup that always runs when a player joins.
     *
     * For the prototype, joining always cancels the trial and removes all trial effects.
     * This prevents the player from reconnecting with 1 HP or keeping the Blade of Balance.
     */
    private void cleanupTrialOnJoin(PlayerRef playerRef) {
        String playerName = playerRef.getUsername();

        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null) {
            LOGGER.at(Level.WARNING).log(
                    "[GoddessTrial] Could not cleanup on join: plugin instance is null."
            );
            return;
        }

        TrialManager.TrialResult result =
                plugin.getTrialManager().resetTrialOnJoin(playerName);

        cleanupPlayerEffects(playerRef, "join cleanup");

        LOGGER.at(Level.INFO).log(
                "[GoddessTrial] %s for %s",
                result.message(),
                playerName
        );
    }

    /**
     * Removes trial effects from the actual player entity:
     * - restores HP cap
     * - clears inventory for prototype cleanup
     *
     * Later, replace clearPlayerInventory(player) with removeBladeOfBalance(player)
     * once exact Blade removal is reliable.
     */
    private void cleanupPlayerEffects(PlayerRef playerRef, String reason) {
        String playerName = playerRef.getUsername();

        try {
            Ref<EntityStore> ref = playerRef.getReference();

            if (ref == null || !ref.isValid()) {
                LOGGER.at(Level.WARNING).log(
                        "[GoddessTrial] Could not apply %s for %s: player ref was not valid.",
                        reason,
                        playerName
                );
                return;
            }

            Store<EntityStore> store = ref.getStore();
            Player player = store.getComponent(ref, Player.getComponentType());

            if (player == null) {
                LOGGER.at(Level.WARNING).log(
                        "[GoddessTrial] Could not apply %s for %s: Player component was null.",
                        reason,
                        playerName
                );
                return;
            }

            TrialEffects.restorePlayerHealthCap(store, ref);

            // Prototype-safe cleanup:
            // This guarantees the Blade of Balance is gone after reconnect.
            TrialEffects.clearPlayerInventory(player);

            LOGGER.at(Level.INFO).log(
                    "[GoddessTrial] Restored HP and cleared inventory for %s (%s).",
                    playerName,
                    reason
            );
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log(
                    "[GoddessTrial] Failed to clean up trial effects for %s (%s).",
                    playerName,
                    reason
            );
        }
    }
}