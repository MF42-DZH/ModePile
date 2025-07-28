package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;

/**
 * A class for showing {@code ModePile}'s credits. Not for general use, but feel free to take
 * inspiration for creating your own credits-drawing classes.
 */
public class ModePileCredits {
    // Darkening filter for the credits.
    private static final int FILTER = 192;

    private static GameTextUtilities.TextBlockElement entrySep() {
        return GameTextUtilities.Text.blankLine(0.25f);
    }

    public static GameTextUtilities.TextBlockElement creditText(String string, int color, float scale) {
        return GameTextUtilities.textElems(
            GameTextUtilities.Text.customMixColor(string, color, FILTER, FILTER, FILTER, 255, scale),
            GameTextUtilities.Text.newLine(),
            entrySep()
        );
    }

    public static GameTextUtilities.TextBlockElement creditTextNoSp(String string, int color, float scale) {
        return GameTextUtilities.textElems(
            GameTextUtilities.Text.customMixColor(string, color, FILTER, FILTER, FILTER, 255, scale),
            GameTextUtilities.Text.newLine()
        );
    }

    private static final GameTextUtilities.TextBlock FINAL_BLOCK = GameTextUtilities.TextBlock.of(
        GameTextUtilities.TextJustification.CENTRE,
        creditText("MODE", EventReceiver.COLOR_GREEN, 2f),
        creditText("PILE", EventReceiver.COLOR_GREEN, 2f),
        creditText("PRESENTED BY", EventReceiver.COLOR_WHITE, 0.5f),
        creditText("AZULLIA, MANDL27", EventReceiver.COLOR_YELLOW, 0.625f),
        creditTextNoSp("NIGHTSHADE", EventReceiver.COLOR_YELLOW, 0.625f)
    );

    private final GameTextUtilities.TextBlock mainBlock;
    private final double finalBlockOffset;
    private final double finalBlockMoveProportion;

