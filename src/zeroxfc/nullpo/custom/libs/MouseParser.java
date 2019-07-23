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
