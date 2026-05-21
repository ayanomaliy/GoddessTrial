package com.example.goddesstrial.trial;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Spawns and removes the Sacred Flower objective.
 *
 * New approach:
 * - The world/map is fixed.
 * - Valid Sacred Flower locations are manually defined in SacredFlowerPositions.json.
 * - The plugin randomly picks one configured position.
 * - No terrain scanning, no random Y detection, no fallback guessing.
 */
public final class TrialFlowerSpawner {

    public static final String FLOWER_BLOCK_ID = "Plant_Flower_Orchid_Purple";
    public static final String AIR_BLOCK_ID = "Air";

    private TrialFlowerSpawner() {
    }

    public static void spawnSacredFlower(
            String playerName,
            Store<EntityStore> store,
            Ref<EntityStore> playerRef
    ) {
        Player player = store.getComponent(playerRef, Player.getComponentType());

        if (player == null) {
            System.out.println("[GoddessTrial] Could not place Sacred Flower: player component was null.");
            return;
        }

        EntityStore entityStore = store.getExternalData();
        World world = entityStore.getWorld();

        Vector3d flowerPosition = SacredFlowerPositionConfig.pickRandomPosition();

        if (flowerPosition == null) {
            player.sendMessage(Message.raw("The Sacred Flower has no known place to bloom."));
            System.out.println("[GoddessTrial] Could not place Sacred Flower: no configured position was available.");
            return;
        }

        int x = (int) Math.floor(flowerPosition.getX());
        int y = (int) Math.floor(flowerPosition.getY());
        int z = (int) Math.floor(flowerPosition.getZ());

        boolean placed = placeFlowerBlock(world, x, y, z);

        if (!placed) {
            player.sendMessage(Message.raw("The Sacred Flower tried to bloom, but the veil rejected its chosen place."));
            System.out.println("[GoddessTrial] Failed to place Sacred Flower at " + x + ", " + y + ", " + z);
            return;
        }

        Vector3d actualFlowerPosition = new Vector3d(x, y, z);

        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin != null) {
            plugin.getTrialManager().rememberSacredFlowerPosition(playerName, actualFlowerPosition);
        }

        player.sendMessage(Message.raw(
                "Somewhere beyond the veil, the Sacred Flower begins to bloom."
        ));

        player.sendMessage(Message.raw(
                "Debug Sacred Flower position: " + x + ", " + y + ", " + z
        ));

        System.out.println(
                "[GoddessTrial] Placed Sacred Flower for "
                        + playerName
                        + " at "
                        + x + ", " + y + ", " + z
        );
    }

    private static boolean placeFlowerBlock(World world, int x, int y, int z) {
        try {
            WorldChunk chunk = getChunk(world, x, z);

            if (chunk == null) {
                System.out.println("[GoddessTrial] Could not place Sacred Flower: chunk was null.");
                return false;
            }

            return chunk.setBlock(x, y, z, FLOWER_BLOCK_ID);
        } catch (Exception e) {
            System.out.println("[GoddessTrial] Exception while placing Sacred Flower.");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean removeFlowerBlock(World world, int x, int y, int z) {
        try {
            WorldChunk chunk = getChunk(world, x, z);

            if (chunk == null) {
                return false;
            }

            return chunk.setBlock(x, y, z, AIR_BLOCK_ID);
        } catch (Exception e) {
            System.out.println("[GoddessTrial] Exception while removing Sacred Flower.");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean removeStoredSacredFlower(
            String playerName,
            Store<EntityStore> store
    ) {
        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null) {
            return false;
        }

        Vector3d flowerPosition =
                plugin.getTrialManager().consumeSacredFlowerPosition(playerName);

        if (flowerPosition == null) {
            return false;
        }

        World world = store.getExternalData().getWorld();

        int x = (int) Math.floor(flowerPosition.getX());
        int y = (int) Math.floor(flowerPosition.getY());
        int z = (int) Math.floor(flowerPosition.getZ());

        boolean removed = removeFlowerBlock(world, x, y, z);

        System.out.println(
                "[GoddessTrial] Removed stored Sacred Flower for "
                        + playerName
                        + " at "
                        + x + ", " + y + ", " + z
                        + " | removed="
                        + removed
        );

        return removed;
    }

    private static WorldChunk getChunk(World world, int x, int z) {
        long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
        return world.getNonTickingChunk(chunkIndex);
    }
}