package com.example.goddesstrial.trial;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrialManager {

    private final TrialPersistence.PersistentTrialFile persistentFile;

    private final Map<String, TrialState> statesByPlayerName = new HashMap<>();
    private final Map<String, List<Ref<EntityStore>>> spawnedTrialMonstersByPlayerName = new HashMap<>();
    private final Map<String, Vector3d> sacredFlowerPositionsByPlayerName = new HashMap<>();
    private final Map<String, List<Vector3d>> spawnedTrialMonsterPositionsByPlayerName = new HashMap<>();

    public TrialManager() {
        this.persistentFile = TrialPersistence.load();
        loadPersistentStates();
    }

    private void loadPersistentStates() {
        if (persistentFile.players == null) {
            persistentFile.players = new HashMap<>();
        }

        boolean changed = false;

        for (Map.Entry<String, TrialPersistence.PersistentPlayerTrial> entry
                : persistentFile.players.entrySet()) {

            String playerName = entry.getKey();
            TrialPersistence.PersistentPlayerTrial saved = entry.getValue();

            if (playerName == null || saved == null) {
                continue;
            }

            /*
             * New rule:
             * We never resume ACTIVE/OFFERED/COMPLETED trials after server restart.
             * Saved trial data only exists so cleanup can happen.
             */
            TrialState state = getOrCreateState(playerName);
            state.setPhase(TrialPhase.NONE);

            if (saved.sacredFlowerPosition != null) {
                sacredFlowerPositionsByPlayerName.put(
                        playerName,
                        saved.sacredFlowerPosition.toVector3d()
                );
            }

            if (saved.monsterPositions != null && !saved.monsterPositions.isEmpty()) {
                List<Vector3d> positions = new ArrayList<>();

                for (TrialPersistence.PersistentVector3d savedPosition : saved.monsterPositions) {
                    if (savedPosition != null) {
                        positions.add(savedPosition.toVector3d());
                    }
                }

                spawnedTrialMonsterPositionsByPlayerName.put(playerName, positions);
            }

            /*
             * If a trial was saved as ongoing/completed, it means the server stopped
             * before normal cleanup finished. Mark it for cleanup on next join.
             */
            if (saved.phase != null
                    && !TrialPhase.NONE.name().equals(saved.phase)
                    && !TrialPhase.REFUSED.name().equals(saved.phase)) {
                saved.monsterCleanupPending = true;
                saved.phase = TrialPhase.NONE.name();
                changed = true;
            }
        }

        if (changed) {
            TrialPersistence.save(persistentFile);
        }
    }

    private void savePersistentState(String playerName) {
        TrialState state = getOrCreateState(playerName);

        TrialPersistence.PersistentPlayerTrial saved =
                persistentFile.players.computeIfAbsent(
                        playerName,
                        ignored -> new TrialPersistence.PersistentPlayerTrial()
                );

        saved.phase = state.getPhase().name();

        saved.sacredFlowerPosition = TrialPersistence.PersistentVector3d.from(
                sacredFlowerPositionsByPlayerName.get(playerName)
        );

        List<Vector3d> monsterPositions =
                spawnedTrialMonsterPositionsByPlayerName.getOrDefault(playerName, List.of());

        saved.monsterPositions = new ArrayList<>();

        for (Vector3d position : monsterPositions) {
            TrialPersistence.PersistentVector3d savedPosition =
                    TrialPersistence.PersistentVector3d.from(position);

            if (savedPosition != null) {
                saved.monsterPositions.add(savedPosition);
            }
        }

        TrialPersistence.save(persistentFile);
    }

    private void removePersistentState(String playerName) {
        persistentFile.players.remove(playerName);
        TrialPersistence.save(persistentFile);
    }

    public TrialState getOrCreateState(String playerName) {
        return statesByPlayerName.computeIfAbsent(playerName, TrialState::new);
    }

    public TrialState getState(String playerName) {
        return statesByPlayerName.get(playerName);
    }

    public TrialPhase getPhase(String playerName) {
        TrialState state = getState(playerName);
        return state == null ? TrialPhase.NONE : state.getPhase();
    }

    public TrialResult offerTrial(String playerName) {
        TrialState state = getOrCreateState(playerName);

        if (state.getPhase() == TrialPhase.ACTIVE) {
            return new TrialResult(false, "You are already inside the Goddess' trial.");
        }

        if (state.getPhase() == TrialPhase.OFFERED) {
            return new TrialResult(false, "The Goddess is already awaiting your answer.");
        }

        state.setPhase(TrialPhase.OFFERED);
        savePersistentState(playerName);

        return new TrialResult(true, "The Goddess offers you a trial.");
    }

    public TrialResult acceptTrial(String playerName) {
        TrialState state = getOrCreateState(playerName);

        if (state.getPhase() != TrialPhase.OFFERED) {
            return new TrialResult(false, "No trial has been offered.");
        }

        state.setPhase(TrialPhase.ACTIVE);
        savePersistentState(playerName);

        return new TrialResult(true, "You accepted the Trial of the Goddess.");
    }

    public TrialResult refuseTrial(String playerName) {
        TrialState state = getOrCreateState(playerName);

        if (state.getPhase() != TrialPhase.OFFERED) {
            return new TrialResult(false, "No trial has been offered.");
        }

        state.setPhase(TrialPhase.REFUSED);

        /*
         * Refusing does not need persistent cleanup data.
         */
        removePersistentState(playerName);

        return new TrialResult(true, "You refused the Goddess' trial.");
    }

    public TrialResult failTrialBecauseOfDeath(String playerName) {
        TrialState state = getState(playerName);

        if (state == null || state.getPhase() != TrialPhase.ACTIVE) {
            return new TrialResult(false, "Player died, but no active trial was running.");
        }

        state.setPhase(TrialPhase.NONE);
        markMonsterCleanupPending(playerName);
        savePersistentState(playerName);

        return new TrialResult(true, "You died. The Trial of the Goddess has been reset.");
    }

    public TrialResult completeTrial(String playerName) {
        TrialState state = getOrCreateState(playerName);

        if (state.getPhase() != TrialPhase.ACTIVE) {
            return new TrialResult(false, "No active trial can be completed.");
        }

        state.setPhase(TrialPhase.COMPLETED);
        sacredFlowerPositionsByPlayerName.remove(playerName);

        /*
         * Keep persistent state until cleanup removes old monsters.
         */
        markMonsterCleanupPending(playerName);
        savePersistentState(playerName);

        return new TrialResult(true, "The Trial of the Goddess has been completed.");
    }

    public TrialResult resetTrial(String playerName) {
        TrialState state = getOrCreateState(playerName);
        state.setPhase(TrialPhase.NONE);

        spawnedTrialMonstersByPlayerName.remove(playerName);
        spawnedTrialMonsterPositionsByPlayerName.remove(playerName);
        sacredFlowerPositionsByPlayerName.remove(playerName);

        removePersistentState(playerName);

        return new TrialResult(true, "Your trial state has been reset.");
    }

    public TrialResult resetTrialOnJoin(String playerName) {
        return resetTrial(playerName);
    }

    public void finishCleanupAndRemovePersistentState(String playerName) {
        spawnedTrialMonstersByPlayerName.remove(playerName);
        spawnedTrialMonsterPositionsByPlayerName.remove(playerName);
        sacredFlowerPositionsByPlayerName.remove(playerName);

        TrialState state = getOrCreateState(playerName);
        state.setPhase(TrialPhase.NONE);

        removePersistentState(playerName);
    }

    public void failAllOngoingTrialsBecauseServerIsStopping() {
        boolean changed = false;

        for (Map.Entry<String, TrialState> entry : statesByPlayerName.entrySet()) {
            String playerName = entry.getKey();
            TrialState state = entry.getValue();

            if (playerName == null || state == null) {
                continue;
            }

            TrialPhase phase = state.getPhase();

            if (phase == TrialPhase.ACTIVE
                    || phase == TrialPhase.OFFERED
                    || phase == TrialPhase.COMPLETED) {

                state.setPhase(TrialPhase.NONE);

                TrialPersistence.PersistentPlayerTrial saved =
                        persistentFile.players.computeIfAbsent(
                                playerName,
                                ignored -> new TrialPersistence.PersistentPlayerTrial()
                        );

                saved.phase = TrialPhase.NONE.name();
                saved.monsterCleanupPending = true;

                saved.sacredFlowerPosition = TrialPersistence.PersistentVector3d.from(
                        sacredFlowerPositionsByPlayerName.get(playerName)
                );

                List<Vector3d> monsterPositions =
                        spawnedTrialMonsterPositionsByPlayerName.getOrDefault(playerName, List.of());

                saved.monsterPositions = new ArrayList<>();

                for (Vector3d position : monsterPositions) {
                    TrialPersistence.PersistentVector3d savedPosition =
                            TrialPersistence.PersistentVector3d.from(position);

                    if (savedPosition != null) {
                        saved.monsterPositions.add(savedPosition);
                    }
                }

                changed = true;
            }
        }

        if (changed) {
            TrialPersistence.save(persistentFile);
            System.out.println("[GoddessTrial] Server stopping: ongoing trials marked as failed for cleanup on next start.");
        }
    }

    public void rememberSpawnedTrialMonster(
            String playerName,
            Ref<EntityStore> monsterRef
    ) {
        if (playerName == null || monsterRef == null) {
            return;
        }

        spawnedTrialMonstersByPlayerName
                .computeIfAbsent(playerName, ignored -> new ArrayList<>())
                .add(monsterRef);
    }

    public List<Ref<EntityStore>> consumeSpawnedTrialMonsters(String playerName) {
        List<Ref<EntityStore>> refs =
                spawnedTrialMonstersByPlayerName.remove(playerName);

        return refs == null ? List.of() : refs;
    }

    public void rememberSpawnedTrialMonsterPosition(String playerName, Vector3d position) {
        if (playerName == null || position == null) {
            return;
        }

        spawnedTrialMonsterPositionsByPlayerName
                .computeIfAbsent(playerName, ignored -> new ArrayList<>())
                .add(position);

        savePersistentState(playerName);
    }

    public List<Vector3d> getSpawnedTrialMonsterPositions(String playerName) {
        return spawnedTrialMonsterPositionsByPlayerName.getOrDefault(playerName, List.of());
    }

    public List<Vector3d> consumeSpawnedTrialMonsterPositions(String playerName) {
        List<Vector3d> positions =
                spawnedTrialMonsterPositionsByPlayerName.remove(playerName);

        savePersistentState(playerName);

        return positions == null ? List.of() : positions;
    }

    public void rememberSacredFlowerPosition(String playerName, Vector3d position) {
        if (playerName == null || position == null) {
            return;
        }

        sacredFlowerPositionsByPlayerName.put(playerName, position);
        savePersistentState(playerName);
    }

    public Vector3d getSacredFlowerPosition(String playerName) {
        return sacredFlowerPositionsByPlayerName.get(playerName);
    }

    public Vector3d consumeSacredFlowerPosition(String playerName) {
        Vector3d position = sacredFlowerPositionsByPlayerName.remove(playerName);
        savePersistentState(playerName);
        return position;
    }

    public void clearSacredFlowerPosition(String playerName) {
        sacredFlowerPositionsByPlayerName.remove(playerName);
        savePersistentState(playerName);
    }

    public void markMonsterCleanupPending(String playerName) {
        TrialPersistence.PersistentPlayerTrial saved =
                persistentFile.players.computeIfAbsent(
                        playerName,
                        ignored -> new TrialPersistence.PersistentPlayerTrial()
                );

        saved.monsterCleanupPending = true;
        TrialPersistence.save(persistentFile);
    }

    public boolean isMonsterCleanupPending(String playerName) {
        TrialPersistence.PersistentPlayerTrial saved = persistentFile.players.get(playerName);
        return saved != null && saved.monsterCleanupPending;
    }

    public void clearMonsterCleanupPending(String playerName) {
        TrialPersistence.PersistentPlayerTrial saved = persistentFile.players.get(playerName);

        if (saved != null) {
            saved.monsterCleanupPending = false;
            TrialPersistence.save(persistentFile);
        }
    }

    public record TrialResult(boolean success, String message) {}
}