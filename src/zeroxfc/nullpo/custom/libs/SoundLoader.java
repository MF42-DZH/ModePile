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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.sound.sampled.Clip;
import mu.nu.nullpo.gui.sdl.ResourceHolderSDL;
import mu.nu.nullpo.gui.sdl.SoundManagerSDL;
import mu.nu.nullpo.gui.slick.ResourceHolder;
import mu.nu.nullpo.gui.slick.SoundManager;
import mu.nu.nullpo.gui.swing.ResourceHolderSwing;
import mu.nu.nullpo.gui.swing.WaveEngine;
import org.apache.log4j.Logger;
import org.newdawn.slick.Sound;
import sdljava.mixer.MixChunk;

public class SoundLoader {
    private static final String CUSTOM_SKIN_DIRECTORY = "custom.skin.directory";
    private static final String SE_ZEROXFC = "/se/zeroxfc/";

    /**
     * Soundpack IDs
     */
    public static final int LOADTYPE_FIREWORKS = 0;
    public static final int LOADTYPE_SCANNER = 1;
    public static final int LOADTYPE_MINESWEEPER = 2;
    public static final int LOADTYPE_COLLAPSE = 3;
    public static final int LOADTYPE_CONSTANTRIS = 4;

    /**
     * Debug log
     */
    private static final Logger log = Logger.getLogger(SoundLoader.class);

    private SoundLoader() {}

    public static void loadSoundset(int loadType) {
        switch (loadType) {
            case LOADTYPE_FIREWORKS:
                importSound("fireworklaunch");
                importSound("fireworkexplode");
                break;
            case LOADTYPE_SCANNER:
                importSound("linescanned");
                importSound("linescannermove");
                break;
            case LOADTYPE_MINESWEEPER:
                importSound("explosion1");
                importSound("explosion2");
                importSound("explosion3");
                importSound("explosion4");
                break;
            case LOADTYPE_COLLAPSE:
                importSound("bombexplode");
                importSound("landing");
                importSound("rise");
                importSound("bonus");
                importSound("bigclear");
                importSound("normalclear");
                importSound("nolanding");
                importSound("noclear");
                break;
            case LOADTYPE_CONSTANTRIS:
                importSound("horn");
                importSound("timeincrease");
                importSound("timereduce");
                break;
            default:
                break;
        }
    }

    /**
     * Private sound import method for use with soundpacks.<br />
     * <br />
     * Credit goes to <code>GlitchyPSI</code> for the base of this code snippet.
     *
     * @param soundName Name of sound in pack
     */
    private static void importSound(String soundName) {
        String skindir = null;
        final CustomResourceHolder.Runtime holderType = CustomResourceHolder.getCurrentNullpominoRuntime();

        switch (holderType) {
            case SLICK:
                skindir = mu.nu.nullpo.gui.slick.NullpoMinoSlick.propConfig.getProperty(CUSTOM_SKIN_DIRECTORY, "res");
                break;
            case SWING:
                skindir = mu.nu.nullpo.gui.swing.NullpoMinoSwing.propConfig.getProperty(CUSTOM_SKIN_DIRECTORY, "res");
                break;
            case SDL:
                skindir = mu.nu.nullpo.gui.sdl.NullpoMinoSDL.propConfig.getProperty(CUSTOM_SKIN_DIRECTORY, "res");
                break;
        }

        if (ResourceHolderSwing.soundManager != null) {
            ResourceHolderSwing.soundManager.load(soundName, skindir + SE_ZEROXFC + soundName + ".wav");
        } else if (ResourceHolder.soundManager != null) {
            ResourceHolder.soundManager.load(soundName, skindir + SE_ZEROXFC + soundName + ".wav");
        } else if (ResourceHolderSDL.soundManager != null) {
            ResourceHolderSDL.soundManager.load(soundName, skindir + SE_ZEROXFC + soundName + ".wav");
        }
    }

    /**
     * Public sound import method when not using a soundpack.
     *
     * @param filePath  Path to sound
     * @param soundName Name to store sound as
     */
    public static void importSound(String filePath, String soundName) {
        if (ResourceHolderSwing.soundManager != null) {
            ResourceHolderSwing.soundManager.load(soundName, filePath);
        } else if (ResourceHolder.soundManager != null) {
            ResourceHolder.soundManager.load(soundName, filePath);
        } else if (ResourceHolderSDL.soundManager != null) {
            ResourceHolderSDL.soundManager.load(soundName, filePath);
        }
    }

    /**
     * Gets all the internal names for all the loaded sounds.<br />
     * Useful for random sound selection or debugging.
     *
     * @return ArrayList containing the internal names for all sounds.
     */
    @SuppressWarnings("unchecked")
    public static List<String> getSoundNames() {
        final ArrayList<String> soundList = new ArrayList<>();
        final CustomResourceHolder.Runtime holderType = CustomResourceHolder.getCurrentNullpominoRuntime();

        /*
         * XXX: welp, i guess it's time to use reflection again... apologies in advance for this atrocity.
         */
        Field localField;
        switch (holderType) {
            case SLICK:
                Class<SoundManager> slickSound = SoundManager.class;
                try {
                    localField = slickSound.getDeclaredField("clipMap");
                    localField.setAccessible(true);

                    /*
                     * This should not return anything other than HashMap<String, Sound>,
                     * as verified in the source code (see SoundManager.java).
                     */
                    HashMap<String, Sound> slickSE = (HashMap<String, Sound>) localField.get(ResourceHolder.soundManager);

                    soundList.addAll(slickSE.keySet());
                    log.info("Sound name loading from Slick soundManager successful.");
                } catch (Exception e) {
                    log.error("Sound name loading from Slick soundManager failed.");
                }

                break;
            case SWING:
                Class<WaveEngine> swingSound = WaveEngine.class;
                try {
                    localField = swingSound.getDeclaredField("clipMap");
                    localField.setAccessible(true);

                    /*
                     * This should not return anything other than HashMap<String, Clip>,
                     * as verified in the source code (see WaveEngine.java).
                     */
                    HashMap<String, Clip> swingSE = (HashMap<String, Clip>) localField.get(ResourceHolderSwing.soundManager);

                    soundList.addAll(swingSE.keySet());
                    log.info("Sound name loading from Swing soundManager successful.");
                } catch (Exception e) {
                    log.error("Sound name loading from Swing soundManager failed.");
                }

                break;
            case SDL:
                Class<SoundManagerSDL> sdlSound = SoundManagerSDL.class;
                try {
                    localField = sdlSound.getDeclaredField("clipMap");
                    localField.setAccessible(true);

                    /*
                     * This should not return anything other than HashMap<String, MixChunk>,
                     * as verified in the source code (see SoundManagerSDL.java).
                     */
                    HashMap<String, MixChunk> sdlSE = (HashMap<String, MixChunk>) localField.get(ResourceHolderSDL.soundManager);

                    soundList.addAll(sdlSE.keySet());
                    log.info("Sound name loading from SDL soundManager successful.");
                } catch (Exception e) {
                    log.error("Sound name loading from SDL soundManager failed.");
                }

                break;
            default:
                return new ArrayList<>();
        }

        return soundList;
    }
}