    /**
     * Construct a credits instance.
     *
     * @param mainBlockHeader      Put mode info here
     * @param creatorBlock         Put mode creator info here
     * @param congratulationsBlock Put congratulations message here
     * @param finalBlockOffset     Offset of credits progress for when the ModePile text block shows up
     * @param finalBlockProportion How fast the ModePile text block moves and how long it stays still (smaller = faster and sticks around longer before end of credits)
     * @param skipSpecialThanks    Skips the special thanks and shoutouts sections, used for short credits
     */
    public ModePileCredits(
        GameTextUtilities.TextBlockElement mainBlockHeader,
        GameTextUtilities.TextBlockElement creatorBlock,
        GameTextUtilities.TextBlockElement congratulationsBlock,
        double finalBlockOffset,
        double finalBlockProportion,
        boolean skipSpecialThanks
    ) {
        assert (finalBlockOffset > 0d && finalBlockOffset <= 1d);
        assert (finalBlockProportion > 0d && finalBlockProportion <= (1d - finalBlockOffset));

        this.finalBlockOffset = finalBlockOffset;
        this.finalBlockMoveProportion = finalBlockProportion;

        mainBlock = GameTextUtilities.TextBlock.of(
            GameTextUtilities.TextJustification.CENTRE,
            mainBlockHeader,
            GameTextUtilities.Text.blankLine(4f),
            creatorBlock,
            (skipSpecialThanks
                ? GameTextUtilities.Text.custom(" ", EventReceiver.COLOR_WHITE, 0f)
                : GameTextUtilities.textElems(
                    GameTextUtilities.Text.blankLine(4f),
                    creditTextNoSp("SPECIAL THANKS", EventReceiver.COLOR_YELLOW, 0.7f),
                    GameTextUtilities.Text.blankLine(1f),
                    creditText("THE OSHISAURES", EventReceiver.COLOR_PINK, 0.65f),
                    creditText("GLITCHYPSI", EventReceiver.COLOR_CYAN, 0.65f),
                    creditText("FARTERYHR", EventReceiver.COLOR_WHITE, 0.65f),
                    creditText("GRAV", EventReceiver.COLOR_PINK, 0.65f),
                    creditText("DM DOKURO", EventReceiver.COLOR_RED, 0.65f),
                    creditText("SIMPLEFLIPS", EventReceiver.COLOR_YELLOW, 0.65f),
                    creditText("ZAPPOOLA", EventReceiver.COLOR_PINK, 0.65f),
                    creditText("KINGSTATIC", EventReceiver.COLOR_BLUE, 0.65f),
                    creditText("TDGNERROTH", EventReceiver.COLOR_CYAN, 0.65f),
                    creditText("MRXBAS", EventReceiver.COLOR_YELLOW, 0.65f),
                    creditText("TEAKANJI", EventReceiver.COLOR_PURPLE, 0.65f),
                    creditText("VENTILO_", EventReceiver.COLOR_PURPLE, 0.65f),
                    creditText("ROMAJIMILTONAMULO", EventReceiver.COLOR_GREEN, 0.575f),
                    creditText("NICK666101", EventReceiver.COLOR_PURPLE, 0.65f),
                    creditText("LEIKAISHO", EventReceiver.COLOR_RED, 0.65f),
                    creditText("WHAMER100", EventReceiver.COLOR_PURPLE, 0.65f),
                    creditText("MARKGAMED7794", EventReceiver.COLOR_PURPLE, 0.65f),
                    creditText("MERP", EventReceiver.COLOR_BLUE, 0.65f),
                    creditText("TIM_THE_ENCHANTER", EventReceiver.COLOR_BLUE, 0.575f),
                    creditText("LEFALCHIZZLE", EventReceiver.COLOR_BLUE, 0.65f),
                    creditText("FATE", EventReceiver.COLOR_BLUE, 0.65f),
                    creditText("THEPROGUE", EventReceiver.COLOR_BLUE, 0.65f),
                    creditTextNoSp("RURURARURI", EventReceiver.COLOR_BLUE, 0.65f),
                    GameTextUtilities.Text.blankLine(1f),
                    creditTextNoSp("YOU!", EventReceiver.COLOR_ORANGE, 1.75f),
                    GameTextUtilities.Text.blankLine(4f),
                    creditTextNoSp("SHOUTOUTS TO", EventReceiver.COLOR_YELLOW, 0.7f),
                    GameTextUtilities.Text.blankLine(1f),
                    creditText("NULLNONAME", EventReceiver.COLOR_WHITE, 0.65f),
                    creditText("TETRIS.WIKI", EventReceiver.COLOR_WHITE, 0.65f),
                    creditText("HARD DROP WIKI", EventReceiver.COLOR_WHITE, 0.65f),
                    creditText("ALL DTET PLAYERS", EventReceiver.COLOR_WHITE, 0.625f),
                    creditText("ALL T-EX PLAYERS", EventReceiver.COLOR_WHITE, 0.625f),
                    creditText("ALL HEBORIS PLAYERS", EventReceiver.COLOR_WHITE, 0.525f),
                    creditText("ALL #GM SERIES PLAYERS", EventReceiver.COLOR_WHITE, 0.45f),
                    creditText("ALL NULLPOMINO PLAYERS", EventReceiver.COLOR_WHITE, 0.45f),
                    creditTextNoSp("ALL SEGATET '99 PLAYERS", EventReceiver.COLOR_WHITE, 0.43f)
                )
            ),
            GameTextUtilities.Text.blankLine(4f),
            congratulationsBlock,
            GameTextUtilities.Text.blankLine(4f),
            creditText("THANK YOU", EventReceiver.COLOR_WHITE, (10f / 13f)),
            creditText("FOR STICKING", EventReceiver.COLOR_WHITE, (10f / 13f)),
            creditText("WITH MODEPILE", EventReceiver.COLOR_WHITE, (10f / 13f)),
            creditText("FOR ALL THESE", EventReceiver.COLOR_WHITE, (10f / 13f)),
            creditTextNoSp("YEARS!", EventReceiver.COLOR_WHITE, (10f / 13f))
        );
    }

