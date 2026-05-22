package com.example.goddesstrial.trial;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Objective;
import com.hypixel.hytale.protocol.ObjectiveTask;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.assets.TrackOrUpdateObjective;
import com.hypixel.hytale.protocol.packets.assets.UntrackObjective;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Client-side objective tracker for the Trial of the Goddess.
 *
 * Simplified version:
 * - one static objective for the whole trial
 * - no unreliable mid-trial objective update
 * - one task line with the full instruction
 */
public final class TrialObjectiveTracker {

    private static final String OBJECTIVE_LINE_ID = "GoddessTrial";

    private TrialObjectiveTracker() {
    }

    public static void showFindFlowerObjective(
            PlayerRef playerRef,
            Vector3d flowerPosition
    ) {
        if (playerRef == null) {
            return;
        }

        clearObjective(playerRef);

        UUID objectiveUuid = objectiveUuidFor(playerRef);

        ObjectiveTask task = new ObjectiveTask(
                Message.raw("Find the Sacred Flower nearby and return it to the statue.").getFormattedMessage(),

                /*
                 * Try 0/0 instead of 0/1.
                 * In this UI, 0/1 creates the visible counter. 0/0 may be treated
                 * as a plain task without progress.
                 */
                0,
                0
        );

        Objective objective = new Objective(
                objectiveUuid,
                Message.raw("Trial of the Goddess").getFormattedMessage(),
                Message.raw("").getFormattedMessage(),
                OBJECTIVE_LINE_ID,
                new ObjectiveTask[]{task}
        );

        sendPacket(playerRef, new TrackOrUpdateObjective(objective));

        System.out.println(
                "[GoddessTrial] Objective shown for "
                        + playerRef.getUsername()
                        + ": find flower and return it."
        );
    }

    /**
     * Kept so BlockInspectListener can still call it without breaking.
     * We intentionally do nothing here because the objective already contains
     * the full instruction from the beginning.
     */
    public static void showReturnToStatueObjective(PlayerRef playerRef) {
        if (playerRef == null) {
            return;
        }

        System.out.println(
                "[GoddessTrial] Sacred Flower collected by "
                        + playerRef.getUsername()
                        + ". Objective already says to return it."
        );
    }

    public static void markCompletedAndClear(PlayerRef playerRef) {
        clearObjective(playerRef);

        if (playerRef != null) {
            System.out.println(
                    "[GoddessTrial] Objective completed and cleared for "
                            + playerRef.getUsername()
            );
        }
    }

    public static void markFailedAndClear(PlayerRef playerRef) {
        clearObjective(playerRef);

        if (playerRef != null) {
            System.out.println(
                    "[GoddessTrial] Objective failed and cleared for "
                            + playerRef.getUsername()
            );
        }
    }

    public static void clearObjective(PlayerRef playerRef) {
        if (playerRef == null) {
            return;
        }

        sendPacket(playerRef, new UntrackObjective(objectiveUuidFor(playerRef)));

        System.out.println(
                "[GoddessTrial] Objective cleared for "
                        + playerRef.getUsername()
        );
    }

    private static UUID objectiveUuidFor(PlayerRef playerRef) {
        String source = "GoddessTrial:objective:" + playerRef.getUuid();

        return UUID.nameUUIDFromBytes(
                source.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static void sendPacket(PlayerRef playerRef, Packet packet) {
        if (playerRef == null || packet == null) {
            return;
        }

        Object packetHandler = playerRef.getPacketHandler();

        if (packetHandler == null) {
            return;
        }

        String[] methodNames = {
                "sendPacket",
                "send",
                "write",
                "queuePacket",
                "sendToClient"
        };

        for (String methodName : methodNames) {
            if (tryInvokePacketMethod(packetHandler, methodName, packet)) {
                return;
            }
        }

        for (Method method : packetHandler.getClass().getMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();

            if (parameterTypes.length != 1) {
                continue;
            }

            if (!parameterTypes[0].isAssignableFrom(packet.getClass())
                    && !parameterTypes[0].isAssignableFrom(Packet.class)) {
                continue;
            }

            try {
                method.invoke(packetHandler, packet);
                return;
            } catch (Exception ignored) {
                // Try next candidate.
            }
        }

        System.out.println(
                "[GoddessTrial] Could not send objective packet "
                        + packet.getClass().getSimpleName()
                        + ": no matching packet handler method found."
        );
    }

    private static boolean tryInvokePacketMethod(
            Object packetHandler,
            String methodName,
            Packet packet
    ) {
        for (Method method : packetHandler.getClass().getMethods()) {
            if (!methodName.equals(method.getName())) {
                continue;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();

            if (parameterTypes.length != 1) {
                continue;
            }

            if (!parameterTypes[0].isAssignableFrom(packet.getClass())
                    && !parameterTypes[0].isAssignableFrom(Packet.class)) {
                continue;
            }

            try {
                method.invoke(packetHandler, packet);
                return true;
            } catch (Exception ignored) {
                // Try next overload.
            }
        }

        return false;
    }
}