package com.example.goddesstrial.listeners;

import com.example.goddesstrial.trial.GoddessDialogueScript;
import com.example.goddesstrial.trial.GoddessSoundUtil;
import com.example.goddesstrial.ui.TrialOfferPage;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Runs the Goddess statue dialogue over time.
 *
 * Flow:
 * - intro appears in chat, line by line
 * - goddess dialogue appears as Event Title overlay
 * - every goddess page plays the goddess voice sound
 * - after the final goddess page, TrialOfferPage opens
 */
public class GoddessDialogueSequenceSystem extends EntityTickingSystem<EntityStore> {

    private static final float INTRO_LINE_DELAY_SECONDS = 1.4f;
    private static final float GODDESS_PAGE_DELAY_SECONDS = 3.0f;

    private static final float TITLE_FADE_IN_SECONDS = 0.35f;
    private static final float TITLE_DURATION_SECONDS = 2.30f;
    private static final float TITLE_FADE_OUT_SECONDS = 0.35f;

    private static final Map<String, DialogueState> STATES_BY_PLAYER_NAME = new HashMap<>();

    public static void startDialogue(String playerName) {
        STATES_BY_PLAYER_NAME.put(playerName, new DialogueState());
    }

    public static void cancelDialogue(String playerName) {
        STATES_BY_PLAYER_NAME.remove(playerName);
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
        DialogueState state = STATES_BY_PLAYER_NAME.get(playerName);

        if (state == null) {
            return;
        }

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        Player player = archetypeChunk.getComponent(index, Player.getComponentType());

        if (player == null) {
            STATES_BY_PLAYER_NAME.remove(playerName);
            return;
        }

        state.elapsedSeconds += dt;

        if (state.stage == DialogueStage.INTRO) {
            tickIntro(state, player);
            return;
        }

        if (state.stage == DialogueStage.GODDESS) {
            tickGoddess(state, playerName, playerRef, player, ref, store);
        }
    }

    private void tickIntro(
            DialogueState state,
            Player player
    ) {
        if (state.elapsedSeconds < INTRO_LINE_DELAY_SECONDS) {
            return;
        }

        state.elapsedSeconds = 0.0f;

        if (state.pageIndex >= GoddessDialogueScript.INTRO_CHAT_LINES.length) {
            state.stage = DialogueStage.GODDESS;
            state.pageIndex = 0;
            state.elapsedSeconds = GODDESS_PAGE_DELAY_SECONDS;
            return;
        }

        player.sendMessage(Message.raw(
                GoddessDialogueScript.INTRO_CHAT_LINES[state.pageIndex]
        ));

        state.pageIndex++;
    }

    private void tickGoddess(
            DialogueState state,
            String playerName,
            PlayerRef playerRef,
            Player player,
            Ref<EntityStore> ref,
            Store<EntityStore> store
    ) {
        if (state.elapsedSeconds < GODDESS_PAGE_DELAY_SECONDS) {
            return;
        }

        state.elapsedSeconds = 0.0f;

        if (state.pageIndex >= GoddessDialogueScript.GODDESS_PAGES.length) {
            EventTitleUtil.hideEventTitleFromPlayer(playerRef, 0.4f);

            player.getPageManager().openCustomPage(
                    ref,
                    store,
                    new TrialOfferPage(playerRef)
            );

            STATES_BY_PLAYER_NAME.remove(playerName);
            return;
        }

        GoddessDialogueScript.GoddessDialoguePage page =
                GoddessDialogueScript.GODDESS_PAGES[state.pageIndex];

        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw(page.title()),
                Message.raw(page.subtitle()),
                true,
                EventTitleUtil.DEFAULT_ZONE,
                TITLE_FADE_IN_SECONDS,
                TITLE_DURATION_SECONDS,
                TITLE_FADE_OUT_SECONDS
        );

        GoddessSoundUtil.playGoddessVoice(ref, store);

        state.pageIndex++;
    }

    private enum DialogueStage {
        INTRO,
        GODDESS
    }

    private static final class DialogueState {
        private DialogueStage stage = DialogueStage.INTRO;
        private int pageIndex = 0;
        private float elapsedSeconds = INTRO_LINE_DELAY_SECONDS;
    }

    public static boolean isDialoguePlaying(String playerName) {
        return STATES_BY_PLAYER_NAME.containsKey(playerName);
    }

    public static void skipDialogueToOfferUi(
            String playerName,
            PlayerRef playerRef,
            Player player,
            Ref<EntityStore> ref,
            Store<EntityStore> store
    ) {
        STATES_BY_PLAYER_NAME.remove(playerName);

        EventTitleUtil.hideEventTitleFromPlayer(playerRef, 0.2f);

        player.getPageManager().openCustomPage(
                ref,
                store,
                new TrialOfferPage(playerRef)
        );
    }
}