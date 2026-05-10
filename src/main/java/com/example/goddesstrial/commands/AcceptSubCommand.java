package com.example.goddesstrial.commands;

import com.example.goddesstrial.trial.TrialManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class AcceptSubCommand extends AbstractPlayerCommand {

    private final TrialManager trialManager;

    public AcceptSubCommand(TrialManager trialManager) {
        super("accept", "Accept the Trial of the Goddess");
        this.trialManager = trialManager;
        this.setPermissionGroup(null);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        String playerName = playerRef.getUsername();

        TrialManager.TrialResult result = trialManager.acceptTrial(playerName);

        context.sendMessage(Message.raw(""));
        context.sendMessage(Message.raw("=== Trial of the Goddess ==="));
        context.sendMessage(Message.raw(result.message()));

        if (result.success()) {
            context.sendMessage(Message.raw("Prototype next step: give Blade of Balance, lock HP to 1, spawn flower."));
        }

        context.sendMessage(Message.raw("==========================="));
    }
}