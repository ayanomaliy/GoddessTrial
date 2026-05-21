package com.example.goddesstrial.commands;

import com.example.goddesstrial.trial.TrialManager;
import com.example.goddesstrial.trial.TrialStarter;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
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
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) {
            context.sendMessage(Message.raw("Error: Could not access player component."));
            return;
        }

        String playerName = playerRef.getUsername();

        TrialStarter.TrialStartResult result =
                TrialStarter.startTrial(
                        trialManager,
                        playerName,
                        player,
                        ref,
                        store
                );

        context.sendMessage(Message.raw(""));
        context.sendMessage(Message.raw("=== Trial of the Goddess ==="));
        context.sendMessage(Message.raw(result.message()));

        if (result.success()) {
            context.sendMessage(Message.raw("\"Then take my blade. Cross the veil, and bring back what was once mine.\""));        }

        context.sendMessage(Message.raw("==========================="));
    }
}