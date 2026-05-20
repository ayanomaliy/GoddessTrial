package com.example.goddesstrial.commands;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.trial.TrialManager;
import com.example.goddesstrial.trial.TrialPhase;
import com.example.goddesstrial.ui.TrialOfferPage;
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

/**
 * Internal statue trigger command.
 *
 * This command is meant to be called by a Hytale interaction asset,
 * not typed manually by the player.
 */
public class StatueSubCommand extends AbstractPlayerCommand {

    private final TrialManager trialManager;

    public StatueSubCommand(TrialManager trialManager) {
        super("statue", "Internal statue interaction trigger");
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
            context.sendMessage(Message.raw("The statue remains silent."));
            return;
        }

        String playerName = playerRef.getUsername();

        if (trialManager.getPhase(playerName) == TrialPhase.ACTIVE) {
            context.sendMessage(Message.raw("The statue is silent. You are already inside the Goddess' trial."));
            return;
        }

        TrialManager.TrialResult offerResult = trialManager.offerTrial(playerName);

        if (!offerResult.success() && trialManager.getPhase(playerName) != TrialPhase.OFFERED) {
            context.sendMessage(Message.raw(offerResult.message()));
            return;
        }

        context.sendMessage(Message.raw(""));
        context.sendMessage(Message.raw("=== Statue of a Slumbering Deity ==="));
        context.sendMessage(Message.raw("The stone is cold beneath your hand."));
        context.sendMessage(Message.raw("A voice, ancient and tired, whispers:"));
        context.sendMessage(Message.raw("\"Balance is not mercy. Balance is sacrifice.\""));
        context.sendMessage(Message.raw("\"Will you take up my blade and walk the trial?\""));

        player.getPageManager().openCustomPage(
                ref,
                store,
                new TrialOfferPage(playerRef)
        );
    }
}