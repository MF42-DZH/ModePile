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

import mu.nu.nullpo.gui.sdl.MouseInputSDL;
import mu.nu.nullpo.gui.slick.MouseInput;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.backgroundtypes.AnimatedBackgroundHook;

public class MouseParser {
	/** Debug Log */
	private static final Logger log = Logger.getLogger(MouseParser.class);

	/** Mouse Button IDs */
	public static final int BUTTON_LEFT = 0,
			 			    BUTTON_RIGHT = 1,
			 			    BUTTON_MIDDLE = 2;
	
	private int holderType;

	/**
	 * Creates a new instance. All methods are instance methods since
	 * they are dependent on which renderer is being used.
	 */
	public MouseParser() {
		holderType = AnimatedBackgroundHook.getResourceHook();
	}

	/**
	 * Updates current mouse input handler.
	 */
	public void update() {
		switch (holderType) {
		case AnimatedBackgroundHook.HOLDER_SLICK:
			MouseInput.mouseInput.update(NullpoMinoSlick.appGameContainer.getInput());
			break;
		case AnimatedBackgroundHook.HOLDER_SDL:
			try {
				MouseInputSDL.mouseInput.update();
			} catch (Exception e) {
				log.debug("Failed to update SDL's mouse input:\n", e);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Gets the current coordinates of the mouse.
	 * @return <code>int[2] = { x, y }</code> or <code>int[2] = { -1, -1 }</code> if the current handler does not support mouse
	 */
	public int[] getMouseCoordinates() {
		switch (holderType) {
		case AnimatedBackgroundHook.HOLDER_SLICK:
		case AnimatedBackgroundHook.HOLDER_SDL:
			return new int[] { getMouseX(), getMouseY() };
		default:
			return new int[] { -1, -1 };
		}
	}

	/**
	 * Gets the X-coordinate of the mouse position.
	 * @return int
	 */
	public int getMouseX() {
		switch (holderType) {
		case AnimatedBackgroundHook.HOLDER_SLICK:
			return MouseInput.mouseInput.getMouseX();
		case AnimatedBackgroundHook.HOLDER_SDL:
			return MouseInputSDL.mouseInput.getMouseX();
		default:
			return -1;
		}
	}

	/**
	 * Gets the Y-coordinate of the moust position.
	 * @return int
	 */
	public int getMouseY() {
		switch (holderType) {
		case AnimatedBackgroundHook.HOLDER_SLICK:
			return MouseInput.mouseInput.getMouseY();
		case AnimatedBackgroundHook.HOLDER_SDL:
			return MouseInputSDL.mouseInput.getMouseY();
		default:
			return -1;
		}
	}

	/**
	 * Has the mouse been clicked on the updated frame?
	 * @param button Button ID (use the IDs in this class)
	 * @return <code>boolean</code>; <code>true</code> = clicked, <code>false</code> = not clicked or was held before
	 */
	public boolean getMouseClick(int button) {
		switch (holderType) {
		case AnimatedBackgroundHook.HOLDER_SLICK:
			switch (button) {
			case BUTTON_LEFT:
				return MouseInput.mouseInput.isMouseClicked();
			case BUTTON_RIGHT:
				return MouseInput.mouseInput.isMouseRightClicked();
			case BUTTON_MIDDLE:
				return MouseInput.mouseInput.isMouseMiddleClicked();
			default:
				return false;
			}
		case AnimatedBackgroundHook.HOLDER_SDL:
			switch (button) {
			case BUTTON_LEFT:
				return MouseInputSDL.mouseInput.isMouseClicked();
			case BUTTON_RIGHT:
				return MouseInputSDL.mouseInput.isMouseRightClicked();
			case BUTTON_MIDDLE:
				return MouseInputSDL.mouseInput.isMouseMiddleClicked();
			default:
				return false;
			}
		default:
			return false;
		}
	}

	/**
	 * Has the mouse been clicked or held on the updated frame?
	 * @param button Button ID (use the IDs in this class)
	 * @return <code>boolean</code>; <code>true</code> = clicked or held, <code>false</code> = not clicked nor held
	 */
	public boolean getMousePressed(int button) {
		switch (holderType) {
		case AnimatedBackgroundHook.HOLDER_SLICK:
			switch (button) {
			case BUTTON_LEFT:
				return MouseInput.mouseInput.isMousePressed();
			case BUTTON_RIGHT:
				return MouseInput.mouseInput.isMouseRightPressed();
			case BUTTON_MIDDLE:
				return MouseInput.mouseInput.isMouseMiddlePressed();
			default:
				return false;
			}
		case AnimatedBackgroundHook.HOLDER_SDL:
			switch (button) {
			case BUTTON_LEFT:
				return MouseInputSDL.mouseInput.isMousePressed();
			case BUTTON_RIGHT:
				return MouseInputSDL.mouseInput.isMouseRightPressed();
			case BUTTON_MIDDLE:
				return MouseInputSDL.mouseInput.isMouseMiddlePressed();
			default:
				return false;
			}
		default:
			return false;
		}
	}
}