    /**
     * Draw the current credits at a progress level. Use this within the {@code renderFirst} method of a gamemode, between
     * drawing the frame and the field (see {@link zeroxfc.nullpo.custom.modes.GradeMania4} for an example).
     * <p>
     * Do note that overriding the {@code renderMove} is also required for pieces to still work, as you will need to disable
     * field visibility to get the credits in the right layer.
     * <p>
     * Suggested use:
     * <pre>
     * // In onLast:
     * engine.isVisible = !engine.gameActive || engine.ending != 2;
     *
     * if (engine.owner.backgroundStatus.bg >= 0 && engine.ending == 0) {
     *     previousBg = engine.owner.backgroundStatus.bg;
     * } else if (engine.ending == 2) {
     *     engine.owner.backgroundStatus.bg = -1;
     * }
     *
     * if (engine.ending != 2) {
     *     engine.owner.backgroundStatus.bg = previousBg;
     * }
     *
     * // In renderFirst:
     * if (engine.ending == 2) {
     *     rendererExtension.drawDefaultBackground(receiver, engine, previousBg);
     * }
     *
     * if (engine.gameActive && engine.ending == 2) {
     *     int offsetX = receiver.getFieldDisplayPositionX(engine, playerID);
     *     int offsetY = receiver.getFieldDisplayPositionY(engine, playerID);
     *
     *     if (engine.displaysize != -1) {
     *         rendererExtension.drawNext(receiver, engine, offsetX, offsetY);
     *         rendererExtension.drawFrame(receiver, engine, offsetX, offsetY + 48, engine.displaysize);
     *     } else {
     *         rendererExtension.drawFrame(receiver, engine, offsetX, offsetY, -1);
     *     }
     * }
     *
     * if ((engine.gameActive) && (engine.ending == 2)) {
     *     int time = ROLL_TIME_LIMIT - rollTime;
     *     if (time < 0) time = 0;
     *     receiver.drawScoreFont(engine, playerID, 0, 14, "ROLL TIME", EventReceiver.COLOR_BLUE);
     *     receiver.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(time), ((time > 0) && (time < 10 * 60)));
     *
     *     Credits.draw(receiver, engine, playerID, (double) rollTime / ROLL_TIME_LIMIT);
     * }
     *
     * if (engine.gameActive && engine.ending == 2) {
     *     int offsetX = receiver.getFieldDisplayPositionX(engine, playerID);
     *     int offsetY = receiver.getFieldDisplayPositionY(engine, playerID);
     *
     *     if (engine.displaysize != -1) {
     *         rendererExtension.drawField(receiver, engine, offsetX + 4, offsetY + 52, engine.displaysize);
     *     } else {
     *         rendererExtension.drawField(receiver, engine, offsetX + 4, offsetY + 4, -1);
     *     }
     * }
     *
     * // In renderMove:
     * if (engine.gameActive && engine.ending == 2) {
     *     engine.isVisible = true;
     *     receiver.renderMove(engine, playerID);
     *     engine.isVisible = false;
     * }
     * </pre>
     *
     * @param receiver Current renderer
     * @param engine   Current game engine
     * @param playerID Current player ID
     * @param progress Current credits progress
     */
    public void draw(EventReceiver receiver, GameEngine engine, int playerID, double progress) {
        final int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
        final int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;

        GameTextUtilities.drawAlignedBoundedTextBlock(
            engine,
            baseX + (engine.field.getWidth() * 8),
            (int) Math.floor(Interpolation.lerp(baseY + (engine.field.getHeight() * 16d), baseY - (double) mainBlock.getHeight(), progress / 0.9)),
            baseX - 1, baseY, baseX + (engine.field.getWidth() * 16) + 1, baseY + (engine.field.getHeight() * 16),
            false, mainBlock, ObjectAlignment.TOP_MIDDLE
        );

        if (progress > finalBlockOffset) {
            GameTextUtilities.drawAlignedBoundedTextBlock(
                engine,
                baseX + (engine.field.getWidth() * 8),
                (int) Math.floor(Interpolation.lerp(baseY + (engine.field.getHeight() * 16d) + (FINAL_BLOCK.getHeight() / 2d), baseY + (engine.field.getHeight() * 8d), Math.min(1d, (progress - finalBlockOffset) / finalBlockMoveProportion))),
                baseX - 1, baseY, baseX + (engine.field.getWidth() * 16) + 1, baseY + (engine.field.getHeight() * 16),
                false, FINAL_BLOCK, ObjectAlignment.MIDDLE_MIDDLE
            );
        }
    }
}
