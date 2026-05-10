package com.example.goddesstrial.commands;

import com.example.goddesstrial.trial.TrialManager;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

/**
 * Main command for GoddessTrial plugin.
 *
 * Usage:
 * - /trial help
 * - /trial info
 * - /trial reload
 * - /trial ui
 * - /trial start
 * - /trial accept
 * - /trial refuse
 * - /trial status
 */
public class GoddessTrialPluginCommand extends AbstractCommandCollection {

    public GoddessTrialPluginCommand(TrialManager trialManager) {
        super("trial", "GoddessTrial plugin commands");

        this.addSubCommand(new HelpSubCommand());
        this.addSubCommand(new InfoSubCommand());
        this.addSubCommand(new ReloadSubCommand());
        this.addSubCommand(new UISubCommand());

        this.addSubCommand(new StartSubCommand(trialManager));
        this.addSubCommand(new AcceptSubCommand(trialManager));
        this.addSubCommand(new RefuseSubCommand(trialManager));
        this.addSubCommand(new StatusSubCommand(trialManager));
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }
}