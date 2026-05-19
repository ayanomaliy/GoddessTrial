package com.example.goddesstrial;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;

import com.example.goddesstrial.commands.GoddessTrialPluginCommand;
import com.example.goddesstrial.listeners.PlayerListener;

import javax.annotation.Nonnull;
import java.util.logging.Level;

import com.example.goddesstrial.trial.TrialManager;
import com.example.goddesstrial.listeners.TrialDeathSystem;

import com.example.goddesstrial.listeners.BladeBalanceHealthSystem;

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

        registerCommands();
        registerListeners();
        registerSystems();

        LOGGER.at(Level.INFO).log("[GoddessTrial] Setup complete!");
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
    }
}