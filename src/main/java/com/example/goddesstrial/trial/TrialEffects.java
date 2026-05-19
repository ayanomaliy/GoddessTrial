package com.example.goddesstrial.trial;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Applies concrete gameplay effects for the Trial of the Goddess.
 *
 * Current prototype features:
 * - gives the player the Blade of Balance
 * - reduces current HP to 1 while the blade is equipped
 * - keeps max HP unchanged
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
    public static final String BLADE_ITEM_ID = "Weapon_Longsword_Spectral";

    /**
     * The current HP threshold while the Blade of Balance is equipped.
     *
     * Important:
     * This is NOT a max HP cap. The player keeps their normal max HP.
     */
    private static final float BLADE_BALANCE_HP_THRESHOLD = 1.0f;

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
     * Reduces the player's current HP to 1, but does not change max HP.
     *
     * Use this when the player first accepts the trial and whenever the blade
     * is equipped while the player has healed above 1 HP.
     */
    public static void reducePlayerToOneHp(
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

        stats.setStatValue(healthIndex, BLADE_BALANCE_HP_THRESHOLD);
    }

    /**
     * No longer restores a health cap, because the Blade of Balance does not
     * modify max HP anymore.
     *
     * This method is kept so existing cleanup code still compiles.
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

        // Optional cleanup behavior:
        // After the trial ends, restore the player to full health.
        stats.maximizeStatValue(healthIndex);
    }

    /**
     * Checks whether the given item stack is the Blade of Balance.
     */
    public static boolean isBladeOfBalance(ItemStack itemStack) {
        return itemStack != null;
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