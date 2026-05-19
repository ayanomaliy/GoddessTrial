package com.example.goddesstrial.trial;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.example.goddesstrial.GoddessTrialPlugin;

import java.util.Random;

/**
 * Handles one-time spawning of trial monsters.
 *
 * This class is intentionally not an EntityTickingSystem.
 * NPCPlugin.spawnNPC creates entities, so it should be called from safe contexts
 * such as commands, dialogue callbacks, statue interaction callbacks, or item interaction callbacks.
 */
public final class TrialMonsterSpawner {

    private static final Random RANDOM = new Random();

    private static final double MIN_SPAWN_RADIUS = 12.0;
    private static final double MAX_SPAWN_RADIUS = 24.0;

    /**
     * Heavy monsters found from local Hytale assets / server logs.
     */
    private static final String[] HEAVY_MONSTERS = {
            "Golem_Guardian",
            "Shadow_Knight",
            "Golem_Firesteel"
    };

    /**
     * Temporary flying monster list.
     *
     * Replace these once you find real flying NPC ids.
     */
    private static final String[] FLYING_MONSTERS = {
            "Ghoul"
    };

    /**
     * Smaller / normal enemies.
     */
    private static final String[] SMALL_MONSTERS = {
            "Ghoul",
            "Skeleton_Incandescent_Fighter",
            "Skeleton_Incandescent_Mage",
            "Skeleton_Knight"
    };

    private TrialMonsterSpawner() {
        // Utility class
    }

    /**
     * Spawns a one-time trial monster wave around the player.
     *
     * @param store the entity store
     * @param playerRef the player entity reference
     */
    public static void spawnTrialWave(
            String playerName,
            Store<EntityStore> store,
            Ref<EntityStore> playerRef
    ) {
        TransformComponent transform = store.getComponent(
                playerRef,
                TransformComponent.getComponentType()
        );

        if (transform == null) {
            System.out.println("[GoddessTrial] Could not spawn trial wave: player has no TransformComponent.");
            return;
        }

        Vector3d playerPosition = transform.getPosition();

        System.out.println("[GoddessTrial] Spawning trial wave around player at " + playerPosition);

        // Several normal/small enemies
        spawnRandomMonster(playerName, store, playerPosition, SMALL_MONSTERS);
        spawnRandomMonster(playerName, store, playerPosition, SMALL_MONSTERS);
        spawnRandomMonster(playerName, store, playerPosition, SMALL_MONSTERS);

        spawnRandomMonster(playerName, store, playerPosition, FLYING_MONSTERS);

        spawnRandomMonster(playerName, store, playerPosition, HEAVY_MONSTERS);

        if (RANDOM.nextDouble() < 0.50) {
            spawnRandomMonster(playerName, store, playerPosition, HEAVY_MONSTERS);
        }
    }

    /**
     * Spawns one random monster from the given list.
     */
    private static void spawnRandomMonster(
            String playerName,
            Store<EntityStore> store,
            Vector3d playerPosition,
            String[] possibleMonsters
    ) {
        if (possibleMonsters == null || possibleMonsters.length == 0) {
            System.out.println("[GoddessTrial] No monster types configured for this category.");
            return;
        }

        String npcType = possibleMonsters[RANDOM.nextInt(possibleMonsters.length)];
        Vector3d spawnPosition = randomPositionAround(playerPosition);

        Vector3f rotation = new Vector3f(
                0.0f,
                RANDOM.nextFloat() * 360.0f,
                0.0f
        );

        try {
            var spawnedNpc = NPCPlugin.get().spawnNPC(
                    store,
                    npcType,
                    null,
                    spawnPosition,
                    rotation
            );

            if (spawnedNpc == null) {
                System.out.println("[GoddessTrial] Could not spawn NPC type: " + npcType);
                return;
            }

            Ref<EntityStore> monsterRef = spawnedNpc.first();

            GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

            if (plugin != null) {
                plugin.getTrialManager().rememberSpawnedTrialMonster(playerName, monsterRef);
            }

            System.out.println("[GoddessTrial] Spawned trial NPC type: " + npcType);
        } catch (Exception e) {
            System.out.println("[GoddessTrial] Error while spawning NPC type: " + npcType);
            e.printStackTrace();
        }
    }

    /**
     * Chooses a random position in a ring around the player.
     */
    private static Vector3d randomPositionAround(Vector3d center) {
        double angle = RANDOM.nextDouble() * Math.PI * 2.0;
        double distance = MIN_SPAWN_RADIUS
                + RANDOM.nextDouble() * (MAX_SPAWN_RADIUS - MIN_SPAWN_RADIUS);

        double x = center.x + Math.cos(angle) * distance;
        double z = center.z + Math.sin(angle) * distance;

        return new Vector3d(x, center.y, z);
    }
}