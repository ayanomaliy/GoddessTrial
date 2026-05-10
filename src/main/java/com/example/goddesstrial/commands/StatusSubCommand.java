package com.example.goddesstrial.commands;

import com.example.goddesstrial.trial.TrialManager;
import com.example.goddesstrial.trial.TrialPhase;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class StatusSubCommand extends AbstractPlayerCommand {

    private final TrialManager trialManager;

    public StatusSubCommand(TrialManager trialManager) {
        super("status", "Show your current trial status");
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
        TrialPhase phase = trialManager.getPhase(playerName);

        context.sendMessage(Message.raw(""));
        context.sendMessage(Message.raw("=== Trial Status ==="));
        context.sendMessage(Message.raw("Player: " + playerName));
        context.sendMessage(Message.raw("Phase: " + phase));
        context.sendMessage(Message.raw("===================="));
    }
}