package com.example.goddesstrial.listeners;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.logging.Level;

/**
 * Listener for normal player lifecycle events.
 *
 * With the simplified restart behavior:
 * - joining always schedules delayed validation
 * - if shutdown/death cleanup is pending, monster cleanup is scheduled
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

        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null) {
            LOGGER.at(Level.WARNING).log(
                    "[GoddessTrial] Could not validate join for %s: plugin instance is null.",
                    playerName
            );
            return;
        }

        if (plugin.getTrialManager().isMonsterCleanupPending(playerName)) {
            TrialMonsterCleanupSystem.requestCleanup(playerName);

            LOGGER.at(Level.INFO).log(
                    "[GoddessTrial] Scheduled pending persistent monster cleanup for %s.",
                    playerName
            );
        }

        TrialJoinCleanupSystem.requestCleanup(playerName);

        LOGGER.at(Level.INFO).log(
                "[GoddessTrial] Scheduled delayed trial validation for %s.",
                playerName
        );
    }

    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        String playerName = event.getPlayerRef() != null
                ? event.getPlayerRef().getUsername()
                : "Unknown";

        LOGGER.at(Level.INFO).log("[GoddessTrial] Player %s disconnected", playerName);
    }
}