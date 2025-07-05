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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;

public class GameTextUtilities {
    /**
     * Rainbow colour order
     */
    public static final Integer[] RAINBOW_ORDER = {
        EventReceiver.COLOR_RED,
        EventReceiver.COLOR_ORANGE,
        EventReceiver.COLOR_YELLOW,
        EventReceiver.COLOR_WHITE,
        EventReceiver.COLOR_GREEN,
        EventReceiver.COLOR_CYAN,
        EventReceiver.COLOR_BLUE,
        EventReceiver.COLOR_DARKBLUE,
        EventReceiver.COLOR_PURPLE,
        EventReceiver.COLOR_PINK,
    };

    /**
     * Text alignment option
     */
    public static final int ALIGN_TOP_LEFT = 0,
        ALIGN_TOP_MIDDLE = 1,
        ALIGN_TOP_RIGHT = 2,
        ALIGN_MIDDLE_LEFT = 3,
        ALIGN_MIDDLE_MIDDLE = 4,
        ALIGN_MIDDLE_RIGHT = 5,
        ALIGN_BOTTOM_LEFT = 6,
        ALIGN_BOTTOM_MIDDLE = 7,
        ALIGN_BOTTOM_RIGHT = 8;

    /**
     * Rainbow colour count
     */
    public static final int RAINBOW_COLOURS = 10;

    /**
     * Valid characters
     */
    private static final String CHARACTERS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopq";

    /**
     * Sequential Character Phase
     */
    private static int characterPhase = 0;

    private static CustomResourceHolder customGraphics;

    private static CustomResourceHolder getCustomGraphics() {
        if (customGraphics != null) return customGraphics;

        customGraphics = new CustomResourceHolder(1);
        return customGraphics;
    }

    /**
     * Representation of a piece of text to draw.
     * Do not use newlines directly, use the special newline constructor.
     */
    public static class Text {
        public static final int BASE_UNIT = 16;

        public final String string;
        public final int colour;
        public final float scale;

        private Text(String string, int colour, float scale) {
            this.string = string;
            this.colour = colour;
            this.scale = scale;
        }

        public static Text of(String string) {
            return new Text(string, EventReceiver.COLOR_WHITE, 1f);
        }

        public static Text of(String string, int colour) {
            return new Text(string, colour, 1f);
        }

        public static Text ofSmall(String string) {
            return new Text(string, EventReceiver.COLOR_WHITE, 0.5f);
        }

        public static Text ofSmall(String string, int colour) {
            return new Text(string, colour, 0.5f);
        }

        public static Text ofBig(String string) {
            return new Text(string, EventReceiver.COLOR_WHITE, 2f);
        }

        public static Text ofBig(String string, int colour) {
            return new Text(string, colour,  2f);
        }

        public static Text custom(String string, int colour, float scale) {
            return new Text(string, colour, scale);
        }

        public static Text newLine() {
            return new Text("\n", EventReceiver.COLOR_WHITE, 0f);
        }

        public int getWidth() {
            return (int) (string.length() * BASE_UNIT * scale);
        }

        public int getHeight() {
            return (int) (BASE_UNIT * scale);
        }

        public boolean isNewLine() {
            return string.equals("\n") && scale == 0f;
        }
    }

    /** Representation of a left-aligned block of lines to draw. */
    public static class TextBlock {
        private final Text[] texts;
        private int width = -1;
        private int height = -1;

        public TextBlock(Text... texts) {
            this.texts = texts;
        }

        public TextBlock(Collection<Text> texts) {
            this.texts = texts.toArray(new Text[0]);
        }

        public Text get(int i) {
            return texts[i];
        }

        public int length() {
            return texts.length;
        }

        public int getWidth() {
            if (width > -1) return width;

            int offset = 0;

            while (offset < length()) {
                final int lineEnd = findLineEndIndex(this, offset);
                int cWidth = 0;

                for (int i = offset; i < lineEnd; ++i) {
                    cWidth += texts[i].getWidth();
                }

                width = Math.max(width, cWidth);

                offset = lineEnd + 1;
            }

            return width;
        }

        public int getHeight() {
            if (height > -1) return height;

            height = 0;

            int offset = 0;
            while (offset < length()) {
                final int lineEnd = findLineEndIndex(this, offset);

                float maxLineScale = 0f;
                for (int i = offset; i < lineEnd; ++i) {
                    maxLineScale = Math.max(texts[i].scale, maxLineScale);
                }

                height += (int) (Text.BASE_UNIT * maxLineScale);

                offset = lineEnd + 1;
            }

            return height;
        }
    }

