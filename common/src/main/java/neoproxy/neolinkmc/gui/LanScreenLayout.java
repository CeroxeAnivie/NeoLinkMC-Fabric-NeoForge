package neoproxy.neolinkmc.gui;

/**
 * Shared geometry for NeoLinkMC's LAN configuration screen.
 *
 * <p>The Minecraft LAN screen convention anchors the main option rows to a
 * stable top area while keeping the port row near the bottom edge. Keeping this
 * calculation in common code prevents version modules from drifting into
 * different resize behavior.</p>
 */
public final class LanScreenLayout {
    public static final int BUTTON_WIDTH = 150;
    public static final int BUTTON_HEIGHT = 20;
    public static final int INPUT_WIDTH = 147;

    private static final int LEFT_COLUMN_OFFSET = -155;
    private static final int RIGHT_COLUMN_OFFSET = 5;
    private static final int TOP_TITLE_Y = 50;
    private static final int TOP_ROW_Y = 74;
    private static final int PLAYER_SETTINGS_Y = 132;
    private static final int FIRST_OPTION_ROW_Y = 164;
    private static final int SECOND_OPTION_ROW_Y = 188;
    private static final int BOTTOM_LABEL_OFFSET = 66;
    private static final int BOTTOM_INPUT_OFFSET = 54;
    private static final int BOTTOM_BUTTON_OFFSET = 28;
    private static final int COMPACT_INPUT_LABEL_GAP = 16;
    private static final int COMPACT_INPUT_ROW_GAP = 12;
    private static final int COMPACT_BOTTOM_ROW_GAP = 12;

    private LanScreenLayout() {
    }

    public static Positions calculate(int screenWidth, int screenHeight) {
        int centerX = screenWidth / 2;
        int leftColumnX = centerX + LEFT_COLUMN_OFFSET;
        int rightColumnX = centerX + RIGHT_COLUMN_OFFSET;
        int inputLabelY = screenHeight - BOTTOM_LABEL_OFFSET;
        int inputRowY = screenHeight - BOTTOM_INPUT_OFFSET;
        int bottomRowY = screenHeight - BOTTOM_BUTTON_OFFSET;

        int minimumInputLabelY = SECOND_OPTION_ROW_Y + BUTTON_HEIGHT + COMPACT_INPUT_LABEL_GAP;
        if (inputLabelY < minimumInputLabelY) {
            inputLabelY = minimumInputLabelY;
            inputRowY = inputLabelY + COMPACT_INPUT_ROW_GAP;
            bottomRowY = inputRowY + BUTTON_HEIGHT + COMPACT_BOTTOM_ROW_GAP;
        }

        return new Positions(
                centerX,
                leftColumnX,
                rightColumnX,
                TOP_TITLE_Y,
                TOP_ROW_Y,
                PLAYER_SETTINGS_Y,
                FIRST_OPTION_ROW_Y,
                SECOND_OPTION_ROW_Y,
                inputLabelY,
                inputRowY,
                bottomRowY
        );
    }

    public record Positions(
            int centerX,
            int leftColumnX,
            int rightColumnX,
            int titleY,
            int topRowY,
            int playerSettingsY,
            int firstOptionRowY,
            int secondOptionRowY,
            int inputLabelY,
            int inputRowY,
            int bottomRowY
    ) {
    }
}
