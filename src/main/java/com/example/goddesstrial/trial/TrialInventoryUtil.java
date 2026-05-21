package com.example.goddesstrial.trial;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Method;

/**
 * Inventory helper for quest items.
 *
 * Uses reflection for slot access because Hytale's inventory API is still unstable.
 */
public final class TrialInventoryUtil {

    private static final int MAX_SLOT_SCAN = 200;

    private TrialInventoryUtil() {
    }

    public static boolean hasSacredFlower(
            Player player,
            Store<EntityStore> store,
            Ref<EntityStore> playerRef
    ) {
        return hasItem(player, store, playerRef, TrialFlowerConstants.SACRED_FLOWER_ITEM_ID);
    }

    public static boolean removeSacredFlower(
            Player player,
            Store<EntityStore> store,
            Ref<EntityStore> playerRef
    ) {
        return removeOneItem(player, store, playerRef, TrialFlowerConstants.SACRED_FLOWER_ITEM_ID);
    }

    public static boolean removeBladeOfBalance(
            Player player,
            Store<EntityStore> store,
            Ref<EntityStore> playerRef
    ) {
        return removeOneItem(player, store, playerRef, TrialEffects.BLADE_ITEM_ID);
    }

    public static boolean hasItem(
            Player player,
            Store<EntityStore> store,
            Ref<EntityStore> playerRef,
            String itemId
    ) {
        if (itemId == null || itemId.isBlank()) {
            return false;
        }

        if (hasItemLegacy(player, itemId)) {
            return true;
        }

        return hasItemInInventoryComponent(store, playerRef, InventoryComponent.Hotbar.getComponentType(), itemId)
                || hasItemInInventoryComponent(store, playerRef, InventoryComponent.Storage.getComponentType(), itemId)
                || hasItemInInventoryComponent(store, playerRef, InventoryComponent.Backpack.getComponentType(), itemId)
                || hasItemInInventoryComponent(store, playerRef, InventoryComponent.Utility.getComponentType(), itemId)
                || hasItemInInventoryComponent(store, playerRef, InventoryComponent.Tool.getComponentType(), itemId)
                || hasItemInInventoryComponent(store, playerRef, InventoryComponent.Armor.getComponentType(), itemId);
    }

    public static boolean removeOneItem(
            Player player,
            Store<EntityStore> store,
            Ref<EntityStore> playerRef,
            String itemId
    ) {
        if (itemId == null || itemId.isBlank()) {
            return false;
        }

        if (removeOneItemLegacy(player, itemId)) {
            return true;
        }

        return removeOneItemFromInventoryComponent(store, playerRef, InventoryComponent.Hotbar.getComponentType(), itemId)
                || removeOneItemFromInventoryComponent(store, playerRef, InventoryComponent.Storage.getComponentType(), itemId)
                || removeOneItemFromInventoryComponent(store, playerRef, InventoryComponent.Backpack.getComponentType(), itemId)
                || removeOneItemFromInventoryComponent(store, playerRef, InventoryComponent.Utility.getComponentType(), itemId)
                || removeOneItemFromInventoryComponent(store, playerRef, InventoryComponent.Tool.getComponentType(), itemId)
                || removeOneItemFromInventoryComponent(store, playerRef, InventoryComponent.Armor.getComponentType(), itemId);
    }

    @SuppressWarnings({"removal", "deprecation"})
    private static boolean hasItemLegacy(Player player, String itemId) {
        if (player == null) {
            return false;
        }

        try {
            Object storage = player.getInventory().getStorage();

            for (short slot = 0; slot < MAX_SLOT_SCAN; slot++) {
                ItemStack stack = getStackFromSlot(storage, slot);

                if (isItem(stack, itemId)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            // Legacy API may not expose slot reading on every server version.
        }

        return false;
    }

    @SuppressWarnings({"removal", "deprecation"})
    private static boolean removeOneItemLegacy(Player player, String itemId) {
        if (player == null) {
            return false;
        }

        try {
            Object storage = player.getInventory().getStorage();

            for (short slot = 0; slot < MAX_SLOT_SCAN; slot++) {
                ItemStack stack = getStackFromSlot(storage, slot);

                if (isItem(stack, itemId)) {
                    invokeRemoveSlot(storage, slot);
                    return true;
                }
            }
        } catch (Exception ignored) {
            // Legacy API may not expose slot reading on every server version.
        }

        return false;
    }

    private static boolean hasItemInInventoryComponent(
            Store<EntityStore> store,
            Ref<EntityStore> playerRef,
            ComponentType<EntityStore, ? extends InventoryComponent> componentType,
            String itemId
    ) {
        InventoryComponent component = store.getComponent(playerRef, componentType);

        if (component == null) {
            return false;
        }

        ItemContainer container = component.getInventory();

        if (container == null) {
            return false;
        }

        for (short slot = 0; slot < MAX_SLOT_SCAN; slot++) {
            ItemStack stack = getStackFromSlot(container, slot);

            if (isItem(stack, itemId)) {
                return true;
            }
        }

        return false;
    }

    private static boolean removeOneItemFromInventoryComponent(
            Store<EntityStore> store,
            Ref<EntityStore> playerRef,
            ComponentType<EntityStore, ? extends InventoryComponent> componentType,
            String itemId
    ) {
        InventoryComponent component = store.getComponent(playerRef, componentType);

        if (component == null) {
            return false;
        }

        ItemContainer container = component.getInventory();

        if (container == null) {
            return false;
        }

        for (short slot = 0; slot < MAX_SLOT_SCAN; slot++) {
            ItemStack stack = getStackFromSlot(container, slot);

            if (isItem(stack, itemId)) {
                invokeRemoveSlot(container, slot);
                component.markDirty();
                return true;
            }
        }

        return false;
    }

    private static ItemStack getStackFromSlot(Object container, short slot) {
        if (container == null) {
            return null;
        }

        String[] methodNames = {
                "getItemStackFromSlot",
                "getItemStack",
                "getStack",
                "get"
        };

        for (String methodName : methodNames) {
            try {
                Method method = container.getClass().getMethod(methodName, short.class);
                Object result = method.invoke(container, slot);

                if (result instanceof ItemStack stack) {
                    return stack;
                }
            } catch (Exception ignored) {
                // Try next method name.
            }

            try {
                Method method = container.getClass().getMethod(methodName, int.class);
                Object result = method.invoke(container, (int) slot);

                if (result instanceof ItemStack stack) {
                    return stack;
                }
            } catch (Exception ignored) {
                // Try next method name.
            }
        }

        return null;
    }

    private static void invokeRemoveSlot(Object container, short slot) {
        if (container == null) {
            return;
        }

        String[] methodNames = {
                "removeItemStackFromSlot",
                "removeItemStack",
                "removeStack",
                "remove"
        };

        for (String methodName : methodNames) {
            try {
                Method method = container.getClass().getMethod(methodName, short.class);
                method.invoke(container, slot);
                return;
            } catch (Exception ignored) {
                // Try next method name.
            }

            try {
                Method method = container.getClass().getMethod(methodName, int.class);
                method.invoke(container, (int) slot);
                return;
            } catch (Exception ignored) {
                // Try next method name.
            }
        }
    }

    private static boolean isItem(ItemStack stack, String itemId) {
        return stack != null
                && !stack.isEmpty()
                && itemId.equals(stack.getItemId());
    }
}