    private static int findLineEndIndex(TextBlock texts, int offset) {
        for (int i = offset; i < texts.length(); ++i) {
            if (texts.get(i).isNewLine()) return i;
        }

        return texts.length();
    }

    // Single text version of the block methods.
    public static void drawDirectText(GameEngine engine, int startX, int startY, Text text) {
        getCustomGraphics().drawString(engine, startX, startY, text.string, text.colour, text.scale);
    }

    public static void drawAlignedText(GameEngine engine, int startX, int startY, Text text) {
        drawAlignedText(engine, startX, startY, text, ALIGN_TOP_LEFT);
    }

    public static void drawAlignedText(GameEngine engine, int startX, int startY, Text text, int alignment) {
        int offsetX, offsetY;

        switch (alignment) {
            case ALIGN_TOP_MIDDLE:
            case ALIGN_MIDDLE_MIDDLE:
            case ALIGN_BOTTOM_MIDDLE:
                offsetX = text.getWidth() / 2;
                break;
            case ALIGN_TOP_RIGHT:
            case ALIGN_MIDDLE_RIGHT:
            case ALIGN_BOTTOM_RIGHT:
                offsetX = text.getWidth();
                break;
            default:
                offsetX = 0;
                break;
        }

        switch (alignment) {
            case ALIGN_MIDDLE_LEFT:
            case ALIGN_MIDDLE_MIDDLE:
            case ALIGN_MIDDLE_RIGHT:
                offsetY = text.getHeight() / 2;
                break;
            case ALIGN_BOTTOM_LEFT:
            case ALIGN_BOTTOM_MIDDLE:
            case ALIGN_BOTTOM_RIGHT:
                offsetY = text.getHeight();
                break;
            default:
                offsetY = 0;
                break;
        }

        drawDirectText(engine, startX - offsetX, startY - offsetY, text);
    }

    public static void drawAlignedScoreText(EventReceiver receiver, GameEngine engine, int playerID, boolean smallGrid, int x, int y, Text text) {
        drawAlignedScoreText(receiver, engine, playerID, smallGrid, x, y, text, ALIGN_TOP_LEFT);
    }

    public static void drawAlignedScoreText(EventReceiver receiver, GameEngine engine, int playerID, boolean smallGrid, int x, int y, Text text, int alignment) {
        int gridSize = smallGrid ? 8 : 16;

        drawAlignedText(
            engine,
            receiver.getScoreDisplayPositionX(engine, playerID) + (x * gridSize),
            receiver.getScoreDisplayPositionY(engine, playerID) + (y * gridSize),
            text,
            alignment
        );
    }

    public static void drawAlignedMenuText(EventReceiver receiver, GameEngine engine, int playerID, boolean smallGrid, int x, int y, Text text) {
        drawAlignedMenuText(receiver, engine, playerID, smallGrid, x, y, text, ALIGN_TOP_LEFT);
    }

    public static void drawAlignedMenuText(EventReceiver receiver, GameEngine engine, int playerID, boolean smallGrid, int x, int y, Text text, int alignment) {
        int gridSize = smallGrid ? 8 : 16;

        drawAlignedText(
            engine,
            receiver.getFieldDisplayPositionX(engine, playerID) + (x * gridSize) + 4,
            receiver.getFieldDisplayPositionY(engine, playerID) + (y * gridSize) + 52,
            text,
            alignment
        );
    }

    /**
     * Draws a block of texts defined by a text block.
     * Text blocks always left-align all lines.
     *
     * @param engine         <code>GameEngine</code> to draw with
     * @param startX         Start X-coordinate (Top-Left Corner)
     * @param startY         Start Y-coortinate (Top-Right Corner)
     * @param pinTop         Pin line to top instead of bottom when varying scale text exists
     * @param texts          The text block to draw
     */
    public static void drawDirectTextBlock(GameEngine engine, int startX, int startY, boolean pinTop, TextBlock texts) {
        int dx = startX;
        int dy = startY;

        // Process all lines.
        int offset = 0;
        while (offset < texts.length()) {
            final int lineEnd = findLineEndIndex(texts, offset);

            float maxLineScale = 0f;
            for (int i = offset; i < lineEnd; ++i) {
                maxLineScale = Math.max(texts.get(i).scale, maxLineScale);
            }

            for (int i = offset; i < lineEnd; ++i) {
                getCustomGraphics().drawString(
                    engine,
                    dx,
                    pinTop ? dy : dy + (int) ((maxLineScale - texts.get(i).scale) * Text.BASE_UNIT),
                    texts.get(i).string,
                    texts.get(i).colour,
                    texts.get(i).scale
                );

                dx += texts.get(i).getWidth();
            }

            dx = startX;
            dy += (int) (Text.BASE_UNIT * maxLineScale);

            offset = lineEnd + 1;
        }
    }

