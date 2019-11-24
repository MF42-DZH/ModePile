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

import mu.nu.nullpo.gui.sdl.ResourceHolderSDL;
import mu.nu.nullpo.gui.sdl.SoundManagerSDL;
import mu.nu.nullpo.gui.slick.ResourceHolder;
import mu.nu.nullpo.gui.slick.SoundManager;
import mu.nu.nullpo.gui.swing.ResourceHolderSwing;
import mu.nu.nullpo.gui.swing.WaveEngine;
import org.apache.log4j.Logger;
import org.newdawn.slick.Sound;
import sdljava.mixer.MixChunk;
import zeroxfc.nullpo.custom.libs.backgroundtypes.AnimatedBackgroundHook;

import javax.sound.sampled.Clip;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class SoundLoader {
	/** Debug log */
	private static final Logger log = Logger.getLogger(SoundLoader.class);

	/** Soundpack IDs */
	public static final int LOADTYPE_FIREWORKS = 0,
							LOADTYPE_SCANNER = 1,
							LOADTYPE_MINESWEEPER = 2,
							LOADTYPE_COLLAPSE = 3,
	                        LOADTYPE_CONSTANTRIS = 4;
	
	private static final int HOLDER_SLICK = 0,
							 HOLDER_SWING = 1,
							 HOLDER_SDL = 2;
	
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
	 * @param soundName Name of sound in pack
	 */
	private static void importSound(String soundName) {
		String skindir = null;
		int holderType = -1;

		String mainClass = ResourceHolderCustomAssetExtension.getMainClassName();

		if (mainClass.contains("Slick")) holderType = HOLDER_SLICK;
		else if (mainClass.contains("Swing")) holderType = HOLDER_SWING;
		else if (mainClass.contains("SDL")) holderType = HOLDER_SDL;
		
		switch (holderType) {
		case HOLDER_SLICK:
			skindir = mu.nu.nullpo.gui.slick.NullpoMinoSlick.propConfig.getProperty("custom.skin.directory", "res");
			break;
		case HOLDER_SWING:
			skindir = mu.nu.nullpo.gui.swing.NullpoMinoSwing.propConfig.getProperty("custom.skin.directory", "res");
			break;
		case HOLDER_SDL:
			skindir = mu.nu.nullpo.gui.sdl.NullpoMinoSDL.propConfig.getProperty("custom.skin.directory", "res");
			break;
		}
		
		if (ResourceHolderSwing.soundManager != null) {
			ResourceHolderSwing.soundManager.load(soundName, skindir + "/se/zeroxfc/" + soundName + ".wav");
		} else if (ResourceHolder.soundManager != null) {
			ResourceHolder.soundManager.load(soundName, skindir + "/se/zeroxfc/" + soundName + ".wav");
		} else if (ResourceHolderSDL.soundManager != null) {
			ResourceHolderSDL.soundManager.load(soundName, skindir + "/se/zeroxfc/" + soundName + ".wav");
		}
	}

	/**
	 * Public sound import method when not using a soundpack.
	 * @param filePath Path to sound
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
	 * @return ArrayList containing the internal names for all sounds.
	 */
	public static ArrayList<String> getSoundNames() {
		ArrayList<String> soundList = new ArrayList<>();
		final int holderType = AnimatedBackgroundHook.getResourceHook();

		/*
		 * XXX: welp, i guess it's time to use reflection again... apologies in advance for this atrocity.
		 */
		Field localField;
		switch (holderType) {
			case AnimatedBackgroundHook.HOLDER_SLICK:
				Class<SoundManager> slickSound = SoundManager.class;
				try {
					localField = slickSound.getDeclaredField("clipMap");
					localField.setAccessible(true);

					/*
					 * This should not return anything other than HashMap<String, Sound>,
					 * as verified in the source code (see SoundManager.java).
					 *
					 * Use @SuppressWarnings("unchecked").
					 */
					HashMap<String, Sound> slickSE = (HashMap<String, Sound>)localField.get(ResourceHolder.soundManager);

					soundList.addAll(slickSE.keySet());
					log.info("Sound name loading from Slick soundManager successful.");
				} catch (Exception e) {
					log.error("Sound name loading from Slick soundManager failed.");
				}

				break;
			case AnimatedBackgroundHook.HOLDER_SWING:
				Class<WaveEngine> swingSound = WaveEngine.class;
				try {
					localField = swingSound.getDeclaredField("clipMap");
					localField.setAccessible(true);

					/*
					 * This should not return anything other than HashMap<String, Clip>,
					 * as verified in the source code (see WaveEngine.java).
					 *
					 * Use @SuppressWarnings("unchecked").
					 */
					HashMap<String, Clip> swingSE = (HashMap<String, Clip>)localField.get(ResourceHolderSwing.soundManager);

					soundList.addAll(swingSE.keySet());
					log.info("Sound name loading from Swing soundManager successful.");
				} catch (Exception e) {
					log.error("Sound name loading from Swing soundManager failed.");
				}

				break;
			case AnimatedBackgroundHook.HOLDER_SDL:
				Class<SoundManagerSDL> sdlSound = SoundManagerSDL.class;
				try {
					localField = sdlSound.getDeclaredField("clipMap");
					localField.setAccessible(true);

					/*
					 * This should not return anything other than HashMap<String, MixChunk>,
					 * as verified in the source code (see SoundManagerSDL.java).
					 *
					 * Use @SuppressWarnings("unchecked").
					 */
					HashMap<String, MixChunk> sdlSE = (HashMap<String, MixChunk>)localField.get(ResourceHolderSDL.soundManager);

					soundList.addAll(sdlSE.keySet());
					log.info("Sound name loading from SDL soundManager successful.");
				} catch (Exception e) {
					log.error("Sound name loading from SDL soundManager failed.");
				}

				break;
			default:
				return null;
		}

		return soundList;
	}
}
