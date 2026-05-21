package com.example.goddesstrial.listeners;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.trial.TrialEffects;
import com.example.goddesstrial.trial.TrialPhase;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Keeps the Blade of Balance health behavior active.
 *
 * This does not reduce max HP.
 * It only reduces current HP back to 1 while the player has the Blade of Balance equipped.
 */
public class BladeBalanceHealthSystem extends EntityTickingSystem<EntityStore> {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                Player.getComponentType(),
                PlayerRef.getComponentType(),
                InventoryComponent.Hotbar.getComponentType()
        );
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

        PlayerRef playerRef = archetypeChunk.getComponent(
                index,
                PlayerRef.getComponentType()
        );

        if (playerRef == null) {
            return;
        }

        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null) {
            return;
        }

        String playerName = playerRef.getUsername();

        TrialPhase phase = plugin.getTrialManager().getPhase(playerName);

        if (phase != TrialPhase.ACTIVE && phase != TrialPhase.COMPLETED) {
            return;
        }

        InventoryComponent.Hotbar hotbar = archetypeChunk.getComponent(
                index,
                InventoryComponent.Hotbar.getComponentType()
        );

        if (hotbar == null) {
            return;
        }

        ItemStack activeItem = hotbar.getActiveItem();

        if (!TrialEffects.isBladeOfBalance(activeItem)) {
            return;
        }

        TrialEffects.reducePlayerToOneHp(store, ref);
    }
}