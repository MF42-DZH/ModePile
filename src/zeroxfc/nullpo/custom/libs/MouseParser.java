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

import mu.nu.nullpo.gui.sdl.MouseInputSDL;
import mu.nu.nullpo.gui.sdl.ResourceHolderSDL;
import mu.nu.nullpo.gui.slick.MouseInput;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.gui.slick.ResourceHolder;
import mu.nu.nullpo.gui.swing.ResourceHolderSwing;

public class MouseParser {
	private static final int HOLDER_SLICK = 0,
			 				 HOLDER_SWING = 1,
			 				 HOLDER_SDL = 2;
	
	public static final int BUTTON_LEFT = 0,
			 			    BUTTON_RIGHT = 1,
			 			    BUTTON_MIDDLE = 2;
	
	private int holderType;
	
	public MouseParser() {
		holderType = -1;
		
		if (ResourceHolder.imgNormalBlockList != null) {
			holderType = HOLDER_SLICK;
		} else if (ResourceHolderSwing.imgNormalBlockList != null) {
			holderType = HOLDER_SWING;
		} else if (ResourceHolderSDL.imgNormalBlockList != null) {
			holderType = HOLDER_SDL;
		}
	}
	
	public void update() {
		switch (holderType) {
		case HOLDER_SLICK:
			MouseInput.mouseInput.update(NullpoMinoSlick.appGameContainer.getInput());
			break;
		case HOLDER_SDL:
			try {
				MouseInputSDL.mouseInput.update();
			} catch (Exception e) {
				// DO NOTHING
			}
			break;
		default:
			break;
		}
	}
	
	public int[] getMouseCoordinates() {
		switch (holderType) {
		case HOLDER_SLICK:
			return new int[] { MouseInput.mouseInput.getMouseX(), MouseInput.mouseInput.getMouseY() };
		case HOLDER_SDL:
			return new int[] { MouseInputSDL.mouseInput.getMouseX(), MouseInputSDL.mouseInput.getMouseY() };
		default:
			return new int[] { -1, -1 };
		}
	}
	
	public int getMouseX() {
		switch (holderType) {
		case HOLDER_SLICK:
			return MouseInput.mouseInput.getMouseX();
		case HOLDER_SDL:
			return MouseInputSDL.mouseInput.getMouseX();
		default:
			return -1;
		}
	}
	
	public int getMouseY() {
		switch (holderType) {
		case HOLDER_SLICK:
			return MouseInput.mouseInput.getMouseY();
		case HOLDER_SDL:
			return MouseInputSDL.mouseInput.getMouseY();
		default:
			return -1;
		}
	}
	
	public boolean getMouseClick(int button) {
		switch (holderType) {
		case HOLDER_SLICK:
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
		case HOLDER_SDL:
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
	
	public boolean getMousePressed(int button) {
		switch (holderType) {
		case HOLDER_SLICK:
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
		case HOLDER_SDL:
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
