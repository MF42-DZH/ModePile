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

import mu.nu.nullpo.game.play.GameEngine;

public class ScrollingMarqueeText {
    // Text size index names
    public static final int SIZE_SMALL = 0,
        SIZE_NORMAL = 1,
        SIZE_LARGE = 2;

    // Sizes of texts
    private static final int[] SIZES = { 8, 16, 32 };

    // Float sizes
    private static final float[] SCALES_FLOAT = { 0.5f, 1f, 2f };

    // Excess length
    private static final int EXCESS_LENGTH = 5;

    // Whole string
    private String mainHeadingString;

    // Whole text string
    private String mainTextString;

    // Main colour
    private final int headingColour;

    // Text colour
    private final int textColour;

    /*
     * Create a staff roll.
     */
    public ScrollingMarqueeText(String[] headingArray, String[] textArray, int hColour, int tColour) {
        // Strings that make up the headings
        // Strings that fill the info under the headings
        headingColour = hColour;
        textColour = tColour;

        mainHeadingString = "";
        mainTextString = "";

        StringBuilder mHS = new StringBuilder(mainHeadingString);
        StringBuilder mTS = new StringBuilder(mainTextString);
        for (int i = 0; i < headingArray.length; i++) {
            mHS.append(new String(new char[headingArray[i].length()]).replace("\0", " ")).append(" ").append(textArray[i]).append((i < headingArray.length - 1) ? " / " : "");
            mTS.append(headingArray[i]).append(" ").append(new String(new char[textArray[i].length()]).replace("\0", " ")).append((i < headingArray.length - 1) ? " / " : "");
        }

        mainHeadingString = mHS.toString();
        mainTextString = mTS.toString();
    }

    /**
     * Automatically draw the roll at a certain Y value.
     *
     * @param engine   Current GameEngine instance
     * @param y        Y-coordinate to draw on
     * @param size     Size of text to draw with
     * @param progress Progress of the roll (0: start, 1: end)
     */
    public void drawAtY(GameEngine engine, double y, int size, double progress) {
        int mainOffset1 = (int) (40 * SIZES[size] / SCALES_FLOAT[size]) - (int) ((progress) * ((40 * SIZES[size]) + ((mainHeadingString.length() + EXCESS_LENGTH) * SIZES[size])));
        int mainOffset2 = (int) (40 * SIZES[size] / SCALES_FLOAT[size]) - (int) ((progress) * ((40 * SIZES[size]) + ((mainTextString.length() + EXCESS_LENGTH) * SIZES[size])));
        GameTextUtilities.drawDirectText(engine, mainOffset1, (int) (y * SIZES[size]), GameTextUtilities.Text.custom(mainHeadingString, headingColour, SCALES_FLOAT[size]));
        GameTextUtilities.drawDirectText(engine, mainOffset2, (int) (y * SIZES[size]), GameTextUtilities.Text.custom(mainTextString, textColour, SCALES_FLOAT[size]));
    }
}
