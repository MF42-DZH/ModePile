package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.gui.sdl.MouseInputSDL;
import mu.nu.nullpo.gui.slick.MouseInput;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import org.apache.log4j.Logger;

public class MouseParser {
    /** Mouse button representation. */
    public enum MouseButton {
        LEFT, RIGHT, MIDDLE
    }

    /**
     * Debug Log
     */
    private static final Logger log = Logger.getLogger(MouseParser.class);
    private final CustomResourceHolder.Runtime holderType;

    /**
     * Creates a new instance. All methods are instance methods since
     * they are dependent on which renderer is being used.
     */
    public MouseParser() {
        holderType = CustomResourceHolder.getCurrentNullpominoRuntime();
    }

    /**
     * Updates current mouse input handler.
     */
    public void update() {
        switch (holderType) {
            case SLICK:
                MouseInput.mouseInput.update(NullpoMinoSlick.appGameContainer.getInput());
                break;
            case SDL:
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
     *
     * @return <code>int[2] = { x, y }</code> or <code>int[2] = { -1, -1 }</code> if the current handler does not support mouse
     */
    public int[] getMouseCoordinates() {
        switch (holderType) {
            case SLICK:
            case SDL:
                return new int[] { getMouseX(), getMouseY() };
            default:
                return new int[] { -1, -1 };
        }
    }

    /**
     * Gets the X-coordinate of the mouse position.
     *
     * @return int
     */
    public int getMouseX() {
        switch (holderType) {
            case SLICK:
                return MouseInput.mouseInput.getMouseX();
            case SDL:
                return MouseInputSDL.mouseInput.getMouseX();
            default:
                return -1;
        }
    }

    /**
     * Gets the Y-coordinate of the moust position.
     *
     * @return int
     */
    public int getMouseY() {
        switch (holderType) {
            case SLICK:
                return MouseInput.mouseInput.getMouseY();
            case SDL:
                return MouseInputSDL.mouseInput.getMouseY();
            default:
                return -1;
        }
    }

    /**
     * Gets the coordinates on the player field where the mouse is located on.
     *
     * @param receiver Current game renderer
     * @param engine   Current game engine
     * @param playerID Current player ID
     * @return Mapped coordinates in game field
     */
    public int[] getMouseFieldCoordinates(EventReceiver receiver, GameEngine engine, int playerID) {
        return new int[] { getMouseFieldX(receiver, engine, playerID), getMouseFieldY(receiver, engine, playerID) };
    }

    /**
     * Gets the X coordinate on the player field where the mouse is located on.
     *
     * @param receiver Current game renderer
     * @param engine   Current game engine
     * @param playerID Current player ID
     * @return Mapped X coordinate in game field
     */
    public int getMouseFieldX(EventReceiver receiver, GameEngine engine, int playerID) {
        return (getMouseX() - 4 - receiver.getFieldDisplayPositionX(engine, playerID)) / 16;
    }

    /**
     * Gets the Y coordinate on the player field where the mouse is located on.
     *
     * @param receiver Current game renderer
     * @param engine   Current game engine
     * @param playerID Current player ID
     * @return Mapped Y coordinate in game field
     */
    public int getMouseFieldY(EventReceiver receiver, GameEngine engine, int playerID) {
        return (getMouseY() - 52 - receiver.getFieldDisplayPositionY(engine, playerID)) / 16;
    }

    /**
     * Has the mouse been clicked on the updated frame?
     *
     * @param button Button ID (use the IDs in this class)
     * @return <code>boolean</code>; <code>true</code> = clicked, <code>false</code> = not clicked or was held before
     */
    public boolean getMouseClick(MouseButton button) {
        switch (holderType) {
            case SLICK:
                switch (button) {
                    case LEFT:
                        return MouseInput.mouseInput.isMouseClicked();
                    case RIGHT:
                        return MouseInput.mouseInput.isMouseRightClicked();
                    case MIDDLE:
                        return MouseInput.mouseInput.isMouseMiddleClicked();
                    default:
                        return false;
                }
            case SDL:
                switch (button) {
                    case LEFT:
                        return MouseInputSDL.mouseInput.isMouseClicked();
                    case RIGHT:
                        return MouseInputSDL.mouseInput.isMouseRightClicked();
                    case MIDDLE:
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
     *
     * @param button Button ID (use the IDs in this class)
     * @return <code>boolean</code>; <code>true</code> = clicked or held, <code>false</code> = not clicked nor held
     */
    public boolean getMousePressed(MouseButton button) {
        switch (holderType) {
            case SLICK:
                switch (button) {
                    case LEFT:
                        return MouseInput.mouseInput.isMousePressed();
                    case RIGHT:
                        return MouseInput.mouseInput.isMouseRightPressed();
                    case MIDDLE:
                        return MouseInput.mouseInput.isMouseMiddlePressed();
                    default:
                        return false;
                }
            case SDL:
                switch (button) {
                    case LEFT:
                        return MouseInputSDL.mouseInput.isMousePressed();
                    case RIGHT:
                        return MouseInputSDL.mouseInput.isMouseRightPressed();
                    case MIDDLE:
                        return MouseInputSDL.mouseInput.isMouseMiddlePressed();
                    default:
                        return false;
                }
            default:
                return false;
        }
    }
}
