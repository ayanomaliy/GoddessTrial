package com.example.goddesstrial;

import com.example.goddesstrial.listeners.*;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;

import com.example.goddesstrial.commands.GoddessTrialPluginCommand;

import javax.annotation.Nonnull;
import java.util.logging.Level;

import com.example.goddesstrial.trial.TrialManager;

import com.example.goddesstrial.interactions.GoddessStatueTrialInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;

/**
 * GoddessTrial - A Hytale server plugin.
 */
public class GoddessTrialPlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static GoddessTrialPlugin instance;

    private final TrialManager trialManager = new TrialManager();

    public GoddessTrialPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public TrialManager getTrialManager() {
        return trialManager;
    }

    public static GoddessTrialPlugin getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        LOGGER.at(Level.INFO).log("[GoddessTrial] Setting up...");

        registerInteractions();
        registerCommands();
        registerListeners();
        registerSystems();

        LOGGER.at(Level.INFO).log("[GoddessTrial] Setup complete!");
    }

    private void registerInteractions() {
        try {
            getCodecRegistry(Interaction.CODEC).register(
                    "GoddessTrial_Statue",
                    GoddessStatueTrialInteraction.class,
                    GoddessStatueTrialInteraction.CODEC
            );

            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered GoddessTrial_Statue interaction");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING)
                    .withCause(e)
                    .log("[GoddessTrial] Failed to register GoddessTrial_Statue interaction");
        }
    }

    private void registerCommands() {
        try {
            getCommandRegistry().registerCommand(new GoddessTrialPluginCommand(trialManager));
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered /trial command");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register commands");
        }
    }

    private void registerListeners() {
        EventRegistry eventBus = getEventRegistry();

        try {
            new PlayerListener().register(eventBus);
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered player event listeners");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register listeners");
        }

        try {
            new BlockInspectListener().register(eventBus);
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered block inspect listener");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register block inspect listener");
        }
    }

    @Override
    protected void start() {
        LOGGER.at(Level.INFO).log("[GoddessTrial] Started!");
        LOGGER.at(Level.INFO).log("[GoddessTrial] Use /trial help for commands");
    }

    @Override
    protected void shutdown() {
        LOGGER.at(Level.INFO).log("[GoddessTrial] Shutting down...");

        try {
            trialManager.failAllOngoingTrialsBecauseServerIsStopping();
        } catch (Exception e) {
            LOGGER.at(Level.WARNING)
                    .withCause(e)
                    .log("[GoddessTrial] Failed to mark ongoing trials as failed during shutdown.");
        }

        instance = null;
    }

    private void registerSystems() {
        try {
            getEntityStoreRegistry().registerSystem(new TrialDeathSystem());
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered TrialDeathSystem");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register TrialDeathSystem");
        }

        try {
            getEntityStoreRegistry().registerSystem(new TrialJoinCleanupSystem());
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered TrialJoinCleanupSystem");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register TrialJoinCleanupSystem");
        }

        try {
            getEntityStoreRegistry().registerSystem(new BladeBalanceHealthSystem());
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered BladeBalanceHealthSystem");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register BladeBalanceHealthSystem");
        }

        try {
            getEntityStoreRegistry().registerSystem(new DamageSystem());
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered DamageSystem");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register DamageSystem");
        }

        try {
            getEntityStoreRegistry().registerSystem(new BladeRechargeNotificationSystem());
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered BladeRechargeNotificationSystem");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register BladeRechargeNotificationSystem");
        }

        try {
            getEntityStoreRegistry().registerSystem(new GoddessDialogueSequenceSystem());
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered GoddessDialogueSequenceSystem");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register GoddessDialogueSequenceSystem");
        }

        try {
            getEntityStoreRegistry().registerSystem(new TrialMonsterCleanupSystem());
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered TrialMonsterCleanupSystem");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register TrialMonsterCleanupSystem");
        }

//        try {
//            getEntityStoreRegistry().registerSystem(new TrialReinforcementSpawnSystem());
//            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered TrialReinforcementSpawnSystem");
//        } catch (Exception e) {
//            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register TrialReinforcementSpawnSystem");
//        }

        /*
         * Disabled for now.
         *
         * TrialReinforcementSpawnSystem calls NPCPlugin.spawnNPC from inside an
         * EntityTickingSystem tick. Hytale rejects that with:
         *
         * "Store is currently processing! Ensure you aren't calling a store method from a system."
         *
         * Initial monster spawning still works because TrialStarter calls it from the
         * player interaction/command flow after the flower has spawned.
         */
        LOGGER.at(Level.INFO).log("[GoddessTrial] TrialReinforcementSpawnSystem disabled for now");


        try {
            getEntityStoreRegistry().registerSystem(new GoddessCompletionSequenceSystem());
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered GoddessCompletionSequenceSystem");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register GoddessCompletionSequenceSystem");
        }
    }
}