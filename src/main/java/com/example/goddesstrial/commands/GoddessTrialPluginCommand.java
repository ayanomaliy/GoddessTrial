package com.example.goddesstrial.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

/**
 * Main command for GoddessTrial plugin.
 *
 * Usage:
 * - /trial help - Show available commands
 * - /trial info - Show plugin information
 * - /trial reload - Reload plugin configuration
 * - /trial ui - Open the plugin dashboard
 */
public class GoddessTrialPluginCommand extends AbstractCommandCollection {

    public GoddessTrialPluginCommand() {
        super("trial", "GoddessTrial plugin commands");

        // Add subcommands
        this.addSubCommand(new HelpSubCommand());
        this.addSubCommand(new InfoSubCommand());
        this.addSubCommand(new ReloadSubCommand());
        this.addSubCommand(new UISubCommand());
    }

    @Override
    protected boolean canGeneratePermission() {
        return false; // No permission required for base command
    }
}