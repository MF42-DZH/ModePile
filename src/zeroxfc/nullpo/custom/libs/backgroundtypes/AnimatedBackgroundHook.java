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
package zeroxfc.nullpo.custom.libs.backgroundtypes;

import java.lang.reflect.Field;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.gui.sdl.NullpoMinoSDL;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.gui.swing.NullpoMinoSwing;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;

public abstract class AnimatedBackgroundHook {
    /**
     * Animation type ID
     * <p>
     * Please add a new one for every new background type made.
     */
    public static final int ANIMATION_NONE = 0,                   // No animation. Essentially a quick-switchable custom BG.
        ANIMATION_FRAME_ANIM = 1,             // Full frame 640x480 animation at up to 60 FPS. Uses a single image.
        ANIMATION_IMAGE_SEQUENCE_ANIM = 2,    // Full frame 640x480 animation at up to 60 FPS. Uses a sequence of images.
        ANIMATION_PULSE_HORIZONTAL_BARS = 3,  // Pulsating horizontal bars, like waves across a pseudo-fluid.
        ANIMATION_PULSE_VERTICAL_BARS = 4,    // Pulsating vertical bars, like waves across a pseudo-fluid.
        ANIMATION_CIRCULAR_RIPPLE = 5,        // Droplets on a smooth psudo-liquid.
        ANIMATION_DIAGONAL_RIPPLE = 6,        // Droplets on a smooth psudo-liquid.
        ANIMATION_SLIDING_TILES = 7,          // NOTE: SDL window handling is gross.
        ANIMATION_TGM3TI_STYLE = 8,           // NOTE: Swing and SDL will not be able to use rotations.
        ANIMATION_INTERLACE_HORIZONTAL = 9,   // I hope you like Earthbound.
        ANIMATION_INTERLACE_VERTICAL = 10,    // I hope you like Earthbound.
        ANIMATION_FAKE_SCANLINES = 11;        // Fake CRT Scanlines.
    /**
     * ResourceHolder--- types
     */
    public static final int HOLDER_SLICK = 0,
        HOLDER_SWING = 1,
        HOLDER_SDL = 2;
    protected static Logger log = Logger.getLogger(AnimatedBackgroundHook.class);
    private static int LAST_BG = -1;
    private static int LAST_FADE_BG = -1;
    /**
     * Stored ResourceHolder--- type
     */
    private static int ResourceHolderType = -1;
    protected int ID;
    protected CustomResourceHolder customHolder;
    protected String imageName;

    /**
     * Gets the current resource holder type.<br />
     * Useful for selecting different renderers, sound engines or input handlers.
     *
     * @return Integer that represents the holder type.
     */
    public static int getResourceHook() {
        if (ResourceHolderType < 0) {
            String mainClass = CustomResourceHolder.getMainClassName();

            if (mainClass.contains("Slick")) ResourceHolderType = HOLDER_SLICK;
            else if (mainClass.contains("Swing")) ResourceHolderType = HOLDER_SWING;
            else if (mainClass.contains("SDL")) ResourceHolderType = HOLDER_SDL;
            else ResourceHolderType = -1;
        }

        return ResourceHolderType;
    }

    /**
     * Gets the last valid background number stored in a GameManager instance.
     *
     * @param owner GameManager instance to check
     * @return Background number (0 <= bg < 19)
     */
    public static int getBGState(GameManager owner) {
        int bg = owner.backgroundStatus.bg;
        if (bg < 0 || bg > 19) return LAST_BG;
        else {
            LAST_BG = bg;
            return bg;
        }
    }

    /**
     * Gets the last valid background number stored in a GameManager instance.
     *
     * @param owner GameManager instance to check
     * @return Background number (0 <= bg < 19)
     */
    public static int getFadeBGState(GameManager owner) {
        int bg = owner.backgroundStatus.fadebg;
        if (bg < 0 || bg > 19) return LAST_FADE_BG;
        else {
            LAST_FADE_BG = bg;
            return bg;
        }
    }

