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
 *     - Only binaries of this library are allowed to be distributed unless at the
 *       permission of the Library Creator.
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
import mu.nu.nullpo.gui.slick.ResourceHolder;
import mu.nu.nullpo.gui.swing.ResourceHolderSwing;

public class SoundLoader {
	public static final int LOADTYPE_FIREWORKS = 0,
							LOADTYPE_SCANNER = 1,
							LOADTYPE_MINESWEEPER = 2,
							LOADTYPE_COLLAPSE = 3;
	
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
		case LOADTYPE_MINESWEEPER:
			importSound("explosion1");
			importSound("explosion2");
			importSound("explosion3");
			importSound("explosion4");
		case LOADTYPE_COLLAPSE:
			importSound("bombexplode");
			importSound("landing");
			importSound("rise");
			importSound("bonus");
			importSound("bigclear");
			importSound("normalclear");
			importSound("nolanding");
			importSound("noclear");
		default:
			break;
		}
	}
	
	private static void importSound(String soundName) {
		String skindir = null;
		int holderType = -1;
		
		if (ResourceHolder.imgNormalBlockList != null) holderType = HOLDER_SLICK;
		else if (ResourceHolderSwing.imgNormalBlockList != null) holderType = HOLDER_SWING;
		else if (ResourceHolderSDL.imgNormalBlockList != null) holderType = HOLDER_SDL;
		
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

	public static void importSound(String filePath, String soundName) {
		if (ResourceHolderSwing.soundManager != null) {
			ResourceHolderSwing.soundManager.load(soundName, filePath);
		} else if (ResourceHolder.soundManager != null) {
			ResourceHolder.soundManager.load(soundName, filePath);
		} else if (ResourceHolderSDL.soundManager != null) {
			ResourceHolderSDL.soundManager.load(soundName, filePath);
		}
	}
}
