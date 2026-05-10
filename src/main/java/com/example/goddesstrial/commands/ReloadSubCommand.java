package com.example.goddesstrial.commands;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.trial.TrialEffects;
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
 * For now this clears the player's inventory completely.
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

        TrialEffects.clearPlayerInventory(player);

        context.sendMessage(Message.raw("GoddessTrial debug reload complete."));
        context.sendMessage(Message.raw("Your inventory was cleared."));
    }
}