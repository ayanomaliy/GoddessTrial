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
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles Blade of Balance damage behavior.
 *
 * Energy mechanic:
 * - 3 empowered hits
 * - then 5 seconds recharge
 * - during recharge the Blade deals 0 damage
 * - after recharge the Blade has 3 empowered hits again
 */
public class DamageSystem extends DamageEventSystem {

    private static final float BLADE_DAMAGE_MULTIPLIER = 100_000.0f;
    private static final float MINIMUM_BLADE_DAMAGE = 1_000_000.0f;

    private static final int MAX_BLADE_CHARGES = 3;
    private static final long RECHARGE_DURATION_MILLIS = 5_000L;
    private static final long STATUS_MESSAGE_COOLDOWN_MILLIS = 1_000L;

    private static final Map<String, BladeEnergyState> ENERGY_BY_PLAYER_NAME = new HashMap<>();

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
            clearBladeEnergy(attackerName);
            return;
        }

        if (!isHoldingBladeOfBalance(store, attackerRef)) {
            return;
        }

        BladeEnergyState energyState = ENERGY_BY_PLAYER_NAME.computeIfAbsent(
                attackerName,
                ignored -> new BladeEnergyState()
        );

        long now = System.currentTimeMillis();

        if (energyState.shouldFinishRecharge(now)) {
            energyState.finishRecharge();
            sendDirectMessage(store, attackerRef, "The Blade of Balance is charged again.");
        }

        if (energyState.isRecharging(now)) {
            damage.setAmount(0.0f);

            sendStatusMessageIfAllowed(
                    store,
                    attackerRef,
                    energyState,
                    now,
                    "The Blade of Balance is still recharging..."
            );

            System.out.println(
                    "[GoddessTrial] Blade hit dealt 0 damage for "
                            + attackerName
                            + " because the blade is recharging."
            );
            return;
        }

        if (energyState.getCharges() <= 0) {
            energyState.startRecharge(now);
            damage.setAmount(0.0f);

            sendDirectMessage(
                    store,
                    attackerRef,
                    "The Blade of Balance is empty. Recharging for 5 seconds..."
            );

            return;
        }

        float originalDamage = damage.getAmount();
        float boostedDamage = Math.max(
                originalDamage * BLADE_DAMAGE_MULTIPLIER,
                MINIMUM_BLADE_DAMAGE
        );

        damage.setAmount(boostedDamage);
        energyState.consumeCharge();

        if (energyState.getCharges() > 0) {
            String chargeText = energyState.getCharges() == 1
                    ? "1 empowered hit remains."
                    : energyState.getCharges() + " empowered hits remain.";

            sendDirectMessage(
                    store,
                    attackerRef,
                    "Blade of Balance empowered hit. " + chargeText
            );
        } else {
            energyState.startRecharge(now);

            sendDirectMessage(
                    store,
                    attackerRef,
                    "The Blade of Balance is empty. Recharging for 5 seconds..."
            );
        }

        System.out.println(
                "[GoddessTrial] Blade of Balance boosted damage for "
                        + attackerName
                        + ": "
                        + originalDamage
                        + " -> "
                        + boostedDamage
                        + " | charges left: "
                        + energyState.getCharges()
        );
    }

    private boolean isHoldingBladeOfBalance(
            Store<EntityStore> store,
            Ref<EntityStore> attackerRef
    ) {
        InventoryComponent.Hotbar hotbar = store.getComponent(
                attackerRef,
                InventoryComponent.Hotbar.getComponentType()
        );

        if (hotbar == null) {
            return false;
        }

        ItemStack activeItem = hotbar.getActiveItem();

        return TrialEffects.isBladeOfBalance(activeItem);
    }

    private static void sendStatusMessageIfAllowed(
            Store<EntityStore> store,
            Ref<EntityStore> playerRef,
            BladeEnergyState energyState,
            long now,
            String message
    ) {
        if (now < energyState.getNextStatusMessageMillis()) {
            return;
        }

        sendDirectMessage(store, playerRef, message);
        energyState.setNextStatusMessageMillis(now + STATUS_MESSAGE_COOLDOWN_MILLIS);
    }

    private static void sendDirectMessage(
            Store<EntityStore> store,
            Ref<EntityStore> playerRef,
            String message
    ) {
        Player player = store.getComponent(playerRef, Player.getComponentType());

        if (player != null) {
            player.sendMessage(Message.raw(message));
        }
    }

    /**
     * Called by BladeRechargeNotificationSystem so the player gets a message
     * exactly when recharge completes, even if they do not attack again.
     */
    public static void checkRechargeNotification(
            String playerName,
            Store<EntityStore> store,
            Ref<EntityStore> playerRef
    ) {
        BladeEnergyState energyState = ENERGY_BY_PLAYER_NAME.get(playerName);

        if (energyState == null) {
            return;
        }

        long now = System.currentTimeMillis();

        if (!energyState.shouldFinishRecharge(now)) {
            return;
        }

        energyState.finishRecharge();
        sendDirectMessage(store, playerRef, "The Blade of Balance is charged again.");
    }

    public static void clearBladeEnergy(String playerName) {
        ENERGY_BY_PLAYER_NAME.remove(playerName);
    }

    private static final class BladeEnergyState {

        private int charges = MAX_BLADE_CHARGES;
        private long rechargeEndMillis = 0L;
        private long nextStatusMessageMillis = 0L;

        int getCharges() {
            return charges;
        }

        long getNextStatusMessageMillis() {
            return nextStatusMessageMillis;
        }

        void setNextStatusMessageMillis(long nextStatusMessageMillis) {
            this.nextStatusMessageMillis = nextStatusMessageMillis;
        }

        boolean isRecharging(long now) {
            return rechargeEndMillis > now;
        }

        boolean shouldFinishRecharge(long now) {
            return rechargeEndMillis > 0L && now >= rechargeEndMillis;
        }

        void finishRecharge() {
            charges = MAX_BLADE_CHARGES;
            rechargeEndMillis = 0L;
            nextStatusMessageMillis = 0L;
        }

        void consumeCharge() {
            if (charges > 0) {
                charges--;
            }
        }

        void startRecharge(long now) {
            charges = 0;
            rechargeEndMillis = now + RECHARGE_DURATION_MILLIS;
        }
    }
}