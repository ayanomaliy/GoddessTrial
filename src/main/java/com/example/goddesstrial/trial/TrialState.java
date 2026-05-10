package com.example.goddesstrial.trial;

public class TrialState {

    private final String playerName;
    private TrialPhase phase;
    private long lastUpdatedMillis;

    public TrialState(String playerName) {
        this.playerName = playerName;
        this.phase = TrialPhase.NONE;
        this.lastUpdatedMillis = System.currentTimeMillis();
    }

    public String getPlayerName() {
        return playerName;
    }

    public TrialPhase getPhase() {
        return phase;
    }

    public void setPhase(TrialPhase phase) {
        this.phase = phase;
        this.lastUpdatedMillis = System.currentTimeMillis();
    }

    public long getLastUpdatedMillis() {
        return lastUpdatedMillis;
    }
}