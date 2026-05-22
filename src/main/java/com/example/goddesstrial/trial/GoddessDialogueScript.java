package com.example.goddesstrial.trial;

/**
 * Central place for editing the Goddess statue dialogue.
 */
public final class GoddessDialogueScript {

    private GoddessDialogueScript() {
    }

    public static final String[] INTRO_CHAT_LINES = {
            "You place your hand upon the cold stone. For a moment, nothing happens. Then warmth spreads through your fingers. Not fire. Not sunlight. Something older.",
            "A voice unfolds inside your mind."
    };

    public static final GoddessDialoguePage[] GODDESS_PAGES = {
            page("Little wanderer... do not be afraid."),

            page("I am only what remains of an old goddess."),

            page("My name has faded with the prayers of this world."),

            page("My soul is trapped beyond the veil."),

            page("I have accepted that I will disappear."),

            page("But I cannot leave this world incomplete."),

            page("Beyond the veil lies the Sacred Flower."),

            page("It is the last piece of myself."),

            page("Bring it to me, and I may finally slumber whole."),

            page("Take my weapon, and cross the veil."),

    };

    public static final String[] COMPLETION_CHAT_LINES = {
            "The Sacred Flower trembles in your hands. Its light sinks into the cold stone."
    };

    public static final GoddessDialoguePage[] COMPLETION_PAGES = {
            page("Finally... I am whole again."),

            page("Thank you, little wanderer."),

            page("I have no need for the blade anymore."),

            page("Keep it, as a sign of my gratitude."),

            page("Now... I may slumber. Farewell...")
    };

    public static final String[] COMPLETION_AFTER_CHAT_LINES = {
            "The warmth fades. The voice is gone. The statue is only stone again.",
            "The Trial of the Goddess is complete."
    };
    public static final GoddessDialoguePage[] ACCEPTED_PAGES = {
            page("Then take my blade."),
            page("Cross the veil, and bring back what was once mine.")
    };

    public static final GoddessDialoguePage[] REFUSED_PAGES = {
            page("Then leave me to my silence.")
    };

    public static final String[] REFUSED_AFTER_CHAT_LINES = {
            "The stone grows cold beneath your hand."
    };

    public static final GoddessDialoguePage[] RETURN_WITHOUT_FLOWER_PAGES = {
            page("The Sacred Flower is not with you."),
            page("Find it and bring it to me..."),
            page("So I can be complete once again")
    };

    private static GoddessDialoguePage page(String title) {
        return new GoddessDialoguePage(title, "");
    }

    public record GoddessDialoguePage(String title, String subtitle) {}
}