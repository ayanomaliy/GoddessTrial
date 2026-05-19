package com.example.goddesstrial.listeners;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.trial.TrialEffects;
import com.example.goddesstrial.trial.TrialPhase;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Handles Blade of Balance damage behavior.
 *
 * The Blade of Balance only boosts damage when the active trial player is
 * actually holding the Blade item in the selected hotbar slot.
 */
public class DamageSystem extends DamageEventSystem {

    /**
     * Huge multiplier to make the Blade of Balance one-hit enemies.
     */
    private static final float BLADE_DAMAGE_MULTIPLIER = 100_000.0f;

    /**
     * Safety floor in case the original damage is very small.
     */
    private static final float MINIMUM_BLADE_DAMAGE = 1_000_000.0f;

    /**
     * Critical:
     * Run in the Filter Damage Group so the modified damage is applied to HP.
     */
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getFilterDamageGroup();
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull Damage damage
    ) {
        if (damage.isCancelled()) {
            return;
        }

        Damage.Source source = damage.getSource();

        if (!(source instanceof Damage.EntitySource entitySource)) {
            return;
        }

        Ref<EntityStore> attackerRef = entitySource.getRef();

        if (attackerRef == null || !attackerRef.isValid()) {
            return;
        }

        PlayerRef attackerPlayerRef = store.getComponent(
                attackerRef,
                PlayerRef.getComponentType()
        );

        if (attackerPlayerRef == null) {
            return;
        }

        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null) {
            return;
        }

        String attackerName = attackerPlayerRef.getUsername();

        if (plugin.getTrialManager().getPhase(attackerName) != TrialPhase.ACTIVE) {
            return;
        }

        if (!isHoldingBladeOfBalance(store, attackerRef)) {
            System.out.println("[GoddessTrial] Damage not boosted: attacker is not holding Blade of Balance.");
            return;
        }

        float originalDamage = damage.getAmount();
        float boostedDamage = Math.max(
                originalDamage * BLADE_DAMAGE_MULTIPLIER,
                MINIMUM_BLADE_DAMAGE
        );

        damage.setAmount(boostedDamage);

        System.out.println(
                "[GoddessTrial] Blade of Balance boosted damage for "
                        + attackerName
                        + ": "
                        + originalDamage
                        + " -> "
                        + boostedDamage
        );
    }

    /**
     * Checks whether the attacking player currently has the Blade of Balance
     * selected in the hotbar.
     */
    private boolean isHoldingBladeOfBalance(
            Store<EntityStore> store,
            Ref<EntityStore> attackerRef
    ) {
        InventoryComponent.Hotbar hotbar = store.getComponent(
                attackerRef,
                InventoryComponent.Hotbar.getComponentType()
        );

        if (hotbar == null) {
            System.out.println("[GoddessTrial] Damage not boosted: attacker has no hotbar component.");
            return false;
        }

        ItemStack activeItem = hotbar.getActiveItem();

        if (activeItem == null || activeItem.isEmpty()) {
            System.out.println("[GoddessTrial] Damage not boosted: active item is empty.");
            return false;
        }

        System.out.println("[GoddessTrial] Active item while attacking: " + activeItem.getItemId());

        return TrialEffects.isBladeOfBalance(activeItem);
    }
}