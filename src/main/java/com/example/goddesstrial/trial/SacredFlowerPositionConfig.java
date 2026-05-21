package com.example.goddesstrial.trial;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.hypixel.hytale.math.vector.Vector3d;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class SacredFlowerPositionConfig {

    private static final String RESOURCE_PATH =
            "/Server/Item/Items/GoddessTrial/SacredFlowerPositions.json";

    private static final Random RANDOM = new Random();
    private static final Gson GSON = new Gson();

    private static FlowerPositionsFile cachedFile;

    private SacredFlowerPositionConfig() {
    }

    public static Vector3d pickRandomPosition() {
        List<ConfiguredFlowerPosition> positions = getShuffledPositions();

        if (positions.isEmpty()) {
            return null;
        }

        return positions.getFirst().position();
    }

    public static List<ConfiguredFlowerPosition> getShuffledPositions() {
        FlowerPositionsFile file = load();

        if (file == null || file.positions == null || file.positions.isEmpty()) {
            System.out.println("[GoddessTrial] No Sacred Flower positions configured.");
            return List.of();
        }

        List<ConfiguredFlowerPosition> positions = new ArrayList<>();

        for (FlowerPosition position : file.positions) {
            positions.add(new ConfiguredFlowerPosition(
                    position.name,
                    new Vector3d(position.x, position.y, position.z)
            ));
        }

        Collections.shuffle(positions, RANDOM);
        return positions;
    }

    private static FlowerPositionsFile load() {
        if (cachedFile != null) {
            return cachedFile;
        }

        try (InputStream stream = SacredFlowerPositionConfig.class.getResourceAsStream(RESOURCE_PATH)) {
            if (stream == null) {
                System.out.println("[GoddessTrial] Could not find " + RESOURCE_PATH);
                return null;
            }

            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                cachedFile = GSON.fromJson(reader, FlowerPositionsFile.class);
                return cachedFile;
            }
        } catch (Exception e) {
            System.out.println("[GoddessTrial] Failed to load Sacred Flower position config.");
            e.printStackTrace();
            return null;
        }
    }

    public record ConfiguredFlowerPosition(String name, Vector3d position) {}

    private static final class FlowerPositionsFile {
        @SerializedName("Positions")
        private List<FlowerPosition> positions;
    }

    private static final class FlowerPosition {
        @SerializedName("Name")
        private String name;

        @SerializedName("X")
        private int x;

        @SerializedName("Y")
        private int y;

        @SerializedName("Z")
        private int z;
    }
}