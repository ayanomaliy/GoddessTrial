package com.example.goddesstrial.trial;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier.ModifierTarget;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Applies concrete gameplay effects for the Trial of the Goddess.
 *
 * Current prototype features:
 * - gives the player the Blade of Balance
 * - locks max/current health to 1 HP
 * - restores health cap after trial
 * - removes the Blade of Balance
 * - clears inventory for debugging via /trial reload
 */
public final class TrialEffects {

    /**
     * Existing Hytale item used as prototype Blade of Balance.
     *
     * Asset file:
     * Server/Item/Items/Weapon/Longsword/Weapon_Longsword_Spectral.json
     */
    private static final String BLADE_ITEM_ID = "Weapon_Longsword_Spectral";

    /**
     * Unique key for the HP cap modifier.
     * Used so we can remove exactly our modifier later.
     */
    private static final String TRIAL_HEALTH_MODIFIER_KEY =
            "goddesstrial_blade_of_balance_hp_cap";

    private TrialEffects() {
        // Utility class
    }

    /**
     * Gives the player the Blade of Balance.
     *
     * Uses Player#giveItem instead of directly accessing the inventory where possible.
     */
    public static void grantBladeOfBalance(
            Player player,
            Ref<EntityStore> playerRef,
            Store<EntityStore> store
    ) {
        ItemStack blade = new ItemStack(BLADE_ITEM_ID, 1);
        player.giveItem(blade, playerRef, store);
    }

    /**
     * Locks the player to exactly 1 HP.
     *
     * This changes the max health to 1 and also sets current health to 1.
     * Because max health is capped, normal healing should not raise the player above 1 HP.
     */
    public static void lockPlayerToOneHp(
            Store<EntityStore> store,
            Ref<EntityStore> playerRef
    ) {
        EntityStatMap stats = store.getComponent(
                playerRef,
                EntityStatsModule.get().getEntityStatMapComponentType()
        );

        if (stats == null) {
            return;
        }

        int healthIndex = DefaultEntityStatTypes.getHealth();

        // Prevent duplicate stacking if this method is called multiple times.
        stats.removeModifier(healthIndex, TRIAL_HEALTH_MODIFIER_KEY);

        float currentMaxHealth = stats.get(healthIndex).getMax();

        // Additive modifier that makes max HP exactly 1.
        // Example: if max HP is 20, modifier is -19.
        float modifierAmount = 1.0f - currentMaxHealth;

        StaticModifier maxHpCap = new StaticModifier(
                ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                modifierAmount
        );

        stats.putModifier(healthIndex, TRIAL_HEALTH_MODIFIER_KEY, maxHpCap);

        // Force current HP to 1 as well.
        stats.setStatValue(healthIndex, 1.0f);
    }

    /**
     * Removes the 1 HP cap and restores the player to full health.
     *
     * Use this when the trial ends, fails, or gets reset.
     */
    public static void restorePlayerHealthCap(
            Store<EntityStore> store,
            Ref<EntityStore> playerRef
    ) {
        EntityStatMap stats = store.getComponent(
                playerRef,
                EntityStatsModule.get().getEntityStatMapComponentType()
        );

        if (stats == null) {
            return;
        }

        int healthIndex = DefaultEntityStatTypes.getHealth();

        stats.removeModifier(healthIndex, TRIAL_HEALTH_MODIFIER_KEY);

        // Restore to full health after removing the cap.
        stats.maximizeStatValue(healthIndex);
    }

    /**
     * Removes one Blade of Balance from the player's inventory.
     *
     * This still uses the older inventory API because the current Player class
     * does not expose a clean non-deprecated inventory-clear/remove method.
     */
    @SuppressWarnings({"removal", "deprecation"})
    public static void removeBladeOfBalance(Player player) {
        ItemStack blade = new ItemStack(BLADE_ITEM_ID, 1);

        try {
            player.getInventory()
                    .getStorage()
                    .removeItemStack(blade);
        } catch (Exception ignored) {
            // Prototype fallback.
            // If exact stack removal fails, /trial reload can still clear inventory.
        }
    }

    /**
     * Debug helper for /trial reload.
     *
     * Clears many possible storage slots.
     * This is intentionally brute-force and only meant for the prototype phase.
     */
    @SuppressWarnings({"removal", "deprecation"})
    public static void clearPlayerInventory(Player player) {
        for (short slot = 0; slot < 200; slot++) {
            try {
                player.getInventory()
                        .getStorage()
                        .removeItemStackFromSlot(slot);
            } catch (Exception ignored) {
                // Some slots may not exist or may already be empty.
            }
        }
    }
}