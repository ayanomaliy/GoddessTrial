package com.example.goddesstrial.trial;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Small helper for GoddessTrial custom sounds.
 */
public final class GoddessSoundUtil {

    private static final String GODDESS_SOUND_EVENT_ID = "SFX_GoddessTrial_Goddess";
    private static final String BLADE_SOUND_EVENT_ID = "SFX_GoddessTrial_Blade";

    private GoddessSoundUtil() {
    }

    public static void playGoddessVoice(
            Ref<EntityStore> playerEntityRef,
            Store<EntityStore> store
    ) {
        play2dSoundToPlayer(
                playerEntityRef,
                store,
                GODDESS_SOUND_EVENT_ID,
                1.0f,
                1.0f
        );
    }

    public static void playBladeReceived(
            Ref<EntityStore> playerEntityRef,
            Store<EntityStore> store
    ) {
        play2dSoundToPlayer(
                playerEntityRef,
                store,
                BLADE_SOUND_EVENT_ID,
                1.0f,
                1.0f
        );
    }

    private static void play2dSoundToPlayer(
            Ref<EntityStore> playerEntityRef,
            Store<EntityStore> store,
            String soundEventId,
            float volumeModifier,
            float pitchModifier
    ) {
        if (playerEntityRef == null || !playerEntityRef.isValid()) {
            System.out.println("[GoddessTrial] Could not play sound " + soundEventId + ": invalid player ref.");
            return;
        }

        try {
            int soundEventIndex = SoundEvent.getAssetMap().getIndex(soundEventId);

            SoundUtil.playSoundEvent2d(
                    playerEntityRef,
                    soundEventIndex,
                    SoundCategory.SFX,
                    volumeModifier,
                    pitchModifier,
                    store
            );

            System.out.println("[GoddessTrial] Played sound event: " + soundEventId);
        } catch (Exception e) {
            System.out.println("[GoddessTrial] Failed to play sound event: " + soundEventId);
            e.printStackTrace();
        }
    }
}