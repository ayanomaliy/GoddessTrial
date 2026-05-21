package com.example.goddesstrial.ui;

import com.example.goddesstrial.GoddessTrialPlugin;
import com.example.goddesstrial.trial.GoddessDialogueScript;
import com.example.goddesstrial.trial.GoddessSoundUtil;
import com.example.goddesstrial.trial.TrialManager;
import com.example.goddesstrial.trial.TrialStarter;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.example.goddesstrial.listeners.GoddessChoiceSequenceSystem;

import javax.annotation.Nonnull;

/**
 * GUI shown after interacting with the Statue of a Slumbering Deity.
 */
public class TrialOfferPage extends InteractiveCustomUIPage<TrialOfferPage.UIEventData> {

    public static final String LAYOUT = "goddesstrial/TrialOffer.ui";

    private static final float TITLE_FADE_IN_SECONDS = 0.5f;
    private static final float TITLE_DURATION_SECONDS = 2.8f;
    private static final float TITLE_FADE_OUT_SECONDS = 0.5f;

    private final PlayerRef playerRef;

    public TrialOfferPage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, UIEventData.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder evt,
            @Nonnull Store<EntityStore> store
    ) {
        cmd.append(LAYOUT);

        evt.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AcceptButton",
                new EventData().append("Action", "accept"),
                false
        );

        evt.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#RefuseButton",
                new EventData().append("Action", "refuse"),
                false
        );
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull UIEventData data
    ) {
        if (data.action == null) {
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) {
            close();
            return;
        }

        GoddessTrialPlugin plugin = GoddessTrialPlugin.getInstance();

        if (plugin == null) {
            player.sendMessage(Message.raw("The statue remains silent."));
            close();
            return;
        }

        String playerName = playerRef.getUsername();
        TrialManager trialManager = plugin.getTrialManager();

        switch (data.action) {
            case "accept":
                TrialStarter.TrialStartResult startResult =
                        TrialStarter.startTrial(
                                trialManager,
                                playerName,
                                player,
                                ref,
                                store
                        );

                player.sendMessage(Message.raw(startResult.message()));

                if (startResult.success()) {
                    GoddessChoiceSequenceSystem.startAcceptedDialogue(playerName);
                }

                close();
                break;

            case "refuse":
                TrialManager.TrialResult refuseResult =
                        trialManager.refuseTrial(playerName);

                player.sendMessage(Message.raw(refuseResult.message()));

                if (refuseResult.success()) {
                    GoddessChoiceSequenceSystem.startRefusedDialogue(playerName);
                }

                close();
                break;

            default:
                close();
                break;
        }
    }

    private void showGoddessTitle(
            GoddessDialogueScript.GoddessDialoguePage page,
            Ref<EntityStore> ref,
            Store<EntityStore> store
    ) {
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
    }

    /**
     * Data sent by UI button events.
     */
    public static class UIEventData {
        public static final BuilderCodec<UIEventData> CODEC = BuilderCodec.builder(
                        UIEventData.class,
                        UIEventData::new
                )
                .append(new KeyedCodec<>("Action", Codec.STRING), (e, v) -> e.action = v, e -> e.action)
                .add()
                .build();

        private String action;

        public UIEventData() {
        }
    }
}