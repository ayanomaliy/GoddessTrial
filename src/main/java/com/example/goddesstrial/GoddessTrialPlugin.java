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

    //Trial fields:
    private final TrialManager trialManager = new TrialManager();

    public GoddessTrialPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }


    public TrialManager getTrialManager() {
        return trialManager;
    }

    /**
     * Get the plugin instance.
     * @return The plugin instance
     */
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

    /**
     * Register custom GoddessTrial interaction types.
     */
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

    /**
     * Register plugin commands.
     */
    private void registerCommands() {
        try {
            getCommandRegistry().registerCommand(new GoddessTrialPluginCommand(trialManager));
            LOGGER.at(Level.INFO).log("[GoddessTrial] Registered /trial command");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[GoddessTrial] Failed to register commands");
        }
    }

    /**
     * Register event listeners.
     */
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
    }
}