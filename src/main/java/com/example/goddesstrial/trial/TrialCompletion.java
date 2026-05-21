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
        TrialInventoryUtil.removeSacredFlower(player, store, playerRef);
        TrialInventoryUtil.removeBladeOfBalance(player, store, playerRef);

        TrialEffects.restorePlayerHealthCap(store, playerRef);
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
