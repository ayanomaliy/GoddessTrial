package com.example.goddesstrial.trial;

import java.util.HashMap;
import java.util.Map;

public class TrialManager {

    private final Map<String, TrialState> statesByPlayerName = new HashMap<>();

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
            return new TrialResult(false, "The Goddess is already awaiting your answer. Use /trial accept or /trial refuse.");
        }

        state.setPhase(TrialPhase.OFFERED);
        return new TrialResult(true, "The Goddess offers you a trial. Use /trial accept or /trial refuse.");
    }

    public TrialResult acceptTrial(String playerName) {
        TrialState state = getOrCreateState(playerName);

        if (state.getPhase() != TrialPhase.OFFERED) {
            return new TrialResult(false, "No trial has been offered. Use /trial start first.");
        }

        state.setPhase(TrialPhase.ACTIVE);
        return new TrialResult(true, "You accepted the Trial of the Goddess. The Blade of Balance will be granted soon.");
    }

    public TrialResult refuseTrial(String playerName) {
        TrialState state = getOrCreateState(playerName);

        if (state.getPhase() != TrialPhase.OFFERED) {
            return new TrialResult(false, "No trial has been offered. Use /trial start first.");
        }

        state.setPhase(TrialPhase.REFUSED);
        return new TrialResult(true, "You refused the Goddess' trial.");
    }

    public TrialResult resetTrial(String playerName) {
        TrialState state = getOrCreateState(playerName);
        state.setPhase(TrialPhase.NONE);
        return new TrialResult(true, "Your trial state has been reset.");
    }

    public record TrialResult(boolean success, String message) {}

    public TrialResult failTrialBecauseOfDeath(String playerName) {
        TrialState state = getState(playerName);

        if (state == null || state.getPhase() != TrialPhase.ACTIVE) {
            return new TrialResult(false, "Player died, but no active trial was running.");
        }

        // Reset completely so the player must start and accept again.
        state.setPhase(TrialPhase.NONE);

        return new TrialResult(true, "You died. The Trial of the Goddess has been reset.");
    }

    public TrialResult resetTrialOnJoin(String playerName) {
        TrialState state = getOrCreateState(playerName);
        state.setPhase(TrialPhase.NONE);
        return new TrialResult(true, "Trial state reset on join.");
    }
}