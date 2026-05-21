package com.example.goddesstrial.listeners;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Removes tracked trial monsters after successful trial completion.
 */
public class TrialMonsterCleanupSystem extends EntityTickingSystem<EntityStore> {

    private static final Set<String> PENDING_CLEANUP_PLAYER_NAMES = new HashSet<>();

    public static void requestCleanup(String playerName) {
        if (playerName != null && !playerName.isBlank()) {
            PENDING_CLEANUP_PLAYER_NAMES.add(playerName);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                Player.getComponentType(),
                PlayerRef.getComponentType()
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
        PlayerRef playerRef = archetypeChunk.getComponent(
                index,
                PlayerRef.getComponentType()
        );

        if (playerRef == null) {
            return;
        }

        String playerName = playerRef.getUsername();

        if (!PENDING_CLEANUP_PLAYER_NAMES.remove(playerName)) {
            return;
        }

        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null) {
            return;
        }

        List<Ref<EntityStore>> monsterRefs =
                plugin.getTrialManager().consumeSpawnedTrialMonsters(playerName);

        int removed = 0;
        int invalid = 0;

        for (Ref<EntityStore> monsterRef : monsterRefs) {
            if (monsterRef == null || !monsterRef.isValid()) {
                invalid++;
                continue;
            }

            commandBuffer.tryRemoveEntity(monsterRef, RemoveReason.REMOVE);
            removed++;
        }

        System.out.println(
                "[GoddessTrial] Trial completion monster cleanup for "
                        + playerName
                        + ": scheduled removals="
                        + removed
                        + ", invalid refs="
                        + invalid
        );
    }
}
