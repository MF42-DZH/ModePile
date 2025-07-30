package zeroxfc.nullpo.custom.libs.mixins;

import java.util.function.IntBinaryOperator;
import java.util.function.IntSupplier;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.RendererExtension;

// Mix this into gamemode classes to add flexible custom field drawing.
// Make sure to set engine.isVisible to false, and set the current background to -1.
public interface HasCustomFieldDrawing {
    class FrameDrawingParameters {
        // Both of these are expected to return RGB24 in the lower 24 bits.
        private final IntBinaryOperator frameColouringFunction;
        private final IntSupplier meterColouringFunction;

        public FrameDrawingParameters(IntBinaryOperator frameColouringFunction, IntSupplier meterColouringFunction) {
            // Only the frame colouring function needs to be not null when this is instantiated.
            assert (frameColouringFunction != null);

            this.frameColouringFunction = frameColouringFunction;
            this.meterColouringFunction = meterColouringFunction;
        }
    }

    // Return null to use default field frame drawing.
    FrameDrawingParameters getFrameDrawingParameters(GameEngine engine, int playerID);

    // Fade from this background.
    int getLastBackground();

    // Currently selected background.
    int getCurrentBackground();

    // Background fade progress.
    float getFadeProgress();

    // Set up the game to support this mixin's implementations:
    default void setupBackgrounds(GameEngine engine) {
        engine.isVisible = false;
        engine.owner.backgroundStatus.bg = -1;
        engine.owner.backgroundStatus.fadebg = -1;
    }

    // Draw current background. Also draw things on top of the background but behind the field here.
    default void drawBackgroundElements(RendererExtension rendererExtension, EventReceiver receiver, GameEngine engine, int playerID) {
        rendererExtension.drawFadingBackground(receiver, engine, getLastBackground(), getCurrentBackground(), getFadeProgress());
    }

    // Any drawing code you want to put in between the frame/field bg and blocks.
    default void drawBetweenFrameAndField(RendererExtension rendererExtension, EventReceiver receiver, GameEngine engine, int playerID) {
        // By default, this does nothing at all.
    }

    // Call this in a renderFirst override.
    default void inRenderFirst(RendererExtension rendererExtension, EventReceiver receiver, GameEngine engine, int playerID) {
        drawBackgroundElements(rendererExtension, receiver, engine, playerID);

        int offsetX = receiver.getFieldDisplayPositionX(engine, playerID);
        int offsetY = receiver.getFieldDisplayPositionY(engine, playerID);

        final FrameDrawingParameters params = getFrameDrawingParameters(engine, playerID);

        if (engine.displaysize != -1) {
            rendererExtension.drawNext(receiver, engine, offsetX, offsetY);

            if (params == null) {
                rendererExtension.drawFrame(receiver, engine, offsetX, offsetY + 48, engine.displaysize);
            } else {
                if (params.meterColouringFunction == null) {
                    rendererExtension.drawCustomFrame(receiver, engine, offsetX, offsetY + 48, engine.displaysize, params.frameColouringFunction);
                } else {
                    final int meterColourRaw = params.meterColouringFunction.getAsInt();
                    final int red = (meterColourRaw >>> 16) & 0xFF;
                    final int green = (meterColourRaw >>> 8) & 0xFF;
                    final int blue = meterColourRaw & 0xFF;

                    rendererExtension.drawCustomFrame(receiver, engine, offsetX, offsetY + 48, engine.displaysize, params.frameColouringFunction, red, green, blue);
                }
            }
        } else {
            if (params == null) {
                rendererExtension.drawFrame(receiver, engine, offsetX, offsetY, -1);
            } else {
                if (params.meterColouringFunction == null) {
                    rendererExtension.drawCustomFrame(receiver, engine, offsetX, offsetY, -1, params.frameColouringFunction);
                } else {
                    final int meterColourRaw = params.meterColouringFunction.getAsInt();
                    final int red = (meterColourRaw >>> 16) & 0xFF;
                    final int green = (meterColourRaw >>> 8) & 0xFF;
                    final int blue = meterColourRaw & 0xFF;

                    rendererExtension.drawCustomFrame(receiver, engine, offsetX, offsetY, -1, params.frameColouringFunction, red, green, blue);
                }
            }
        }

        drawBetweenFrameAndField(rendererExtension, receiver, engine, playerID);

        if (engine.displaysize != -1) {
            rendererExtension.drawField(receiver, engine, offsetX + 4, offsetY + 52, engine.displaysize);
        } else {
            rendererExtension.drawField(receiver, engine, offsetX + 4, offsetY + 4, -1);
        }
    }

    // Call this in a renderMove override.
    default void inRenderMove(RendererExtension rendererExtension, EventReceiver receiver, GameEngine engine, int playerID) {
        engine.isVisible = true;
        receiver.renderMove(engine, playerID);
        engine.isVisible = false;
    }

    // Call this in a renderMove override.
    default void inRenderExcellent(RendererExtension rendererExtension, EventReceiver receiver, GameEngine engine, int playerID) {
        engine.isVisible = true;
        receiver.renderExcellent(engine, playerID);
        engine.isVisible = false;
    }

    // Call this in a renderMove override.
    default void inRenderGameOver(RendererExtension rendererExtension, EventReceiver receiver, GameEngine engine, int playerID) {
        engine.isVisible = true;
        receiver.renderGameOver(engine, playerID);
        engine.isVisible = false;
    }

    // Call this in a renderMove override.
    default void inRenderResult(RendererExtension rendererExtension, EventReceiver receiver, GameEngine engine, int playerID) {
        engine.isVisible = true;
        receiver.renderResult(engine, playerID);
        engine.isVisible = false;
    }
}
