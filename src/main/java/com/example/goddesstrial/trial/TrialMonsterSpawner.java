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
 * Handles trial monster spawning.
 *
 * There are two wave types:
 * - initial wave: large dramatic wave with flying, small, normal and giant monsters
 * - reinforcement wave: recurring wave with flying, small and normal monsters only
 */
public final class TrialMonsterSpawner {

    private static final Random RANDOM = new Random();

    private static final double MIN_SPAWN_RADIUS = 12.0;
    private static final double MAX_SPAWN_RADIUS = 28.0;

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
     * Spawns the big initial trial wave.
     *
     * This includes giant monsters.
     */
    public static void spawnTrialWave(
            String playerName,
            Store<EntityStore> store,
            Ref<EntityStore> playerRef
    ) {
        Vector3d playerPosition = getPlayerPosition(store, playerRef);

        if (playerPosition == null) {
            return;
        }

        System.out.println("[GoddessTrial] Spawning initial trial wave around player at " + playerPosition);

        spawnGroup(playerName, store, playerPosition, FLYING_MONSTERS, 12);
        spawnGroup(playerName, store, playerPosition, SMALL_MONSTERS, 8);
        spawnGroup(playerName, store, playerPosition, NORMAL_MONSTERS, 5);
        spawnGroup(playerName, store, playerPosition, GIANT_MONSTERS, 3);
    }

    /**
     * Spawns a recurring reinforcement wave.
     *
     * This intentionally excludes giant monsters.
     * If the player ignores the enemies, these waves accumulate over time.
     */
    public static void spawnReinforcementWave(
            String playerName,
            Store<EntityStore> store,
            Ref<EntityStore> playerRef
    ) {
        Vector3d playerPosition = getPlayerPosition(store, playerRef);

        if (playerPosition == null) {
            return;
        }

        System.out.println("[GoddessTrial] Spawning reinforcement wave around player at " + playerPosition);

        spawnGroup(playerName, store, playerPosition, FLYING_MONSTERS, 6);
        spawnGroup(playerName, store, playerPosition, SMALL_MONSTERS, 4);
        spawnGroup(playerName, store, playerPosition, NORMAL_MONSTERS, 2);
    }

    private static Vector3d getPlayerPosition(
            Store<EntityStore> store,
            Ref<EntityStore> playerRef
    ) {
        TransformComponent transform = store.getComponent(
                playerRef,
                TransformComponent.getComponentType()
        );

        if (transform == null) {
            System.out.println("[GoddessTrial] Could not spawn monsters: player has no TransformComponent.");
            return null;
        }

        return transform.getPosition();
    }

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

    private static Vector3d randomPositionAround(Vector3d center) {
        double angle = RANDOM.nextDouble() * Math.PI * 2.0;
        double distance = MIN_SPAWN_RADIUS
                + RANDOM.nextDouble() * (MAX_SPAWN_RADIUS - MIN_SPAWN_RADIUS);

        double x = center.x + Math.cos(angle) * distance;
        double z = center.z + Math.sin(angle) * distance;

        return new Vector3d(x, center.y, z);
    }
}