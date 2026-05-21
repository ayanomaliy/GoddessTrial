package com.example.goddesstrial.trial;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.math.vector.Vector3d;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TrialPersistence {

    /*
     * Server is started from the server/ folder.
     * This creates:
     * server/GoddessTrial/trial-state.json
     */
    private static final Path SAVE_DIR = Path.of("GoddessTrial");
    private static final Path SAVE_FILE = SAVE_DIR.resolve("trial-state.json");

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private TrialPersistence() {
    }

    public static PersistentTrialFile load() {
        try {
            if (!Files.exists(SAVE_FILE)) {
                return new PersistentTrialFile();
            }

            String json = Files.readString(SAVE_FILE, StandardCharsets.UTF_8);
            PersistentTrialFile file = GSON.fromJson(json, PersistentTrialFile.class);

            if (file == null) {
                return new PersistentTrialFile();
            }

            if (file.players == null) {
                file.players = new HashMap<>();
            }

            /*
             * Defensive cleanup for older save files.
             */
            for (PersistentPlayerTrial playerTrial : file.players.values()) {
                if (playerTrial != null && playerTrial.monsterPositions == null) {
                    playerTrial.monsterPositions = new ArrayList<>();
                }
            }

            return file;
        } catch (Exception e) {
            System.out.println("[GoddessTrial] Failed to load persistent trial state.");
            e.printStackTrace();
            return new PersistentTrialFile();
        }
    }

    public static void save(PersistentTrialFile file) {
        try {
            Files.createDirectories(SAVE_DIR);

            if (file.players == null) {
                file.players = new HashMap<>();
            }

            String json = GSON.toJson(file);
            Files.writeString(SAVE_FILE, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("[GoddessTrial] Failed to save persistent trial state.");
            e.printStackTrace();
        }
    }

    public static final class PersistentTrialFile {
        public Map<String, PersistentPlayerTrial> players = new HashMap<>();
    }

    public static final class PersistentPlayerTrial {
        public String phase = TrialPhase.NONE.name();
        public PersistentVector3d sacredFlowerPosition;

        /*
         * True when the player died and the server still has to purge trial monsters.
         * This must survive server restarts.
         */
        public boolean monsterCleanupPending = false;

        /*
         * Approximate spawn positions of trial monsters.
         * Used to remove them after death, even if the server restarted and entity refs were lost.
         */
        public List<PersistentVector3d> monsterPositions = new ArrayList<>();
    }

    public static final class PersistentVector3d {
        public double x;
        public double y;
        public double z;

        public static PersistentVector3d from(Vector3d vector) {
            if (vector == null) {
                return null;
            }

            PersistentVector3d saved = new PersistentVector3d();
            saved.x = vector.getX();
            saved.y = vector.getY();
            saved.z = vector.getZ();
            return saved;
        }

        public Vector3d toVector3d() {
            return new Vector3d(x, y, z);
        }
    }
}