    /**
     * Draws a block of texts defined by a text block.
     * Text blocks always left-align all lines.
     * <p>
     * Alignment only modifies alignment by bounding box.
     *
     * @param engine         <code>GameEngine</code> to draw with
     * @param startX         Start X-coordinate (Top-Left Corner)
     * @param startY         Start Y-coortinate (Top-Right Corner)
     * @param pinTop         Pin line to top instead of bottom when varying scale text exists
     * @param texts          The text block to draw
     * @param alignment      Alignment of the texts bounding box
     */
    public static void drawAlignedTextBlock(GameEngine engine, int startX, int startY, boolean pinTop, TextBlock texts, int alignment) {
        int offsetX, offsetY;

        switch (alignment) {
            case ALIGN_TOP_MIDDLE:
            case ALIGN_MIDDLE_MIDDLE:
            case ALIGN_BOTTOM_MIDDLE:
                offsetX = texts.getWidth() / 2;
                break;
            case ALIGN_TOP_RIGHT:
            case ALIGN_MIDDLE_RIGHT:
            case ALIGN_BOTTOM_RIGHT:
                offsetX = texts.getWidth();
                break;
            default:
                offsetX = 0;
                break;
        }

        switch (alignment) {
            case ALIGN_MIDDLE_LEFT:
            case ALIGN_MIDDLE_MIDDLE:
            case ALIGN_MIDDLE_RIGHT:
                offsetY = texts.getHeight() / 2;
                break;
            case ALIGN_BOTTOM_LEFT:
            case ALIGN_BOTTOM_MIDDLE:
            case ALIGN_BOTTOM_RIGHT:
                offsetY = texts.getHeight();
                break;
            default:
                offsetY = 0;
                break;
        }

        drawDirectTextBlock(engine, startX - offsetX, startY - offsetY, pinTop, texts);
    }

    /**
     * Draws a block of score texts defined by a text block.
     * Text blocks always left-align all lines.
     * <p>
     * <code>x</code> and <code>y</code> determine where in the score grid to draw the text.
     *
     * @param receiver       <code>EventReceiver</code> to get position info from
     * @param engine         <code>GameEngine</code> to draw with
     * @param x              Start X-coordinate (Top-Left Cornern Grid)
     * @param y              Start Y-coortinate (Top-Right Corner in Grid)
     * @param pinTop         Pin line to top instead of bottom when varying scale text exists
     * @param texts          The text block to draw
     */
    public static void drawAlignedScoreTextBlock(EventReceiver receiver, GameEngine engine, int playerID, boolean smallGrid, int x, int y, boolean pinTop, TextBlock texts, int alignment) {
        int gridSize = smallGrid ? 8 : 16;

        drawAlignedTextBlock(
            engine,
            receiver.getScoreDisplayPositionX(engine, playerID) + (x * gridSize),
            receiver.getScoreDisplayPositionY(engine, playerID) + (y * gridSize),
            pinTop,
            texts,
            alignment
        );
    }

    /**
     * Draws a block of score texts defined by a text block.
     * Text blocks always left-align all lines.
     * <p>
     * <code>x</code> and <code>y</code> determine where in the menu grid to draw the text.
     *
     * @param receiver       <code>EventReceiver</code> to get position info from
     * @param engine         <code>GameEngine</code> to draw with
     * @param x              Start X-coordinate (Top-Left Cornern Grid)
     * @param y              Start Y-coortinate (Top-Right Corner in Grid)
     * @param pinTop         Pin line to top instead of bottom when varying scale text exists
     * @param texts          The text block to draw
     */
    public static void drawAlignedMenuTextBlock(EventReceiver receiver, GameEngine engine, int playerID, boolean smallGrid, int x, int y, boolean pinTop, TextBlock texts, int alignment) {
        int gridSize = smallGrid ? 8 : 16;

        drawAlignedTextBlock(
            engine,
            receiver.getFieldDisplayPositionX(engine, playerID) + (x * gridSize) + 4,
            receiver.getFieldDisplayPositionY(engine, playerID) + (y * gridSize) + 52,
            pinTop,
            texts,
            alignment
        );
    }

