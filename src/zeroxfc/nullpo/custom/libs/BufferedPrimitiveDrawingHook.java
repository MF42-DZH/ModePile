package zeroxfc.nullpo.custom.libs;

import java.util.ArrayList;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.gui.sdl.RendererSDL;
import mu.nu.nullpo.gui.slick.RendererSlick;
import mu.nu.nullpo.gui.swing.RendererSwing;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.backgroundtypes.AnimatedBackgroundHook;

public class BufferedPrimitiveDrawingHook {
    /**
     * Error logger
     */
    private static final Logger log = Logger.getLogger(BufferedPrimitiveDrawingHook.class);

    /**
     * Drawing command queue.
     */
    private final ArrayList<RenderCommand> commandBuffer;

    /** Temporary holder for EventReceiver. */
    private EventReceiver receiver = null;

    /** Lazy instantiator for graphics object. */
    private final LazyReference<Object> USER_GRAPHICS_OBJECT = LazyReference.fromCallable(() -> {
        switch (AnimatedBackgroundHook.getResourceHook()) {
            case AnimatedBackgroundHook.HOLDER_SLICK:
                // Slick graphics object
                return CustomResourceHolder.getGraphicsSlick((RendererSlick) receiver);
            case AnimatedBackgroundHook.HOLDER_SWING:
                // Swing graphics object
                return CustomResourceHolder.getGraphicsSwing((RendererSwing) receiver);
            case AnimatedBackgroundHook.HOLDER_SDL:
                // SDL graphics object
                return CustomResourceHolder.getGraphicsSDL((RendererSDL) receiver);
            default:
                throw new LazyLoadException("Cannot find renderer!");
        }
    });

/**
 * Create a new queue.
 */
public BufferedPrimitiveDrawingHook() {
    commandBuffer = new ArrayList<>();
    }

    /**
     * These can be placed in the non-render methods.
     */
    public void drawRectangle(int x, int y, int sizeX, int sizeY, int red, int green, int blue, int alpha, boolean fill) {
        commandBuffer.add(
            new RenderCommand(
                RenderCommand.RenderType.Rectangle,
                new Object[] { x, y, sizeX, sizeY, red, green, blue, alpha, fill }
            )
        );
    }

    /**
     * These can be placed in the non-render methods.
     */
    public void drawArc(int x, int y, int sizeX, int sizeY, int angleStart, int angleSize, int red, int green, int blue, int alpha, boolean fill) {
        commandBuffer.add(
            new RenderCommand(
                RenderCommand.RenderType.Arc,
                new Object[] { x, y, sizeX, sizeY, angleStart, angleSize, red, green, blue, alpha, fill }
            )
        );
    }

    /**
     * These can be placed in the non-render methods.
     */
    public void drawOval(int x, int y, int sizeX, int sizeY, int red, int green, int blue, int alpha, boolean fill) {
        commandBuffer.add(
            new RenderCommand(
                RenderCommand.RenderType.Rectangle,
                new Object[] { x, y, sizeX, sizeY, red, green, blue, alpha, fill }
            )
        );
    }

    /**
     * Draws all the commands to the renderer. Recommended to use in <code>renderLast</code>.
     *
     * @param receiver Renderer to use
     */
    public void renderAll(EventReceiver receiver) {
        if (commandBuffer.size() <= 0) return;

        try {
            // Get our graphics object.
            if (this.receiver == null) {
                this.receiver = receiver;
            }

            final Object graphicsObject = USER_GRAPHICS_OBJECT.getValue();

            if (graphicsObject == null) return;
            for (RenderCommand cmd : commandBuffer) {
                try {
                    switch (cmd.renderType) {
                        case Rectangle:
                            PrimitiveDrawingHook.drawRectangleFast(
                                graphicsObject,
                                (int) cmd.args[0],
                                (int) cmd.args[1],
                                (int) cmd.args[2],
                                (int) cmd.args[3],
                                (int) cmd.args[4],
                                (int) cmd.args[5],
                                (int) cmd.args[6],
                                (int) cmd.args[7],
                                (boolean) cmd.args[8]
                            );
                            break;
                        case Arc:
                            PrimitiveDrawingHook.drawArcFast(
                                graphicsObject,
                                (int) cmd.args[0],
                                (int) cmd.args[1],
                                (int) cmd.args[2],
                                (int) cmd.args[3],
                                (int) cmd.args[4],
                                (int) cmd.args[5],
                                (int) cmd.args[6],
                                (int) cmd.args[7],
                                (int) cmd.args[8],
                                (int) cmd.args[9],
                                (boolean) cmd.args[10]
                            );
                            break;
                        case Oval:
                            PrimitiveDrawingHook.drawOvalFast(
                                graphicsObject,
                                (int) cmd.args[0],
                                (int) cmd.args[1],
                                (int) cmd.args[2],
                                (int) cmd.args[3],
                                (int) cmd.args[4],
                                (int) cmd.args[5],
                                (int) cmd.args[6],
                                (int) cmd.args[7],
                                (boolean) cmd.args[8]
                            );
                            break;
                        default:
                            log.error("Invalid render type.");
                            break;
                    }
                } catch (Exception e) {
                    log.error("Error encountered.", e);
                    break;
                }
            }
            commandBuffer.clear();
        } catch (Exception e) {
            log.error("Something went wrong.", e);
        }
    }
}