    /**
     * Disables the current background.
     *
     * @param owner GameManager to disable BG in.
     */
    public static void disableDefaultBG(GameManager owner) {
        getBGState(owner);
        getFadeBGState(owner);

        owner.backgroundStatus.bg = -1;
        owner.backgroundStatus.fadebg = -1;
    }

    /**
     * Re-enables the current background.
     *
     * @param owner GameManager to re-enable BG in.
     */
    public static void enableDefaultBG(GameManager owner) {
        owner.backgroundStatus.bg = LAST_BG;
        owner.backgroundStatus.fadebg = LAST_FADE_BG;
    }

    /**
     * Gets the value of the setting that dictates whether backgrounds should be drawn.
     *
     * @return <code>boolean</code>; <code>true</code> = enabled, <code>false</code> = disabled.
     */
    @Deprecated
    public static boolean getInitialBGState() {
        switch (getResourceHook()) {
            case HOLDER_SLICK:
                return NullpoMinoSlick.propConfig.getProperty("option.showbg", true);
            case HOLDER_SWING:
                return NullpoMinoSwing.propConfig.getProperty("option.showbg", true);
            case HOLDER_SDL:
                return NullpoMinoSDL.propConfig.getProperty("option.showbg", true);
            default:
                return false;
        }
    }

    /**
     * Overload of <code>getBGState()</code> that checks if the current renderer has
     * background rendering enabled.
     *
     * @param receiver Renderer to check
     * @return <code>boolean</code>; <code>true</code> = enabled, <code>false</code> = disabled.
     */
    public static boolean getBGState(EventReceiver receiver) {
        try {
            Class<EventReceiver> renderer = EventReceiver.class;
            Field localField;
            localField = renderer.getDeclaredField("showbg");
            localField.setAccessible(true);
            final boolean b = localField.getBoolean(receiver);
            log.info("showbg in current EventReceiver gotten successfully as " + b);
            return b;
        } catch (Exception e) {
            log.error("Access to showbg in current EventReceiver failed.");
            return false;
        }
    }

    /**
     * Sets the toggle on background rendering in the current renderer.
     *
     * @param receiver Renderer to modify
     * @param bool     State to set it to
     */
    @Deprecated
    public static void setBGState(EventReceiver receiver, boolean bool) {
        try {
            Class<EventReceiver> renderer = EventReceiver.class;
            Field localField;
            localField = renderer.getDeclaredField("showbg");
            localField.setAccessible(true);
            if (localField.getBoolean(receiver) != bool) {
                localField.setBoolean(receiver, bool);
                log.info("showbg in current EventReceiver set successfully to " + bool);
            }
        } catch (Exception e) {
            log.error("Access to showbg in current EventReceiver failed.");
        }
    }

    // Fuzzy equals.
    protected static boolean almostEqual(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
    }

    public void setExternalHolder(CustomResourceHolder holder) {
        customHolder = holder;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * Performs an update tick on the background. Advisably used in onLast.
     */
    public abstract void update();

    /**
     * Resets the background to its base state.
     */
    public abstract void reset();

    /**
     * Draws the background to the game screen.
     *
     * @param engine   Current GameEngine instance
     * @param playerID Current player ID (1P = 0)
     */
    public abstract void draw(GameEngine engine, int playerID);

    /**
     * Change BG to one of the default ones.
     *
     * @param bg New BG number
     */
    public abstract void setBG(int bg);

    /**
     * Change BG to a custom BG using its file path.
     *
     * @param filePath File path of new background
     */
    public abstract void setBG(String filePath);

    /**
     * Allows the hot-swapping of pre-loaded BGs from a storage instance of a <code>ResourceHolderCustomAssetExtension</code>.
     *
     * @param holder Storage instance
     * @param name   Image name
     */
    public abstract void setBGFromHolder(CustomResourceHolder holder, String name);

    /**
     * This last one is important. In the case that any of the child types are used, it allows identification.
     * The identification can be used to allow casting during operations.
     *
     * @return Identification number of child class.
     */
    public abstract int getID();
}
