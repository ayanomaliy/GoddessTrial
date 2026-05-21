package com.example.goddesstrial.commands;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.trial.TrialEffects;
import com.example.goddesstrial.trial.TrialManager;
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
 * /trial reload - Prototype reload/debug command.
 *
 * This command resets the player's trial state and removes prototype trial effects:
 * - clears the player's inventory through the ECS inventory components
 * - also runs the old inventory cleanup fallback
 * - restores the player's health
 *
 * It is mainly meant for testing during development.
 */
public class ReloadSubCommand extends AbstractPlayerCommand {

    public ReloadSubCommand() {
        super("reload", "Reload/debug GoddessTrial");
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
        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null) {
            context.sendMessage(Message.raw("Error: Plugin not loaded."));
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) {
            context.sendMessage(Message.raw("Error: Could not access player component."));
            return;
        }

        String playerName = playerRef.getUsername();

        try {
            com.example.goddesstrial.trial.TrialFlowerSpawner.removeAllConfiguredSacredFlowers(store);
            TrialManager.TrialResult resetResult =
                    plugin.getTrialManager().resetTrial(playerName);

            TrialEffects.clearPlayerInventory(store, ref);

            /*
             * Legacy fallback:
             * Keep this only if you still have clearPlayerInventoryLegacy(Player)
             * in TrialEffects. It helps catch older/deprecated inventory storage.
             */
            TrialEffects.clearPlayerInventoryLegacy(player);

            TrialEffects.restorePlayerHealthCap(store, ref);

            context.sendMessage(Message.raw("GoddessTrial debug reload complete."));
            context.sendMessage(Message.raw(resetResult.message()));
            context.sendMessage(Message.raw("Your inventory was cleared."));
            context.sendMessage(Message.raw("Your health was restored."));
        } catch (Exception e) {
            context.sendMessage(Message.raw("Error during GoddessTrial reload: " + e.getMessage()));
        }
    }
}