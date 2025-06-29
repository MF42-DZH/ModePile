/*
 * This library class was created by 0xFC963F18DC21 / Shots243
 * It is part of an extension library for the game NullpoMino (copyright 2010)
 *
 * Herewith shall the term "Library Creator" be given to 0xFC963F18DC21.
 * Herewith shall the term "Game Creator" be given to the original creator of NullpoMino.
 *
 * THIS LIBRARY AND MODE PACK WAS NOT MADE IN ASSOCIATION WITH THE GAME CREATOR.
 *
 * Repository: https://github.com/Shots243/ModePile
 *
 * When using this library in a mode / library pack of your own, the following
 * conditions must be satisfied:
 *     - This license must remain visible at the top of the document, unmodified.
 *     - You are allowed to use this library for any modding purpose.
 *         - If this is the case, the Library Creator must be credited somewhere.
 *             - Source comments only are fine, but in a README is recommended.
 *     - Modification of this library is allowed, but only in the condition that a
 *       pull request is made to merge the changes to the repository.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import org.apache.log4j.Logger;

public class ExamSpinner {
    /**
     * Default asset coordinates and sizes.
     */
    private static final int[][][] SOURCE_DETAILS = {
        new int[][] {
            new int[] { 0, 0 },
            new int[] { 128, 32 }
        },  // Result announcement
        new int[][] {
            new int[] { 0, 32 },
            new int[] { 128, 32 }
        },  // Examination grade
        new int[][] {
            new int[] { 32, 64 },
            new int[] { 64, 32 }
        },  // Pass
        new int[][] {
            new int[] { 0, 64 },
            new int[] { 96, 32 }
        }   // Fail
    };

    private static final int[] startXs = {
        0, 80, 160, 240  // P F P F
    };

    private static final int TravelClose = 320 * 32;

    private static final int[] endXs = {
        startXs[0] + TravelClose,
        startXs[1] + TravelClose,
        startXs[2] + TravelClose,
        startXs[3] + TravelClose
    };

    private static final int spinDuration = 360;
    private static final Piece HUGE_O;
    private static final Logger log = Logger.getLogger(ExamSpinner.class);

    static {
        HUGE_O = new Piece(Piece.PIECE_O);
        HUGE_O.big = true;
        HUGE_O.setSkin(0);
        HUGE_O.direction = 0;
        HUGE_O.setColor(Block.BLOCK_COLOR_YELLOW);
    }

    private final String gradeText;
    private final Integer selectedOutcome;
    private final Boolean close;
    private final boolean custom;
    private final int[] locations;
    private CustomResourceHolder customHolder;
    private String header, subheading;
    private String[] possibilities;
    private boolean clickedBefore;
    private int lifeTime;
    private RendererExtension rendererExtension;

    {
        lifeTime = 0;

        locations = endXs.clone();

        clickedBefore = false;
    }

    /**
     * Create a new promo exam graphic.
     *
     * @param gradeText       What grade to display
     * @param selectedOutcome 0 = pass, 1 = fail
     * @param close           Was it a close one?
     */
    public ExamSpinner(String gradeText, int selectedOutcome, boolean close) {
        custom = false;

        if (gradeText == null) gradeText = "UNDEFINED";

        customHolder = new CustomResourceHolder();
        customHolder.loadImage("res/graphics/examResultText.png", "default");
        rendererExtension = new RendererExtension(customHolder);

        log.debug("Non-custom ExamSpinner object created.");

        this.gradeText = gradeText;
        this.selectedOutcome = selectedOutcome;
        this.close = close;
    }

    /**
     * Creates a custom spinner. Make sure to fill in all fields. Note: use lowercase "\n" for newlines.
     *
     * @param header          Heading text
     * @param subheading      Subheading text
     * @param gradeText       Grade Qualification text
     * @param possibilities   How many possibilities? (Should be length 2; positive in 0, negative in 1)
     * @param selectedOutcome 0 for first outcome, 1 for second.
     * @param close           Was it a close one?
     */
    public ExamSpinner(String header, String subheading, String gradeText, String[] possibilities, int selectedOutcome, boolean close) {
        custom = true;

        if (header == null) header = "PROMOTION\nEXAM";
        if (subheading == null) subheading = "EXAM\nGRADE";
        if (gradeText == null) gradeText = "UNDEFINED";
        if (possibilities == null) possibilities = new String[] { "PASS", "FAIL" };
        if (possibilities.length < 2) possibilities = new String[] { "PASS", "FAIL" };
        if (possibilities.length > 2) possibilities = new String[] { possibilities[0], possibilities[1] };

        log.debug("Custom ExamSpinner object created.");

        customHolder = new CustomResourceHolder();
        rendererExtension = new RendererExtension(customHolder);

        this.header = header;
        this.subheading = subheading;
        this.gradeText = gradeText;
        this.possibilities = possibilities;
        this.selectedOutcome = selectedOutcome;
        this.close = close;
    }

    /**
     * Draws the spinner to the screen.
     *
     * @param receiver Renderer to draw with
     * @param engine   Current <code>GameEngine</code> instance
     * @param playerID Current Player ID (0 = 1P)
     * @param flag     Yellow text?
     */
    public void draw(EventReceiver receiver, GameEngine engine, int playerID, boolean flag) {
        int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
        int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
        int size = 16;

        HUGE_O.setSkin(engine.getSkin());

        int b = 255;
        if (flag) b = 0;
        int color = EventReceiver.COLOR_WHITE;
        if (flag) color = EventReceiver.COLOR_YELLOW;

        if (custom) {
            String[] splitHeadingText = header.split("\n");
            String[] splitSubheadingText = subheading.split("\n");
            String[] splitGradeText = gradeText.split("\n");
            String[][] splitPossibilityText = new String[possibilities.length][];
            for (int i = 0; i < possibilities.length; i++) {
                splitPossibilityText[i] = possibilities[i].split("\n");
            }

            // region MAIN HEADING
            int HBX = baseX + 80;
            for (int i = 0; i < splitHeadingText.length; i++) {
                GameTextUtilities.drawDirectTextAlign(receiver, engine, playerID, HBX, baseY + (size * i), GameTextUtilities.ALIGN_TOP_MIDDLE, splitHeadingText[i], color, 1f);
            }
            // endregion MAIN HEADING

            // region SUBHEADING
            int SHBY = baseY + (size * 4);
            for (int i = 0; i < splitSubheadingText.length; i++) {
                GameTextUtilities.drawDirectTextAlign(receiver, engine, playerID, HBX, SHBY + (size * i), GameTextUtilities.ALIGN_TOP_MIDDLE, splitSubheadingText[i], color, 1f);
            }
            // endregion SUBHEADING

            // region GRADE
            int GBY = baseY + (size * 9);
            for (int i = 0; i < splitGradeText.length; i++) {
                GameTextUtilities.drawDirectTextAlign(receiver, engine, playerID, HBX, GBY + (size * i), GameTextUtilities.ALIGN_TOP_MIDDLE, splitGradeText[i], color, splitGradeText.length == 1 ? 2f : 1f);
            }
            // endregion GRADE

            int PBY = baseY + (size * 16);
            if (close) {
                if (lifeTime < spinDuration) {
                    // Pass1
                    for (int i = 0; i < splitPossibilityText[0].length; i++) {
                        if ((locations[0] % 320) <= 160)
                            GameTextUtilities.drawDirectTextAlign(receiver, engine, playerID, baseX + (locations[0] % 320), PBY + (size * i), GameTextUtilities.ALIGN_MIDDLE_MIDDLE,
                                splitPossibilityText[0][i], color, 1f);
                    }

                    // Fail1
                    for (int i = 0; i < splitPossibilityText[1].length; i++) {
                        if ((locations[1] % 320) <= 160)
                            GameTextUtilities.drawDirectTextAlign(receiver, engine, playerID, baseX + (locations[1] % 320), PBY + (size * i), GameTextUtilities.ALIGN_MIDDLE_MIDDLE,
                                splitPossibilityText[1][i], EventReceiver.COLOR_DARKBLUE, 1f);
                    }

                    // Pass1
                    for (int i = 0; i < splitPossibilityText[0].length; i++) {
                        if ((locations[2] % 320) <= 160)
                            GameTextUtilities.drawDirectTextAlign(receiver, engine, playerID, baseX + (locations[2] % 320), PBY + (size * i), GameTextUtilities.ALIGN_MIDDLE_MIDDLE,
                                splitPossibilityText[0][i], color, 1f);
                    }

                    // Fail1
                    for (int i = 0; i < splitPossibilityText[1].length; i++) {
                        if ((locations[3] % 320) <= 160)
                            GameTextUtilities.drawDirectTextAlign(receiver, engine, playerID, baseX + (locations[3] % 320), PBY + (size * i), GameTextUtilities.ALIGN_MIDDLE_MIDDLE,
                                splitPossibilityText[1][i], EventReceiver.COLOR_DARKBLUE, 1f);
                    }
                } else if (lifeTime < spinDuration + 120) {
                    int offset = (lifeTime % 3) - 1;
                    // FailShake
                    for (int i = 0; i < splitPossibilityText[1].length; i++) {
                        GameTextUtilities.drawDirectTextAlign(receiver, engine, playerID, HBX + offset, PBY + (size * i), GameTextUtilities.ALIGN_MIDDLE_MIDDLE,
                            splitPossibilityText[1][i], EventReceiver.COLOR_DARKBLUE, 1f);
                    }

                    if ((lifeTime >= spinDuration + 112) && selectedOutcome == 0) {
                        int height = ((lifeTime - spinDuration - 113) * 2) - 1;
                        int width = 3;

                        rendererExtension.drawScaledPiece(receiver, baseX + width * 16, baseY + height * 16, HUGE_O, 1f, 0f);
                    }
                } else {
                    if (lifeTime == spinDuration + 120) {
                        Block blk = new Block(Block.BLOCK_COLOR_YELLOW);
                        for (int y = 13; y < 18; y++) {
                            for (int x = 3; x < 8; x++) {
                                int x2 = x * 16 + baseX;
                                int y2 = y * 16 + baseY;
                                rendererExtension.addBlockBreakEffect(receiver, x2, y2, blk);
                            }
                        }
                    }
                    if (selectedOutcome == 0) {
                        // PASS
                        for (int i = 0; i < splitPossibilityText[0].length; i++) {
                            GameTextUtilities.drawDirectTextAlign(receiver, engine, playerID, HBX, PBY + (size * i), GameTextUtilities.ALIGN_MIDDLE_MIDDLE,
                                splitPossibilityText[0][i], color, 1f);
                        }
                    } else {
                        for (int i = 0; i < splitPossibilityText[1].length; i++) {
                            GameTextUtilities.drawDirectTextAlign(receiver, engine, playerID, HBX, PBY + (size * i), GameTextUtilities.ALIGN_MIDDLE_MIDDLE,
                                splitPossibilityText[1][i], EventReceiver.COLOR_DARKBLUE, 1f);
                        }
                    }
                }
            } else if (lifeTime >= 60) {
                if (selectedOutcome == 0) {
                    // PASS
                    for (int i = 0; i < splitPossibilityText[0].length; i++) {
                        GameTextUtilities.drawDirectTextAlign(receiver, engine, playerID, HBX, PBY + (size * i), GameTextUtilities.ALIGN_MIDDLE_MIDDLE,
                            splitPossibilityText[0][i], color, 1f);
                    }
                } else {
                    for (int i = 0; i < splitPossibilityText[1].length; i++) {
                        GameTextUtilities.drawDirectTextAlign(receiver, engine, playerID, HBX, PBY + (size * i), GameTextUtilities.ALIGN_MIDDLE_MIDDLE,
                            splitPossibilityText[1][i], EventReceiver.COLOR_DARKBLUE, 1f);
                    }
                }
            }
        } else {
            int[] alphas = new int[locations.length];
            for (int i = 0; i < locations.length; i++) {
                int diff;
                int l = (locations[i] % 320);
                if (l <= 80) diff = l;
                else diff = 80 - (l - 80);
                if (diff < 0) diff = 0;

                final int alpha = (int) Interpolation.sineStep(0, 255, (double) diff / 80d);
                alphas[i] = alpha;
            }

            customHolder.drawOffsetImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[0][1][0] / 2), baseY, SOURCE_DETAILS[0][1][0], SOURCE_DETAILS[0][1][1], SOURCE_DETAILS[0][0][0], SOURCE_DETAILS[0][0][1], SOURCE_DETAILS[0][1][0], SOURCE_DETAILS[0][1][1], 255, 255, b, 255);
            customHolder.drawOffsetImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[1][1][0] / 2), baseY + size * 4, SOURCE_DETAILS[1][1][0], SOURCE_DETAILS[1][1][1], SOURCE_DETAILS[1][0][0], SOURCE_DETAILS[1][0][1], SOURCE_DETAILS[1][1][0], SOURCE_DETAILS[1][1][1], 255, 255, b, 255);

            receiver.drawMenuFont(engine, playerID, 5 - gradeText.length(), 9, gradeText, color, 2.0f);

            // NEW CODE GOES HERE.
            if (close) {
                if (lifeTime < spinDuration) {
                    customHolder.drawOffsetImage(engine, "default", baseX + (locations[0] % 320) - (SOURCE_DETAILS[2][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], SOURCE_DETAILS[2][0][0], SOURCE_DETAILS[2][0][1], SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], 255, 255, b, alphas[0]);
                    customHolder.drawOffsetImage(engine, "default", baseX + (locations[1] % 320) - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], 80, 80, 160, alphas[1]);
                    customHolder.drawOffsetImage(engine, "default", baseX + (locations[2] % 320) - (SOURCE_DETAILS[2][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], SOURCE_DETAILS[2][0][0], SOURCE_DETAILS[2][0][1], SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], 255, 255, b, alphas[2]);
                    customHolder.drawOffsetImage(engine, "default", baseX + (locations[3] % 320) - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], 80, 80, 160, alphas[3]);
                } else if (lifeTime < spinDuration + 120) {
                    int offset = (lifeTime % 3) - 1;
                    customHolder.drawOffsetImage(engine, "default", baseX + 80 + offset - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], 80, 80, 160, 255);

                    if (lifeTime >= spinDuration + 112) {
                        int height = ((lifeTime - spinDuration - 113) * 2) - 1;
                        int width = 3;

                        rendererExtension.drawScaledPiece(receiver, baseX + width * 16, baseY + height * 16, HUGE_O, 1f, 0f);
                    }
                } else {
                    if (lifeTime == spinDuration + 120) {
                        Block blk = new Block(Block.BLOCK_COLOR_YELLOW);
                        for (int y = 13; y < 18; y++) {
                            for (int x = 3; x < 8; x++) {
                                int x2 = x * 16 + baseX;
                                int y2 = y * 16 + baseY;
                                rendererExtension.addBlockBreakEffect(receiver, x2, y2, blk);
                            }
                        }
                    }
                    if (selectedOutcome == 0) {
                        // PASS
                        customHolder.drawOffsetImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[2][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], SOURCE_DETAILS[2][0][0], SOURCE_DETAILS[2][0][1], SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], 255, 255, b, 255);
                    } else {
                        customHolder.drawOffsetImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], 80, 80, 160, 255);
                    }
                }
            } else if (lifeTime >= 60) {
                if (selectedOutcome == 0) {
                    // PASS
                    customHolder.drawOffsetImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[2][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], SOURCE_DETAILS[2][0][0], SOURCE_DETAILS[2][0][1], SOURCE_DETAILS[2][1][0], SOURCE_DETAILS[2][1][1], 255, 255, b, 255);
                } else {
                    customHolder.drawOffsetImage(engine, "default", baseX + 80 - (SOURCE_DETAILS[3][1][0] / 2), baseY + size * 14, SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], SOURCE_DETAILS[3][0][0], SOURCE_DETAILS[3][0][1], SOURCE_DETAILS[3][1][0], SOURCE_DETAILS[3][1][1], 80, 80, 160, 255);
                }
            }
        }
    }

    /**
     * Draws the spinner to the screen.
     *
     * @param receiver Renderer to draw with
     * @param engine   Current <code>GameEngine</code> instance
     * @param playerID Current Player ID (0 = 1P)
     */
    public void draw(EventReceiver receiver, GameEngine engine, int playerID) {
        draw(receiver, engine, playerID, (lifeTime / 2) % 2 == 0);
    }

    /**
     * Updates the state of the ExamSpinner instance
     *
     * @param engine GameEngine to play SE with
     */
    public void update(GameEngine engine) {
        lifeTime++;

        if (lifeTime == 60 && !close || lifeTime == spinDuration + 120 && close) {
            engine.playSE("linefall");
            if (selectedOutcome == 0) {
                engine.playSE("excellent");
            } else {
                engine.playSE("regret");
            }
        }

        if (close && lifeTime <= spinDuration) {
            double j = (double) lifeTime / (double) spinDuration;
            for (int i = 0; i < locations.length; i++) {
                double res = Interpolation.smoothStep(endXs[i], startXs[i], 64, j);
                locations[i] = (int) res;
            }

            for (int i = 0; i < locations.length; i++) {
                if (MathHelper.almostEqual(locations[i] % 320, 80, 24)) {
                    if (!clickedBefore) {
                        clickedBefore = true;
                        engine.playSE("change");
                    }
                    break;
                }

                if (i == locations.length - 1) {
                    clickedBefore = false;
                }
            }
        }
    }
}
