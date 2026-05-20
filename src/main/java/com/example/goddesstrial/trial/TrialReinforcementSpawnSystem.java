package com.example.goddesstrial.listeners;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.trial.TrialMonsterSpawner;
import com.example.goddesstrial.trial.TrialPhase;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Spawns recurring non-giant monster reinforcements during an active trial.
 *
 * The longer the player avoids fighting, the more enemies accumulate.
 */
public class TrialReinforcementSpawnSystem extends EntityTickingSystem<EntityStore> {

    private static final float REINFORCEMENT_INTERVAL_SECONDS = 25.0f;

    private final Map<String, Float> timersByPlayerName = new HashMap<>();

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

        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null) {
            return;
        }

        String playerName = playerRef.getUsername();

        if (plugin.getTrialManager().getPhase(playerName) != TrialPhase.ACTIVE) {
            timersByPlayerName.remove(playerName);
            return;
        }

        float timer = timersByPlayerName.getOrDefault(playerName, 0.0f);
        timer += dt;

        if (timer < REINFORCEMENT_INTERVAL_SECONDS) {
            timersByPlayerName.put(playerName, timer);
            return;
        }

        timersByPlayerName.put(playerName, 0.0f);

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

        TrialMonsterSpawner.spawnReinforcementWave(
                playerName,
                store,
                ref
        );
    }
}