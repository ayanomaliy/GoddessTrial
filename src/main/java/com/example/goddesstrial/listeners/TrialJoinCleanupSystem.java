package com.example.goddesstrial.listeners;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.trial.TrialEffects;
import com.example.goddesstrial.trial.TrialFlowerSpawner;
import com.example.goddesstrial.trial.TrialInventoryUtil;
import com.example.goddesstrial.trial.TrialPhase;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.example.goddesstrial.trial.TrialObjectiveTracker;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Delayed player join validation.
 *
 * Server restart behavior:
 * - active/offered trials are not restored after restart
 * - pending cleanup removes trial items, removes the remembered flower if possible,
 *   schedules monster purge
 * - the player is healed after the trial is cancelled
 */
public class TrialJoinCleanupSystem extends EntityTickingSystem<EntityStore> {

    private static final Set<String> PENDING_PLAYER_NAMES = new HashSet<>();

    public static void requestCleanup(String playerName) {
        if (playerName != null && !playerName.isBlank()) {
            PENDING_PLAYER_NAMES.add(playerName);
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

        if (!PENDING_PLAYER_NAMES.remove(playerName)) {
            return;
        }

        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null) {
            return;
        }

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

        if (ref == null || !ref.isValid()) {
            return;
        }

        Player player = archetypeChunk.getComponent(
                index,
                Player.getComponentType()
        );

        if (player == null) {
            return;
        }

        /*
         * If the player entity is still in death state, try again next tick.
         * Do not heal while Hytale is still processing death/respawn.
         */
        if (hasDeathComponent(store, ref)) {
            PENDING_PLAYER_NAMES.add(playerName);
            return;
        }

        TrialPhase phase = plugin.getTrialManager().getPhase(playerName);

        if (plugin.getTrialManager().isMonsterCleanupPending(playerName)) {
            /*
             * Only remove the remembered flower position.
             *
             * Do not call removeAllConfiguredSacredFlowers(store) here. That broad
             * cleanup currently relies on an invalid "Air" block key and can fail
             * repeatedly while leaving stale flowers behind.
             */
            TrialFlowerSpawner.removeStoredSacredFlower(playerName, store);

            TrialMonsterCleanupSystem.requestCleanup(playerName);

            TrialInventoryUtil.removeBladeOfBalance(player, store, ref);
            TrialInventoryUtil.removeSacredFlower(player, store, ref);

            /*
             * Important:
             * Trial is over after restart/death cleanup, so the player must not
             * remain at Blade-of-Balance HP.
             */
            TrialEffects.restorePlayerHealthCap(store, ref);
            DamageSystem.clearBladeEnergy(playerName);
            TrialObjectiveTracker.clearObjective(playerRef);

            System.out.println(
                    "[GoddessTrial] Join validation for "
                            + playerName
                            + ": pending shutdown/death cleanup found. Trial items removed, HP restored, monster cleanup scheduled."
            );

            return;
        }

        if (phase == TrialPhase.COMPLETED) {
            TrialObjectiveTracker.clearObjective(playerRef);

            System.out.println(
                    "[GoddessTrial] Join validation for "
                            + playerName
                            + ": completed trial. Objective cleared."
            );

            return;
        }

        if (phase == TrialPhase.ACTIVE) {
            System.out.println(
                    "[GoddessTrial] Join validation for "
                            + playerName
                            + ": active trial in same server session. No inventory cleanup."
            );
            return;
        }

        if (phase == TrialPhase.OFFERED) {
            System.out.println(
                    "[GoddessTrial] Join validation for "
                            + playerName
                            + ": offered trial in same server session. No inventory cleanup."
            );
            return;
        }

        boolean removedBlade = TrialInventoryUtil.removeBladeOfBalance(player, store, ref);
        boolean removedFlower = TrialInventoryUtil.removeSacredFlower(player, store, ref);

        TrialEffects.restorePlayerHealthCap(store, ref);
        DamageSystem.clearBladeEnergy(playerName);
        TrialObjectiveTracker.clearObjective(playerRef);


        if (removedBlade || removedFlower) {
            System.out.println(
                    "[GoddessTrial] Join validation for "
                            + playerName
                            + ": removed leftover trial item(s), HP restored. Blade="
                            + removedBlade
                            + ", Flower="
                            + removedFlower
            );
        }
    }

    private static boolean hasDeathComponent(
            Store<EntityStore> store,
            Ref<EntityStore> playerRef
    ) {
        try {
            return store.getComponent(
                    playerRef,
                    DeathComponent.getComponentType()
            ) != null;
        } catch (Exception e) {
            return false;
        }
    }
}