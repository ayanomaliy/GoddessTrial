package com.example.goddesstrial.interactions;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.trial.TrialManager;
import com.example.goddesstrial.trial.TrialPhase;
import com.example.goddesstrial.ui.TrialOfferPage;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Custom placed-block interaction for the Statue of a Slumbering Deity.
 *
 * This is triggered from the statue asset JSON through:
 *
 * "Type": "GoddessTrial_Statue"
 */
public class GoddessStatueTrialInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<GoddessStatueTrialInteraction> CODEC =
            BuilderCodec.builder(
                    GoddessStatueTrialInteraction.class,
                    GoddessStatueTrialInteraction::new,
                    SimpleInstantInteraction.CODEC
            ).build();

    @Override
    protected void firstRun(
            @Nonnull InteractionType interactionType,
            @Nonnull InteractionContext interactionContext,
            @Nonnull CooldownHandler cooldownHandler
    ) {
        Ref<EntityStore> playerEntityRef = interactionContext.getEntity();

        if (playerEntityRef == null || !playerEntityRef.isValid()) {
            return;
        }

        Store<EntityStore> store = playerEntityRef.getStore();

        Player player = store.getComponent(
                playerEntityRef,
                Player.getComponentType()
        );

        PlayerRef playerRef = store.getComponent(
                playerEntityRef,
                PlayerRef.getComponentType()
        );

        if (player == null || playerRef == null) {
            return;
        }

        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null) {
            player.sendMessage(Message.raw("The statue remains silent."));
            return;
        }

        String playerName = playerRef.getUsername();
        TrialManager trialManager = plugin.getTrialManager();

        if (trialManager.getPhase(playerName) == TrialPhase.ACTIVE) {
            player.sendMessage(Message.raw(
                    "The statue is silent. You are already inside the Goddess' trial."
            ));
            return;
        }

        TrialManager.TrialResult offerResult = trialManager.offerTrial(playerName);

        if (!offerResult.success()
                && trialManager.getPhase(playerName) != TrialPhase.OFFERED) {
            player.sendMessage(Message.raw(offerResult.message()));
            return;
        }

        player.sendMessage(Message.raw(""));
        player.sendMessage(Message.raw("=== Statue of a Slumbering Deity ==="));
        player.sendMessage(Message.raw("The stone is cold beneath your hand."));
        player.sendMessage(Message.raw("A voice, ancient and tired, whispers:"));
        player.sendMessage(Message.raw("\"Balance is not mercy. Balance is sacrifice.\""));
        player.sendMessage(Message.raw("\"Will you take up my blade and walk the trial?\""));

        player.getPageManager().openCustomPage(
                playerEntityRef,
                store,
                new TrialOfferPage(playerRef)
        );
    }
}