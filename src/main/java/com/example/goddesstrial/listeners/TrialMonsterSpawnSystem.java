package com.example.goddesstrial.listeners;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.trial.TrialPhase;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Spawns additional trial monsters around players during an active trial.
 *
 * This system does not modify the normal Hytale spawn rules.
 * Instead, it adds extra trial enemies near the player while the trial is active.
 */
public class TrialMonsterSpawnSystem extends EntityTickingSystem<EntityStore> {

    private static final Random RANDOM = new Random();

    private static final float SPAWN_INTERVAL_SECONDS = 6.0f;
    private static final double MIN_SPAWN_RADIUS = 12.0;
    private static final double MAX_SPAWN_RADIUS = 24.0;

    private float timer = 0.0f;

    /**
     * Candidate heavy enemies.
     *
     * These names are based on the NPC folders you found in Assets.zip.
     * If one of them is not a valid NPC type, the log will tell you.
     */
    private static final String[] HEAVY_MONSTERS = {
            "Golem_Guardian",
            "Shadow_Knight"
    };

    /**
     * Candidate flying enemies.
     *
     * Temporary placeholder: Ghoul is not flying, but this prevents an empty-array crash.
     * Replace this once you find a real flying NPC type.
     */
    private static final String[] FLYING_MONSTERS = {
            "Ghoul"
    };

    /**
     * Candidate small enemies.
     */
    private static final String[] SMALL_MONSTERS = {
            "Ghoul"
    };

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                Player.getComponentType(),
                PlayerRef.getComponentType(),
                TransformComponent.getComponentType()
        );
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        timer += dt;

        if (timer < SPAWN_INTERVAL_SECONDS) {
            return;
        }

        timer = 0.0f;

        PlayerRef playerRef = archetypeChunk.getComponent(
                index,
                PlayerRef.getComponentType()
        );

        if (playerRef == null) {
            return;
        }

        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null) {
            return;
        }

        String playerName = playerRef.getUsername();

        if (plugin.getTrialManager().getPhase(playerName) != TrialPhase.ACTIVE) {
            return;
        }

        TransformComponent transform = archetypeChunk.getComponent(
                index,
                TransformComponent.getComponentType()
        );

        if (transform == null) {
            return;
        }

        spawnTrialWave(store, transform);
    }

    /**
     * Spawns one trial wave around the player.
     */
    private void spawnTrialWave(
            Store<EntityStore> store,
            TransformComponent playerTransform
    ) {
        Vector3d playerPosition = playerTransform.getPosition();

        spawnRandomMonster(store, playerPosition, SMALL_MONSTERS);

        if (RANDOM.nextDouble() < 0.50) {
            spawnRandomMonster(store, playerPosition, FLYING_MONSTERS);
        }

        if (RANDOM.nextDouble() < 0.30) {
            spawnRandomMonster(store, playerPosition, HEAVY_MONSTERS);
        }
    }

    /**
     * Spawns one random monster from the given list.
     */
    private void spawnRandomMonster(
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

        var spawnedNpc = NPCPlugin.get().spawnNPC(
                store,
                npcType,
                null,
                spawnPosition,
                rotation
        );

        if (spawnedNpc == null) {
            System.out.println("[GoddessTrial] Could not spawn NPC type: " + npcType);
        } else {
            System.out.println("[GoddessTrial] Spawned NPC type: " + npcType);
        }
    }

    /**
     * Picks a random position in a ring around the player.
     */
    private Vector3d randomPositionAround(Vector3d center) {
        double angle = RANDOM.nextDouble() * Math.PI * 2.0;
        double distance = MIN_SPAWN_RADIUS
                + RANDOM.nextDouble() * (MAX_SPAWN_RADIUS - MIN_SPAWN_RADIUS);

        double x = center.x + Math.cos(angle) * distance;
        double z = center.z + Math.sin(angle) * distance;

        return new Vector3d(x, center.y, z);
    }
}