    // region String Utilities

    /**
     * Generates a completely random string.
     *
     * @param length       Length of string
     * @param randomEngine Random instance to use
     * @return Random string, with all characters either being visible or a space.
     */
    public static String randomString(int length, Random randomEngine) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(randomEngine.nextInt(CHARACTERS.length())));
        }

        return sb.toString();
    }

    /**
     * Completely obfuscates a string.
     *
     * @param str          String to obfuscate
     * @param randomEngine Random instance to use
     * @return Obfuscated string.
     */
    public static String obfuscateString(String str, Random randomEngine) {
        return obfuscateString(str, 1d, randomEngine);
    }

    /**
     * Obfuscates a string with random characters.
     *
     * @param str          String to obfuscate
     * @param chance       Chance of character obfuscation (0 < chance <= 1)
     * @param randomEngine Random instance to use
     * @return Obfuscated string.
     */
    public static String obfuscateString(String str, double chance, Random randomEngine) {
        if (chance <= 0) return str;

        StringBuilder sb = new StringBuilder(str);

        for (int i = 0; i < sb.length(); i++) {
            double c = randomEngine.nextDouble();
            if (c < chance) {
                sb.setCharAt(i, CHARACTERS.charAt(randomEngine.nextInt(CHARACTERS.length())));
            }
        }

        return sb.toString();
    }

    // endregion String Utilities

    // region Aligned Text

    /**
     * Draws an aligned string using <code>drawDirectFont</code>.
     *
     * @param engine    Current GameEngine
     * @param x         X coordinate of top-left corner of text
     * @param y         Y coordinate of top-left corner of text
     * @param alignment Alignment of string relative to string's area
     * @param str       String to draw
     * @param color     Color of string
     * @param scale     Scale of string
     */
    @Deprecated
    public static void drawDirectTextAlign(GameEngine engine, int x, int y, int alignment, String str, Integer color, Float scale) {
        if (color == null) color = 0;
        if (scale == null) scale = 1f;
        if (str == null) return;

        drawAlignedText(engine, x, y, Text.custom(str, color, scale), alignment);
    }

    /**
     * Draws an aligned string using <code>drawScoreFont</code>.
     *
     * @param receiver  EventReceiver used to draw
     * @param engine    Current GameEngine
     * @param playerID  Player ID (1P = 0)
     * @param x         X coordinate of top-left corner of text
     * @param y         Y coordinate of top-left corner of text
     * @param str       String to draw
     * @param color     Color of string
     * @param scale     Scale of string
     */
    @Deprecated
    public static void drawScoreTextAlign(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, int alignment, String str, Integer color, Float scale) {
        if (color == null) color = 0;
        if (scale == null) scale = 1f;
        if (str == null) return;

        drawAlignedScoreText(receiver, engine, playerID, scale == 0.5f, x, y, Text.custom(str, color, scale), alignment);
    }

    /**
     * Draws an aligned string using <code>drawMenuFont</code>.
     *
     * @param receiver  EventReceiver used to draw
     * @param engine    Current GameEngine
     * @param playerID  Player ID (1P = 0)
     * @param x         X coordinate of top-left corner of text
     * @param y         Y coordinate of top-left corner of text
     * @param alignment Alignment of string relative to string's area
     * @param str       String to draw
     * @param color     Color of string
     * @param scale     Scale of string
     */
    @Deprecated
    public static void drawMenuTextAlign(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, int alignment, String str, Integer color, Float scale) {
        if (color == null) color = 0;
        if (scale == null) scale = 1f;
        if (str == null) return;

        drawAlignedMenuText(receiver, engine, playerID, scale == 0.5f, x, y, Text.custom(str, color, scale), alignment);
    }

    // endregion Aligned Text

    // region Rainbow Text

    /**
     * Draws a rainbow string using <code>drawDirectFont</code>.
     *
     * @param engine      Current GameEngine
     * @param x           X coordinate of top-left corner of text
     * @param y           Y coordinate of top-left corner of text
     * @param str         String to draw
     * @param startColour Starting colour of text
     * @param scale       Scale of text
     */
    public static void drawRainbowDirectString(GameEngine engine, int x, int y, String str, int startColour, float scale) {
        drawRainbowDirectString(engine, x, y, str, startColour, scale, false);
    }

    /**
     * Draws a rainbow string using <code>drawScoreFont</code>.
     *
     * @param receiver    EventReceiver used to draw
     * @param engine      Current GameEngine
     * @param playerID    Player ID (1P = 0)
     * @param x           X coordinate of top-left corner of text
     * @param y           Y coordinate of top-left corner of text
     * @param str         String to draw
     * @param startColour Starting colour of text
     * @param scale       Scale of text
     */
    public static void drawRainbowScoreString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, int startColour, float scale) {
        drawRainbowScoreString(receiver, engine, playerID, x, y, str, startColour, scale, false);
    }

    /**
     * Draws a rainbow string using <code>drawMenuFont</code>.
     *
     * @param receiver    EventReceiver used to draw
     * @param engine      Current GameEngine
     * @param playerID    Player ID (1P = 0)
     * @param x           X coordinate of top-left corner of text
     * @param y           Y coordinate of top-left corner of text
     * @param str         String to draw
     * @param startColour Starting colour of text
     * @param scale       Scale of text
     */
    public static void drawRainbowMenuString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, int startColour, float scale) {
        drawRainbowMenuString(receiver, engine, playerID, x, y, str, startColour, scale, false);
    }

    /**
     * Draws a rainbow string using <code>drawDirectFont</code>.
     *
     * @param engine      Current GameEngine
     * @param x           X coordinate of top-left corner of text
     * @param y           Y coordinate of top-left corner of text
     * @param str         String to draw
     * @param startColour Starting colour of text
     * @param scale       Scale of text
     * @param reverse     Reverse order or not
     */
    public static void drawRainbowDirectString(GameEngine engine, int x, int y, String str, int startColour, float scale, boolean reverse) {
        int offset = 0;
        final List<Text> chars = new LinkedList<>();

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ' ') {
                offset++;
            } else {
                int j = (Arrays.asList(RAINBOW_ORDER).indexOf(startColour) + (i * (reverse ? -1 : 1)) - (offset * (reverse ? -1 : 1))) % RAINBOW_COLOURS;
                if (j < 0) j = RAINBOW_COLOURS - j;

                chars.add(Text.custom(str.substring(i, i + 1), RAINBOW_ORDER[j], scale));
            }
        }

        drawDirectTextBlock(engine, x, y, false, new TextBlock(chars));
    }

    /**
     * Draws a rainbow string using <code>drawScoreFont</code>.
     *
     * @param receiver    EventReceiver used to draw
     * @param engine      Current GameEngine
     * @param playerID    Player ID (1P = 0)
     * @param x           X coordinate of top-left corner of text
     * @param y           Y coordinate of top-left corner of text
     * @param str         String to draw
     * @param startColour Starting colour of text
     * @param scale       Scale of text
     * @param reverse     Reverse order or not
     */
    public static void drawRainbowScoreString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, int startColour, float scale, boolean reverse) {
        int offset = 0;
        final List<Text> chars = new LinkedList<>();

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ' ') {
                offset++;
            } else {
                int j = (Arrays.asList(RAINBOW_ORDER).indexOf(startColour) + (i * (reverse ? -1 : 1)) - (offset * (reverse ? -1 : 1))) % RAINBOW_COLOURS;
                if (j < 0) j = RAINBOW_COLOURS - j;

                chars.add(Text.custom(str.substring(i, i + 1), RAINBOW_ORDER[j], scale));
            }
        }

        drawAlignedScoreTextBlock(receiver, engine, playerID, scale == 0.5f, x, y, false, new TextBlock(chars), ALIGN_TOP_LEFT);
    }

    /**
     * Draws a rainbow string using <code>drawMenuFont</code>.
     *
     * @param receiver    EventReceiver used to draw
     * @param engine      Current GameEngine
     * @param playerID    Player ID (1P = 0)
     * @param x           X coordinate of top-left corner of text
     * @param y           Y coordinate of top-left corner of text
     * @param str         String to draw
     * @param startColour Starting colour of text
     * @param scale       Scale of text
     * @param reverse     Reverse order or not
     */
    public static void drawRainbowMenuString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, int startColour, float scale, boolean reverse) {
        int offset = 0;
        final List<Text> chars = new LinkedList<>();

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ' ') {
                offset++;
            } else {
                int j = (Arrays.asList(RAINBOW_ORDER).indexOf(startColour) + (i * (reverse ? -1 : 1)) - (offset * (reverse ? -1 : 1))) % RAINBOW_COLOURS;
                if (j < 0) j = RAINBOW_COLOURS - j;

                chars.add(Text.custom(str.substring(i, i + 1), RAINBOW_ORDER[j], scale));
            }
        }

        drawAlignedMenuTextBlock(receiver, engine, playerID, scale == 0.5f, x, y, false, new TextBlock(chars), ALIGN_TOP_LEFT);
    }

    /**
     * Draws a rainbow string using <code>drawDirectFont</code>.
     *
     * @param engine       Current GameEngine
     * @param x            X coordinate of top-left corner of text
     * @param y            Y coordinate of top-left corner of text
     * @param str          String to draw
     * @param randomEngine Random instance to use
     * @param scale        Scale of text
     */
    public static void drawRandomRainbowDirectString(GameEngine engine, int x, int y, String str, Random randomEngine, float scale) {
        int offset = 0;
        final List<Text> chars = new LinkedList<>();

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ' ') {
                offset++;
            } else {
                chars.add(Text.custom(str.substring(i, i + 1), RAINBOW_ORDER[randomEngine.nextInt(RAINBOW_COLOURS)], scale));
            }
        }

        drawDirectTextBlock(engine, x, y, false, new TextBlock(chars));
    }

    /**
     * Draws a rainbow string using <code>drawScoreFont</code>.
     *
     * @param receiver     EventReceiver used to draw
     * @param engine       Current GameEngine
     * @param playerID     Player ID (1P = 0)
     * @param x            X coordinate of top-left corner of text
     * @param y            Y coordinate of top-left corner of text
     * @param str          String to draw
     * @param randomEngine Random instance to use
     * @param scale        Scale of text
     */
    public static void drawRandomRainbowScoreString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, Random randomEngine, float scale) {
        int offset = 0;
        final List<Text> chars = new LinkedList<>();

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ' ') {
                offset++;
            } else {
                chars.add(Text.custom(str.substring(i, i + 1), RAINBOW_ORDER[randomEngine.nextInt(RAINBOW_COLOURS)], scale));
            }
        }

        drawAlignedScoreTextBlock(receiver, engine, playerID, scale == 0.5f, x, y, false, new TextBlock(chars), ALIGN_TOP_LEFT);
    }

    /**
     * Draws a rainbow string using <code>drawMenuFont</code>.
     *
     * @param receiver     EventReceiver used to draw
     * @param engine       Current GameEngine
     * @param playerID     Player ID (1P = 0)
     * @param x            X coordinate of top-left corner of text
     * @param y            Y coordinate of top-left corner of text
     * @param str          String to draw
     * @param randomEngine Random instance to use
     * @param scale        Scale of text
     */
    public static void drawRandomRainbowMenuString(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, String str, Random randomEngine, float scale) {
        int offset = 0;
        final List<Text> chars = new LinkedList<>();

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ' ') {
                offset++;
            } else {
                chars.add(Text.custom(str.substring(i, i + 1), RAINBOW_ORDER[randomEngine.nextInt(RAINBOW_COLOURS)], scale));
            }
        }

        drawAlignedMenuTextBlock(receiver, engine, playerID, scale == 0.5f, x, y, false, new TextBlock(chars), ALIGN_TOP_LEFT);
    }

    // endregion Rainbow Text

    // region Character Phase Functions

    /**
     * Get current character in sequence.
     *
     * @return Character at phase.
     */
    public static char getCurrentCharacter() {
        return CHARACTERS.charAt(characterPhase);
    }

    /**
     * Gets current character in sequence with offset.
     *
     * @param offset Character offset.
     * @return Offset sequence character.
     */
    public static char getCurrentCharacter(int offset) {
        int i = characterPhase + offset;
        i = MathHelper.pythonModulo(i, CHARACTERS.length());

        return CHARACTERS.charAt(i);
    }

    /**
     * Increments character phase by 1.
     */
    public static void updatePhase() {
        updatePhase(1);
    }

    /**
     * Increments character phase by x.
     *
     * @param x Amount to increment by.
     */
    public static void updatePhase(int x) {
        characterPhase = MathHelper.pythonModulo(characterPhase + x, CHARACTERS.length());
    }

    /**
     * Resets phase to 0.
     */
    public static void resetPhase() {
        characterPhase = 0;
    }

    /**
     * Sets the character phase.
     *
     * @param x Integer to set phase to.
     */
    public static void setPhase(int x) {
        characterPhase = x % CHARACTERS.length();
        if (characterPhase < 0) characterPhase = CHARACTERS.length() + characterPhase;
    }

    // endregion Character Phase Functions
}
