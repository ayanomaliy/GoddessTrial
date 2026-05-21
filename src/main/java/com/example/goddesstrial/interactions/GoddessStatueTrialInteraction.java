package com.example.goddesstrial.interactions;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.listeners.GoddessDialogueSequenceSystem;
import com.example.goddesstrial.trial.TrialManager;
import com.example.goddesstrial.trial.TrialPhase;
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
import com.example.goddesstrial.trial.TrialCompletion;
import com.example.goddesstrial.trial.TrialInventoryUtil;
import com.example.goddesstrial.listeners.GoddessChoiceSequenceSystem;

import javax.annotation.Nonnull;

/**
 * Custom placed-block interaction for the Statue of a Slumbering Deity.
 *
 * Triggered from the statue asset JSON through:
 *
 * "Type": "GoddessTrial_Statue"
 *
 * Behaviour:
 * - player presses F on the statue
 * - trial offer state is created
 * - the cinematic goddess dialogue sequence starts
 * - after the dialogue sequence, the Accept / Refuse UI opens
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

        if (trialManager.isMonsterCleanupPending(playerName)) {
            player.sendMessage(Message.raw("The statue remains cold and silent beneath your hands."));
            return;
        }

        if (GoddessDialogueSequenceSystem.isDialoguePlaying(playerName)) {
            GoddessDialogueSequenceSystem.skipDialogueToOfferUi(
                    playerName,
                    playerRef,
                    player,
                    playerEntityRef,
                    store
            );
            return;
        }

        if (trialManager.getPhase(playerName) == TrialPhase.COMPLETED) {
            player.sendMessage(Message.raw(""));
            player.sendMessage(Message.raw("The statue is cold and silent"));
            player.sendMessage(Message.raw("Only the Blade of Balance remains as proof of her gratitude."));
            return;
        }

        if (trialManager.getPhase(playerName) == TrialPhase.ACTIVE) {
            if (TrialInventoryUtil.hasSacredFlower(player, store, playerEntityRef)) {
                TrialCompletion.completeTrial(
                        trialManager,
                        playerName,
                        player,
                        playerEntityRef,
                        store
                );
                return;
            }

            GoddessChoiceSequenceSystem.startReturnWithoutFlowerDialogue(playerName);
            return;
        }

        TrialManager.TrialResult offerResult = trialManager.offerTrial(playerName);

        /*
         * If the player already has an offered trial, we still allow the dialogue
         * to restart. This is nicer than blocking them with command-like text.
         */
        if (!offerResult.success()
                && trialManager.getPhase(playerName) != TrialPhase.OFFERED) {
            player.sendMessage(Message.raw(offerResult.message()));
            return;
        }

        player.sendMessage(Message.raw(""));
        player.sendMessage(Message.raw("=== Statue of a Slumbering Deity ==="));

        GoddessDialogueSequenceSystem.startDialogue(playerName);
    }
}