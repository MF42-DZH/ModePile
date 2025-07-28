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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;

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

    public enum TextJustification {
        LEFT, CENTRE, RIGHT, JUSTIFY
    }

    private GameTextUtilities() {}

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
     * Something that can be used to construct a text block. Due to the overhead of all the collection
     * handling, it is recommended that constructing text blocks via this interface only occur once, usually in a
     * static context. This is to minimise performance impact during gameplay.
     */
    @FunctionalInterface
    public interface TextBlockElement {
        Collection<Text> toInsert();
    }

    /**
     * Representation of a piece of text to draw.
     * Do not use newlines directly, use the special newline constructor.
     */
    public static class Text implements TextBlockElement {
        // Cached instances of texts.
        private static final WeakHashMap<Text, WeakReference<Text>> INSTANCES = new WeakHashMap<>();

        public static final int BASE_UNIT = 16;

        public final String string;
        public final int colour;
        public final float scale;
        public final int[] rgba;

        private Text(String string, int colour, float scale, int red, int green, int blue, int alpha) {
            this.string = string;
            this.colour = colour;
            this.scale = scale;
            this.rgba = new int[] { red, green, blue, alpha };
        }

        private static Text getInstance(String string, int colour, float scale) {
            return getInstance(string, colour, scale, 255, 255, 255, 255);
        }

        private static Text getInstance(String string, int colour, float scale, int red, int green, int blue, int alpha) {
            final Text text = new Text(string, colour, scale, red, green, blue, alpha);

            final WeakReference<Text> ref = INSTANCES.get(text);

            if (ref != null) {
                final Text instance = ref.get();
                if (instance != null) return instance;
            }

            INSTANCES.put(text, new WeakReference<>(text));
            return text;
        }

        @Override
        public Collection<Text> toInsert() {
            final Collection<Text> coll = new ArrayList<>(1);
            coll.add(this);

            return coll;
        }

        public static Text of(String string) {
            return getInstance(string, EventReceiver.COLOR_WHITE, 1f);
        }

        public static Text of(String string, int colour) {
            return getInstance(string, colour, 1f);
        }

        public static Text ofSmall(String string) {
            return getInstance(string, EventReceiver.COLOR_WHITE, 0.5f);
        }

        public static Text ofSmall(String string, int colour) {
            return getInstance(string, colour, 0.5f);
        }

        public static Text ofBig(String string) {
            return getInstance(string, EventReceiver.COLOR_WHITE, 2f);
        }

        public static Text ofBig(String string, int colour) {
            return getInstance(string, colour,  2f);
        }

        public static Text custom(String string, int colour, float scale) {
            return getInstance(string, colour, scale);
        }

        public static Text ofAnyColor(String string, int red, int green, int blue, int alpha) {
            return getInstance(string, EventReceiver.COLOR_WHITE, 1f, red, green, blue, alpha);
        }

        public static Text ofAnyColorSmall(String string, int red, int green, int blue, int alpha) {
            return getInstance(string, EventReceiver.COLOR_WHITE, 0.5f, red, green, blue, alpha);
        }

        public static Text ofAnyColorBig(String string, int red, int green, int blue, int alpha) {
            return getInstance(string, EventReceiver.COLOR_WHITE, 2f, red, green, blue, alpha);
        }

        public static Text customAnyColor(String string, int red, int green, int blue, int alpha, float scale) {
            return getInstance(string, EventReceiver.COLOR_WHITE, scale, red, green, blue, alpha);
        }

        public static Text ofMixColor(String string, int receiverColour, int red, int green, int blue, int alpha) {
            return getInstance(string, receiverColour, 1f, red, green, blue, alpha);
        }

        public static Text ofMixColorSmall(String string, int receiverColour, int red, int green, int blue, int alpha) {
            return getInstance(string, receiverColour, 0.5f, red, green, blue, alpha);
        }

        public static Text ofMixColorBig(String string, int receiverColour, int red, int green, int blue, int alpha) {
            return getInstance(string, receiverColour, 2f, red, green, blue, alpha);
        }

        public static Text customMixColor(String string, int receiverColour, int red, int green, int blue, int alpha, float scale) {
            return getInstance(string, receiverColour, scale, red, green, blue, alpha);
        }

        private static final Text NEWLINE = new Text("\n", EventReceiver.COLOR_WHITE, 0f, 255, 255, 255, 255);

        public static Text newLine() {
            return NEWLINE;
        }

        public int getWidth() {
            return (int) (string.length() * BASE_UNIT * scale);
        }

        public int getHeight() {
            return (int) (BASE_UNIT * scale);
        }


        public static TextBlockElement blankLine(float scale) {
            return texts(
                custom(" ", EventReceiver.COLOR_WHITE, scale),
                newLine()
            );
        }

        public boolean isNewLine() {
            return string.equals("\n") && scale == 0f;
        }

        @Override
        public int hashCode() {
            return Objects.hash(string, colour, scale, rgba[0], rgba[1], rgba[2], rgba[3]);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Text)) return false;
            final Text other = (Text) obj;

            return (string.equals(other.string))
                && (colour == other.colour)
                && (scale == other.scale)
                && (rgba[0] == other.rgba[0])
                && (rgba[1] == other.rgba[1])
                && (rgba[2] == other.rgba[2])
                && (rgba[3] == other.rgba[3]);
        }
    }

    /** Collect a set of texts to one collection, for use with the flattening constructor of TextBlock. */
    public static TextBlockElement texts(Text text, Text... otherTexts) {
        return () -> {
            final Collection<Text> result = new ArrayList<>(text.toInsert());
            result.addAll(Arrays.asList(otherTexts));

            return result;
        };
    }

    /** Collect a set of texts to one collection, for use with the flattening constructor of TextBlock. */
    public static TextBlockElement textElems(TextBlockElement text, TextBlockElement... otherTexts) {
        return () -> {
            final Collection<Text> result = new ArrayList<>(text.toInsert());
            Arrays.stream(otherTexts).forEach(t -> result.addAll(t.toInsert()));

            return result;
        };
    }

    /** Representation of a block of lines to draw. */
    public static class TextBlock implements TextBlockElement {
        private static final WeakHashMap<TextBlock, WeakReference<TextBlock>> INSTANCES = new WeakHashMap<>();

        private final Text[] texts;
        private final TextJustification justification;

        private int width = -1;
        private int height = -1;

        private TextBlock(TextJustification justification, Text... texts) {
            this.justification = justification;
            this.texts = texts;
        }

        public static TextBlock of(TextBlockElement firstCollection, TextBlockElement... textCollections) {
            return of(TextJustification.LEFT, firstCollection, textCollections);
        }

        public static TextBlock of(TextJustification justification, TextBlockElement firstCollection, TextBlockElement... textCollections) {
            final Collection<Text> identity = new ArrayList<>(firstCollection.toInsert());
            final Collection<Text> flattened = Arrays.stream(textCollections).sequential().map(TextBlockElement::toInsert).reduce(identity, (l, r) -> { l.addAll(r); return l; });
            return of(justification, flattened.toArray(new Text[0]));
        }

        @SafeVarargs
        public static TextBlock of(Collection<Text> firstCollection, Collection<Text>... textCollections) {
            return of(TextJustification.LEFT, firstCollection, textCollections);
        }

        @SafeVarargs
        public static TextBlock of(TextJustification justification, Collection<Text> firstCollection, Collection<Text>... textCollections) {
            final Collection<Text> identity = new ArrayList<>(firstCollection);
            final Collection<Text> flattened = Arrays.stream(textCollections).sequential().reduce(identity, (l, r) -> { l.addAll(r); return l; });
            return of(justification, flattened.toArray(new Text[0]));
        }

        public static TextBlock of(Text... texts) {
            return of(TextJustification.LEFT, texts);
        }

        public static TextBlock of(TextJustification justification, Text... texts) {
            final TextBlock block = new TextBlock(justification, texts);

            final WeakReference<TextBlock> ref = INSTANCES.get(block);

            if (ref != null) {
                final TextBlock instance = ref.get();
                if (instance != null) return instance;
            }

            INSTANCES.put(block, new WeakReference<>(block));
            return block;
        }

        public static TextBlock of(TextJustification justification, Collection<Text> texts) {
            return of(justification, texts.toArray(new Text[0]));
        }

        public Text get(int i) {
            return texts[i];
        }

        @Override
        public Collection<Text> toInsert() {
            // You will lose justification information if embedding a text block inside another.
            // The outermost text block's justification determines overall justification.
            return Arrays.asList(texts);
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

        @Override
        public int hashCode() {
            return Objects.hash(justification, Arrays.hashCode(texts));
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TextBlock)) return false;
            final TextBlock other = (TextBlock) obj;

            return Arrays.equals(texts, other.texts) && justification == other.justification;
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
        getCustomGraphics().drawString(
            engine,
            startX,
            startY,
            text.string,
            text.colour,
            text.rgba[0],
            text.rgba[1],
            text.rgba[2],
            text.rgba[3],
            text.scale
        );
    }

    public static void drawAlignedText(GameEngine engine, int startX, int startY, Text text) {
        drawAlignedText(engine, startX, startY, text, ObjectAlignment.TOP_LEFT);
    }

    public static void drawAlignedText(GameEngine engine, int startX, int startY, Text text, ObjectAlignment alignment) {
        int offsetX, offsetY;

        switch (alignment) {
            case TOP_MIDDLE:
            case MIDDLE_MIDDLE:
            case BOTTOM_MIDDLE:
                offsetX = text.getWidth() / 2;
                break;
            case TOP_RIGHT:
            case MIDDLE_RIGHT:
            case BOTTOM_RIGHT:
                offsetX = text.getWidth();
                break;
            default:
                offsetX = 0;
                break;
        }

        switch (alignment) {
            case MIDDLE_LEFT:
            case MIDDLE_MIDDLE:
            case MIDDLE_RIGHT:
                offsetY = text.getHeight() / 2;
                break;
            case BOTTOM_LEFT:
            case BOTTOM_MIDDLE:
            case BOTTOM_RIGHT:
                offsetY = text.getHeight();
                break;
            default:
                offsetY = 0;
                break;
        }

        drawDirectText(engine, startX - offsetX, startY - offsetY, text);
    }

    public static void drawAlignedScoreText(EventReceiver receiver, GameEngine engine, int playerID, boolean smallGrid, int x, int y, Text text) {
        drawAlignedScoreText(receiver, engine, playerID, smallGrid, x, y, text, ObjectAlignment.TOP_LEFT);
    }

    public static void drawAlignedScoreText(EventReceiver receiver, GameEngine engine, int playerID, boolean smallGrid, int x, int y, Text text, ObjectAlignment alignment) {
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
        drawAlignedMenuText(receiver, engine, playerID, smallGrid, x, y, text, ObjectAlignment.TOP_LEFT);
    }

    public static void drawAlignedMenuText(EventReceiver receiver, GameEngine engine, int playerID, boolean smallGrid, int x, int y, Text text, ObjectAlignment alignment) {
        int gridSize = smallGrid ? 8 : 16;

        drawAlignedText(
            engine,
            receiver.getFieldDisplayPositionX(engine, playerID) + (x * gridSize) + 4,
            receiver.getFieldDisplayPositionY(engine, playerID) + (y * gridSize) + 52,
            text,
            alignment
        );
    }

    // Helper for line lengths.
    private static int getTextsWidth(final Text[] texts, int startIx, int endIx) {
        int total = 0;
        for (int ix = startIx; ix < endIx; ++ix) {
            total += texts[ix].getWidth();
        }

        return total;
    }

    /**
     * Draws a block of texts defined by a text block.
     *
     * @param engine <code>GameEngine</code> to draw with
     * @param startX Start X-coordinate (Top-Left Corner)
     * @param startY Start Y-coortinate (Top-Right Corner)
     * @param pinTop Pin line to top instead of bottom when varying scale text exists
     * @param texts  The text block to draw
     */
    public static void drawDirectTextBlock(GameEngine engine, int startX, int startY, boolean pinTop, TextBlock texts) {
        int dx = startX;
        int dy = startY;

        // Process all lines.
        int offset = 0;
        while (offset < texts.length()) {
            final int lineEnd = findLineEndIndex(texts, offset);

            switch (texts.justification) {
                case CENTRE: dx += (texts.getWidth() - getTextsWidth(texts.texts, offset, lineEnd)) / 2; break;
                case RIGHT: dx += (texts.getWidth() - getTextsWidth(texts.texts, offset, lineEnd)); break;
                default: break;
            }

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
                    texts.get(i).rgba[0],
                    texts.get(i).rgba[1],
                    texts.get(i).rgba[2],
                    texts.get(i).rgba[3],
                    texts.get(i).scale
                );

                dx += texts.get(i).getWidth();
                if (texts.justification == TextJustification.JUSTIFY) {
                    dx += (texts.getWidth() - getTextsWidth(texts.texts, offset, lineEnd)) / (lineEnd - offset - 1);
                }
            }

            dx = startX;
            dy += (int) (Text.BASE_UNIT * maxLineScale);

            offset = lineEnd + 1;
        }
    }

    /**
     * Draws a block of texts defined by a text block.<br />
     * Doesn't draw text that is outside a specified bounding box.
     *
     * @param engine <code>GameEngine</code> to draw with
     * @param startX Start X-coordinate (Top-Left Corner)
     * @param startY Start Y-coortinate (Top-Right Corner)
     * @param minX   Bounding box's left X
     * @param minY   Bounding box's top Y
     * @param maxX   Bounding box's right X
     * @param maxY   Bounding box's bottom Y
     * @param pinTop Pin line to top instead of bottom when varying scale text exists
     * @param texts  The text block to draw
     */
    public static void drawBoundedDirectTextBlock(GameEngine engine, int startX, int startY, int minX, int minY, int maxX, int maxY, boolean pinTop, TextBlock texts) {
        int dx = startX;
        int dy = startY;

        // Process all lines.
        int offset = 0;
        while (offset < texts.length() && dy <= maxY) {
            final int lineEnd = findLineEndIndex(texts, offset);

            switch (texts.justification) {
                case CENTRE: dx += (texts.getWidth() - getTextsWidth(texts.texts, offset, lineEnd)) / 2; break;
                case RIGHT: dx += (texts.getWidth() - getTextsWidth(texts.texts, offset, lineEnd)); break;
                default: break;
            }

            float maxLineScale = 0f;
            for (int i = offset; i < lineEnd; ++i) {
                maxLineScale = Math.max(texts.get(i).scale, maxLineScale);
            }

            for (int i = offset; i < lineEnd; ++i) {
                final Text text = texts.get(i);

                getCustomGraphics().drawClippedString(
                    engine,
                    dx,
                    pinTop ? dy : dy + (int) ((maxLineScale - texts.get(i).scale) * Text.BASE_UNIT),
                    minX, minY,
                    maxX, maxY,
                    text.string,
                    text.colour,
                    text.rgba[0],
                    text.rgba[1],
                    text.rgba[2],
                    text.rgba[3],
                    text.scale
                );

                dx += texts.get(i).getWidth();
                if (texts.justification == TextJustification.JUSTIFY) {
                    dx += (texts.getWidth() - getTextsWidth(texts.texts, offset, lineEnd)) / (lineEnd - offset - 1);
                }

                if (dx > maxX) break;
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
    public static void drawAlignedTextBlock(GameEngine engine, int startX, int startY, boolean pinTop, TextBlock texts, ObjectAlignment alignment) {
        int offsetX, offsetY;

        switch (alignment) {
            case TOP_MIDDLE:
            case MIDDLE_MIDDLE:
            case BOTTOM_MIDDLE:
                offsetX = texts.getWidth() / 2;
                break;
            case TOP_RIGHT:
            case MIDDLE_RIGHT:
            case BOTTOM_RIGHT:
                offsetX = texts.getWidth();
                break;
            default:
                offsetX = 0;
                break;
        }

        switch (alignment) {
            case MIDDLE_LEFT:
            case MIDDLE_MIDDLE:
            case MIDDLE_RIGHT:
                offsetY = texts.getHeight() / 2;
                break;
            case BOTTOM_LEFT:
            case BOTTOM_MIDDLE:
            case BOTTOM_RIGHT:
                offsetY = texts.getHeight();
                break;
            default:
                offsetY = 0;
                break;
        }

        drawDirectTextBlock(engine, startX - offsetX, startY - offsetY, pinTop, texts);
    }

    /**
     * Draws a block of texts defined by a text block.
     * Text blocks always left-align all lines.
     * <p>
     * Alignment only modifies alignment by the text block's bounding box.
     * Doesn't draw text that is outside a separate bounding box.
     *
     * @param engine         <code>GameEngine</code> to draw with
     * @param startX         Start X-coordinate (Top-Left Corner)
     * @param startY         Start Y-coortinate (Top-Right Corner)
     * @param minX           Bounding box's left X
     * @param minY           Bounding box's top Y
     * @param maxX           Bounding box's right X
     * @param maxY           Bounding box's bottom Y
     * @param pinTop         Pin line to top instead of bottom when varying scale text exists
     * @param texts          The text block to draw
     * @param alignment      Alignment of the texts bounding box
     */
    public static void drawAlignedBoundedTextBlock(GameEngine engine, int startX, int startY, int minX, int minY, int maxX, int maxY, boolean pinTop, TextBlock texts, ObjectAlignment alignment) {
        int offsetX, offsetY;

        switch (alignment) {
            case TOP_MIDDLE:
            case MIDDLE_MIDDLE:
            case BOTTOM_MIDDLE:
                offsetX = texts.getWidth() / 2;
                break;
            case TOP_RIGHT:
            case MIDDLE_RIGHT:
            case BOTTOM_RIGHT:
                offsetX = texts.getWidth();
                break;
            default:
                offsetX = 0;
                break;
        }

        switch (alignment) {
            case MIDDLE_LEFT:
            case MIDDLE_MIDDLE:
            case MIDDLE_RIGHT:
                offsetY = texts.getHeight() / 2;
                break;
            case BOTTOM_LEFT:
            case BOTTOM_MIDDLE:
            case BOTTOM_RIGHT:
                offsetY = texts.getHeight();
                break;
            default:
                offsetY = 0;
                break;
        }

        drawBoundedDirectTextBlock(engine, startX - offsetX, startY - offsetY, minX, minY, maxX, maxY, pinTop, texts);
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
    public static void drawAlignedScoreTextBlock(EventReceiver receiver, GameEngine engine, int playerID, boolean smallGrid, int x, int y, boolean pinTop, TextBlock texts, ObjectAlignment alignment) {
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
    public static void drawAlignedMenuTextBlock(EventReceiver receiver, GameEngine engine, int playerID, boolean smallGrid, int x, int y, boolean pinTop, TextBlock texts, ObjectAlignment alignment) {
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
    public static void drawDirectTextAlign(GameEngine engine, int x, int y, ObjectAlignment alignment, String str, Integer color, Float scale) {
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
    public static void drawScoreTextAlign(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, ObjectAlignment alignment, String str, Integer color, Float scale) {
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
    public static void drawMenuTextAlign(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, ObjectAlignment alignment, String str, Integer color, Float scale) {
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

        drawDirectTextBlock(engine, x, y, false, TextBlock.of(TextJustification.LEFT, chars));
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

        drawAlignedScoreTextBlock(receiver, engine, playerID, scale == 0.5f, x, y, false, TextBlock.of(TextJustification.LEFT, chars), ObjectAlignment.TOP_LEFT);
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

        drawAlignedMenuTextBlock(receiver, engine, playerID, scale == 0.5f, x, y, false, TextBlock.of(TextJustification.LEFT, chars), ObjectAlignment.TOP_LEFT);
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
        final List<Text> chars = new LinkedList<>();

        for (int i = 0; i < str.length(); i++) {
            chars.add(Text.custom(str.substring(i, i + 1), RAINBOW_ORDER[randomEngine.nextInt(RAINBOW_COLOURS)], scale));
        }

        drawDirectTextBlock(engine, x, y, false, TextBlock.of(TextJustification.LEFT, chars));
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
        final List<Text> chars = new LinkedList<>();

        for (int i = 0; i < str.length(); i++) {
            chars.add(Text.custom(str.substring(i, i + 1), RAINBOW_ORDER[randomEngine.nextInt(RAINBOW_COLOURS)], scale));
        }

        drawAlignedScoreTextBlock(receiver, engine, playerID, scale == 0.5f, x, y, false, TextBlock.of(TextJustification.LEFT, chars), ObjectAlignment.TOP_LEFT);
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
        final List<Text> chars = new LinkedList<>();

        for (int i = 0; i < str.length(); i++) {
            chars.add(Text.custom(str.substring(i, i + 1), RAINBOW_ORDER[randomEngine.nextInt(RAINBOW_COLOURS)], scale));
        }

        drawAlignedMenuTextBlock(receiver, engine, playerID, scale == 0.5f, x, y, false, TextBlock.of(TextJustification.LEFT, chars), ObjectAlignment.TOP_LEFT);
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
