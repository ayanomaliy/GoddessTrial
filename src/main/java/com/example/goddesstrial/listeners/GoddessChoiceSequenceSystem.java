package com.example.goddesstrial.listeners;

import com.example.goddesstrial.trial.GoddessDialogueScript;
import com.example.goddesstrial.trial.GoddessSoundUtil;
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
 * Shows short Goddess title responses after the player accepts/refuses the trial.
 */
public class GoddessChoiceSequenceSystem extends EntityTickingSystem<EntityStore> {

    private static final float PAGE_DELAY_SECONDS = 3.0f;

    private static final float TITLE_FADE_IN_SECONDS = 0.5f;
    private static final float TITLE_DURATION_SECONDS = 2.0f;
    private static final float TITLE_FADE_OUT_SECONDS = 0.5f;

    private static final Map<String, ChoiceState> STATES_BY_PLAYER_NAME = new HashMap<>();

    public static void startAcceptedDialogue(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return;
        }

        STATES_BY_PLAYER_NAME.put(
                playerName,
                new ChoiceState(GoddessDialogueScript.ACCEPTED_PAGES, null)
        );
    }

    public static void startRefusedDialogue(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return;
        }

        STATES_BY_PLAYER_NAME.put(
                playerName,
                new ChoiceState(
                        GoddessDialogueScript.REFUSED_PAGES,
                        GoddessDialogueScript.REFUSED_AFTER_CHAT_LINES
                )
        );
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
        ChoiceState state = STATES_BY_PLAYER_NAME.get(playerName);

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

        if (state.elapsedSeconds < PAGE_DELAY_SECONDS) {
            return;
        }

        state.elapsedSeconds = 0.0f;

        if (state.pageIndex >= state.pages.length) {
            EventTitleUtil.hideEventTitleFromPlayer(playerRef, 0.3f);

            if (state.afterChatLines != null) {
                player.sendMessage(Message.raw(""));

                for (String line : state.afterChatLines) {
                    player.sendMessage(Message.raw(line));
                }
            }

            STATES_BY_PLAYER_NAME.remove(playerName);
            return;
        }

        GoddessDialogueScript.GoddessDialoguePage page = state.pages[state.pageIndex];

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

    private static final class ChoiceState {
        private final GoddessDialogueScript.GoddessDialoguePage[] pages;
        private final String[] afterChatLines;

        private int pageIndex = 0;
        private float elapsedSeconds = PAGE_DELAY_SECONDS;

        private ChoiceState(
                GoddessDialogueScript.GoddessDialoguePage[] pages,
                String[] afterChatLines
        ) {
            this.pages = pages;
            this.afterChatLines = afterChatLines;
        }
    }
}