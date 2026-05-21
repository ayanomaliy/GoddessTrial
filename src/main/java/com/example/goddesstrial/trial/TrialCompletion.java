package com.example.goddesstrial.trial;

import com.example.goddesstrial.listeners.DamageSystem;
import com.example.goddesstrial.listeners.GoddessCompletionSequenceSystem;
import com.example.goddesstrial.listeners.TrialMonsterCleanupSystem;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Handles successful completion of the Trial of the Goddess.
 */
public final class TrialCompletion {

    private TrialCompletion() {
    }

    public static void completeTrial(
            TrialManager trialManager,
            String playerName,
            Player player,
            Ref<EntityStore> playerRef,
            Store<EntityStore> store
    ) {
        /*
         * The Sacred Flower belongs to the Goddess and disappears when the trial is
         * completed.
         */
        TrialInventoryUtil.removeSacredFlower(player, store, playerRef);

        /*
         * Important:
         * Do NOT remove the Blade of Balance here.
         *
         * After the Goddess becomes whole, she no longer needs the blade and leaves
         * it behind as the player's reward.
         */

        /*
         * Keep the Blade energy clean after the trial ends.
         * The post-trial Blade may still keep its HP curse, but the trial combat
         * charge mechanic should not remain stuck in an old state.
         */
        DamageSystem.clearBladeEnergy(playerName);

        trialManager.completeTrial(playerName);

        /*
         * We cannot safely remove tracked monsters directly here because this
         * interaction does not have a CommandBuffer. The cleanup system removes
         * them on the next tick.
         */
        TrialMonsterCleanupSystem.requestCleanup(playerName);

        player.sendMessage(Message.raw(""));

        for (String line : GoddessDialogueScript.COMPLETION_CHAT_LINES) {
            player.sendMessage(Message.raw(line));
        }


        GoddessCompletionSequenceSystem.startCompletionDialogue(playerName);
    }
}
