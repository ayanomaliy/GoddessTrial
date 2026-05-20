package com.example.goddesstrial.trial;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;

import java.util.Random;

/**
 * Handles one-time spawning of trial monsters.
 *
 * The trial wave is intentionally chaotic:
 * - many annoying small/flying enemies
 * - several normal enemies
 * - a few giant enemies that are satisfying to one-hit with the Blade of Balance
 */
public final class TrialMonsterSpawner {

    private static final Random RANDOM = new Random();

    private static final double MIN_SPAWN_RADIUS = 12.0;
    private static final double MAX_SPAWN_RADIUS = 28.0;

    /**
     * Annoying flying enemies.
     *
     * These are meant to be hard to hit, visually chaotic, and annoying.
     */
    private static final String[] FLYING_MONSTERS = {
            "Bat",
            "Bat_Ice",
            "Eye_Void",
            "Wraith",
            "Wraith_Lantern",
            "Spark_Living",
            "Pterodactyl",
            "Vulture",
            "Hawk",
            "Raven"
    };

    /**
     * Small swarm enemies.
     *
     * These add pressure around the player while the flying enemies distract them.
     */
    private static final String[] SMALL_MONSTERS = {
            "Scarak_Louse",
            "Dungeon_Scarak_Louse",
            "Larva_Void",
            "Crawler_Void",
            "Rat",
            "Spider",
            "Spider_Cave",
            "Scorpion",
            "Zombie_Aberrant_Small",
            "Skeleton_Incandescent_Head"
    };

    /**
     * Normal combat enemies.
     */
    private static final String[] NORMAL_MONSTERS = {
            "Ghoul",
            "Zombie",
            "Zombie_Burnt",
            "Zombie_Frost",
            "Zombie_Sand",
            "Skeleton_Fighter",
            "Skeleton_Knight",
            "Skeleton_Mage",
            "Skeleton_Ranger",
            "Goblin_Scrapper",
            "Goblin_Lobber",
            "Outlander_Stalker",
            "Outlander_Hunter"
    };

    /**
     * Giant / dramatic enemies.
     *
     * These are the funny "oh no" enemies that should feel ridiculous
     * when the Blade of Balance one-hits them.
     */
    private static final String[] GIANT_MONSTERS = {
            "Scarak_Broodmother",
            "Dungeon_Scarak_Broodmother",
            "Goblin_Ogre",
            "Golem_Firesteel",
            "Golem_Crystal_Earth",
            "Golem_Crystal_Flame",
            "Golem_Crystal_Frost",
            "Golem_Crystal_Sand",
            "Golem_Crystal_Thunder",
            "Zombie_Aberrant_Big",
            "Yeti",
            "Rex_Cave",
            "Dragon_Fire",
            "Dragon_Frost"
    };

    private TrialMonsterSpawner() {
        // Utility class
    }

    /**
     * Spawns a one-time trial monster wave around the player.
     *
     * @param playerName the player currently doing the trial
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

        System.out.println("[GoddessTrial] Spawning large trial wave around player at " + playerPosition);

        /*
         * Big chaotic wave:
         * - 12 flying enemies: annoying, hard to hit, visually chaotic
         * - 8 small enemies: swarm pressure
         * - 5 normal enemies: actual combat body
         * - 3 giants: funny one-hit targets
         *
         * Total: 28 enemies.
         *
         * If this is too much for performance, reduce flying to 8 and small to 5.
         */
        spawnGroup(playerName, store, playerPosition, FLYING_MONSTERS, 12);
        spawnGroup(playerName, store, playerPosition, SMALL_MONSTERS, 8);
        spawnGroup(playerName, store, playerPosition, NORMAL_MONSTERS, 5);
        spawnGroup(playerName, store, playerPosition, GIANT_MONSTERS, 3);
    }

    /**
     * Spawns several random monsters from one category.
     */
    private static void spawnGroup(
            String playerName,
            Store<EntityStore> store,
            Vector3d playerPosition,
            String[] possibleMonsters,
            int amount
    ) {
        for (int i = 0; i < amount; i++) {
            spawnRandomMonster(playerName, store, playerPosition, possibleMonsters);
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