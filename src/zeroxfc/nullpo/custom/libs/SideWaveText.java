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

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;

public class SideWaveText {
    public static final int MaxLifeTime = 120;
    private static final DoubleVector VerticalVelocity = new DoubleVector(0, -1 * (4.0 / 5.0), false);
    private final String text;
    private final double offsetMax;
    private final double sinFrequency;
    private final boolean big;
    private final boolean lClear;
    private DoubleVector position;
    private double xOffset;
    private double sinPhase;
    private int lifeTime;

    /**
     * Creates a Super Collapse II-styled score popup that flies away.<br />
     * For now, drawing is manual.
     *
     * @param x           X-coordinate of centre (?)
     * @param y           Y-coordinate of centre (?)
     * @param frequency   Waving frequency
     * @param offsetWidth Max offset
     * @param text        Text to draw
     * @param big         Should it be double size?
     * @param largeclear  Should it flash and actually wave?
     */
    public SideWaveText(int x, int y, double frequency, double offsetWidth, String text, boolean big, boolean largeclear) {
        this.text = text;
        position = new DoubleVector(x, y, false);

        sinPhase = 0.0;
        sinFrequency = frequency;
        offsetMax = offsetWidth;
        this.big = big;
        lClear = largeclear;

        xOffset = 0;
        lifeTime = 0;
    }

    /**
     * Updates the instance to a new position.
     */
    public void update() {
        sinPhase += sinFrequency * ((Math.PI * 2) / 60);

        xOffset = offsetMax * Math.sin(sinPhase);

        position = DoubleVector.add(position, VerticalVelocity);

        lifeTime++;
    }

    /**
     * Gets the current location of the text for manual drawing.
     *
     * @return A 2-long int[] in the format { x, y }
     */
    public int[] getLocation() {
        return new int[] { (int) (position.getX() + xOffset), (int) position.getY() };
    }

    /**
     * Automatic drawing of the text object.
     *
     * @param receiver Renderer to draw on
     * @param engine   Current GameEngine instance
     * @param playerID Current player ID
     * @param flag     true ? orange : yellow
     */
    public void drawCentral(EventReceiver receiver, GameEngine engine, int playerID, boolean flag) {
        int[] location = getLocation();
        final int x = location[0];
        final int y = location[1];

        int color = EventReceiver.COLOR_ORANGE;
        if (flag) color = EventReceiver.COLOR_YELLOW;

        float scale = 0;
        float baseScale = big ? 2 : 1;
        if (lifeTime < 24) {
            scale = baseScale;
        } else {
            scale = baseScale - (baseScale * ((float) (lifeTime - 24) / 96));
        }

        GameTextUtilities.drawDirectTextAlign(engine, x, y, GameTextUtilities.ALIGN_MIDDLE_MIDDLE, text, color, scale);
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public boolean getBig() {
        return big;
    }

    public boolean getLargeClear() {
        return lClear;
    }

    public String getText() {
        return text;
    }
}
