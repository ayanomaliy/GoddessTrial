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
 * - initial wave: large dramatic wave with flying, small, normal, giant monsters and one rat herd
 * - reinforcement wave: recurring wave with flying, small and normal monsters only
 *
 * Monsters are spawned in separated clusters instead of all using the same
 * player-centered spawn ring.
 */
public final class TrialMonsterSpawner {

    private static final Random RANDOM = new Random();

    /**
     * Distance from the player where cluster centers can appear.
     */
    private static final double MIN_CLUSTER_DISTANCE = 14.0;
    private static final double MAX_CLUSTER_DISTANCE = 38.0;

    /**
     * Normal scatter radius inside each cluster.
     */
    private static final double DEFAULT_CLUSTER_SPREAD = 5.0;

    /**
     * Tight scatter radius for the rat herd.
     */
    private static final double RAT_HERD_SPREAD = 2.5;

    /**
     * One funny concentrated rat pile.
     */
    private static final int RAT_HERD_SIZE = 10;

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

    /**
     * Dragons removed because they spawn stuck in the ground.
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
            "Rex_Cave"
    };

    private TrialMonsterSpawner() {
        // Utility class
    }

    /**
     * Spawns the big initial trial wave.
     *
     * This includes giant monsters and a dedicated rat herd.
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

        System.out.println("[GoddessTrial] Spawning spread-out initial trial wave around player at " + playerPosition);

        /*
         * Each group gets its own cluster center.
         * This avoids everything appearing in one pile.
         */
        spawnGroupAtNewCluster(playerName, store, playerPosition, FLYING_MONSTERS, 12, 7.0);
        spawnGroupAtNewCluster(playerName, store, playerPosition, SMALL_MONSTERS, 8, 5.0);
        spawnGroupAtNewCluster(playerName, store, playerPosition, NORMAL_MONSTERS, 5, 6.0);
        spawnGroupAtNewCluster(playerName, store, playerPosition, GIANT_MONSTERS, 3, 8.0);

        /*
         * Dedicated joke/mechanic cluster:
         * a tight herd of at least 10 rats in one place.
         */
        spawnRatHerd(playerName, store, playerPosition);
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

        System.out.println("[GoddessTrial] Spawning spread-out reinforcement wave around player at " + playerPosition);

        spawnGroupAtNewCluster(playerName, store, playerPosition, FLYING_MONSTERS, 6, 6.0);
        spawnGroupAtNewCluster(playerName, store, playerPosition, SMALL_MONSTERS, 4, 5.0);
        spawnGroupAtNewCluster(playerName, store, playerPosition, NORMAL_MONSTERS, 2, 5.0);

        /*
         * Optional: small chance of a new rat herd during longer trials.
         * This makes ignored fights get increasingly ridiculous.
         */
        if (RANDOM.nextDouble() < 0.35) {
            spawnRatHerd(playerName, store, playerPosition);
        }
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

    /**
     * Picks one cluster center around the player, then spawns the whole group
     * around that center with local scatter.
     */
    private static void spawnGroupAtNewCluster(
            String playerName,
            Store<EntityStore> store,
            Vector3d playerPosition,
            String[] possibleMonsters,
            int amount,
            double clusterSpread
    ) {
        Vector3d clusterCenter = randomClusterCenterAround(playerPosition);

        System.out.println(
                "[GoddessTrial] Spawning cluster of "
                        + amount
                        + " monster(s) around "
                        + clusterCenter
        );

        for (int i = 0; i < amount; i++) {
            spawnRandomMonsterAtCluster(
                    playerName,
                    store,
                    clusterCenter,
                    possibleMonsters,
                    clusterSpread
            );
        }
    }

    /**
     * Spawns a tight herd of rats in one separate location.
     */
    private static void spawnRatHerd(
            String playerName,
            Store<EntityStore> store,
            Vector3d playerPosition
    ) {
        Vector3d herdCenter = randomClusterCenterAround(playerPosition);

        System.out.println(
                "[GoddessTrial] Spawning rat herd of "
                        + RAT_HERD_SIZE
                        + " rats around "
                        + herdCenter
        );

        for (int i = 0; i < RAT_HERD_SIZE; i++) {
            spawnSpecificMonsterAtCluster(
                    playerName,
                    store,
                    herdCenter,
                    "Rat",
                    RAT_HERD_SPREAD
            );
        }
    }

    /**
     * Spawns one random monster from the given list near a cluster center.
     */
    private static void spawnRandomMonsterAtCluster(
            String playerName,
            Store<EntityStore> store,
            Vector3d clusterCenter,
            String[] possibleMonsters,
            double clusterSpread
    ) {
        if (possibleMonsters == null || possibleMonsters.length == 0) {
            System.out.println("[GoddessTrial] No monster types configured for this category.");
            return;
        }

        String npcType = possibleMonsters[RANDOM.nextInt(possibleMonsters.length)];

        spawnSpecificMonsterAtCluster(
                playerName,
                store,
                clusterCenter,
                npcType,
                clusterSpread
        );
    }

    /**
     * Spawns one specific monster near a cluster center.
     */
    private static void spawnSpecificMonsterAtCluster(
            String playerName,
            Store<EntityStore> store,
            Vector3d clusterCenter,
            String npcType,
            double clusterSpread
    ) {
        Vector3d spawnPosition = randomPositionNear(clusterCenter, clusterSpread);

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

            System.out.println(
                    "[GoddessTrial] Spawned trial NPC type: "
                            + npcType
                            + " at "
                            + spawnPosition
            );
        } catch (Exception e) {
            System.out.println("[GoddessTrial] Error while spawning NPC type: " + npcType);
            e.printStackTrace();
        }
    }

    /**
     * Picks a cluster center in a wide ring around the player.
     */
    private static Vector3d randomClusterCenterAround(Vector3d center) {
        double angle = RANDOM.nextDouble() * Math.PI * 2.0;
        double distance = MIN_CLUSTER_DISTANCE
                + RANDOM.nextDouble() * (MAX_CLUSTER_DISTANCE - MIN_CLUSTER_DISTANCE);

        double x = center.x + Math.cos(angle) * distance;
        double z = center.z + Math.sin(angle) * distance;

        return new Vector3d(x, center.y, z);
    }

    /**
     * Picks a position close to a cluster center.
     */
    private static Vector3d randomPositionNear(Vector3d center, double spreadRadius) {
        double angle = RANDOM.nextDouble() * Math.PI * 2.0;
        double distance = RANDOM.nextDouble() * spreadRadius;

        double x = center.x + Math.cos(angle) * distance;
        double z = center.z + Math.sin(angle) * distance;

        return new Vector3d(x, center.y, z);
    }
}