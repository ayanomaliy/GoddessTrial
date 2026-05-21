package com.example.goddesstrial.listeners;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Removes trial monsters after death/completion/shutdown cleanup.
 *
 * Prototype approach:
 * - protect players
 * - remove non-player entities near stored trial monster spawn positions
 * - use a broad radius because monsters can move/fly after spawning
 */
public class TrialMonsterCleanupSystem extends EntityTickingSystem<EntityStore> {

    private static final Map<String, Long> PENDING_CLEANUP_STARTED_AT = new HashMap<>();

    /*
     * The old radius 10 was too small. Monsters can wander/fly away from their
     * exact spawn point, especially after a server restart.
     */
    private static final double CLEANUP_RADIUS = 80.0;
    private static final double CLEANUP_RADIUS_SQUARED = CLEANUP_RADIUS * CLEANUP_RADIUS;

    /*
     * Keep cleanup alive long enough for nearby entities/chunks to tick.
     */
    private static final long CLEANUP_DURATION_MILLIS = 60_000L;

    public static void requestCleanup(String playerName) {
        if (playerName != null && !playerName.isBlank()) {
            PENDING_CLEANUP_STARTED_AT.put(playerName, System.currentTimeMillis());
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
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
        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null || PENDING_CLEANUP_STARTED_AT.isEmpty()) {
            return;
        }

        finalizeExpiredCleanups(plugin);

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);

        if (entityRef == null || !entityRef.isValid()) {
            return;
        }

        /*
         * Never remove players.
         */
        if (store.getComponent(entityRef, Player.getComponentType()) != null) {
            return;
        }

        if (store.getComponent(entityRef, PlayerRef.getComponentType()) != null) {
            return;
        }

        TransformComponent transform = archetypeChunk.getComponent(
                index,
                TransformComponent.getComponentType()
        );

        if (transform == null) {
            return;
        }

        Vector3d entityPosition = transform.getPosition();

        for (String playerName : Set.copyOf(PENDING_CLEANUP_STARTED_AT.keySet())) {
            List<Vector3d> monsterPositions =
                    plugin.getTrialManager().getSpawnedTrialMonsterPositions(playerName);

            if (monsterPositions.isEmpty()) {
                continue;
            }

            if (!isNearAnyStoredMonsterPosition(entityPosition, monsterPositions)) {
                continue;
            }

            commandBuffer.tryRemoveEntity(entityRef, RemoveReason.REMOVE);

            System.out.println(
                    "[GoddessTrial] Removed non-player entity near trial monster spawn for "
                            + playerName
                            + " at "
                            + entityPosition
            );

            return;
        }
    }

    private static void finalizeExpiredCleanups(GoddessTrialPlugin plugin) {
        long now = System.currentTimeMillis();

        for (String playerName : Set.copyOf(PENDING_CLEANUP_STARTED_AT.keySet())) {
            long startedAt = PENDING_CLEANUP_STARTED_AT.getOrDefault(playerName, now);

            if (now - startedAt < CLEANUP_DURATION_MILLIS) {
                continue;
            }

            plugin.getTrialManager().finishCleanupAndRemovePersistentState(playerName);
            PENDING_CLEANUP_STARTED_AT.remove(playerName);

            System.out.println(
                    "[GoddessTrial] Finished persistent monster cleanup for "
                            + playerName
            );
        }
    }

    private static boolean isNearAnyStoredMonsterPosition(
            Vector3d entityPosition,
            List<Vector3d> storedPositions
    ) {
        for (Vector3d storedPosition : storedPositions) {
            if (storedPosition == null) {
                continue;
            }

            if (distanceSquared(entityPosition, storedPosition) <= CLEANUP_RADIUS_SQUARED) {
                return true;
            }
        }

        return false;
    }

    private static double distanceSquared(Vector3d a, Vector3d b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();

        return dx * dx + dy * dy + dz * dz;
    }
}