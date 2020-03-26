package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.gui.sdl.RendererSDL;
import mu.nu.nullpo.gui.slick.RendererSlick;
import mu.nu.nullpo.gui.swing.RendererSwing;
import org.apache.log4j.Logger;
import org.newdawn.slick.Graphics;
import sdljava.video.SDLSurface;
import zeroxfc.nullpo.custom.libs.backgroundtypes.AnimatedBackgroundHook;

import java.awt.*;
import java.util.ArrayList;

public class BufferedPrimitiveDrawingHook {
	/** Error logger */
	private static final Logger log = Logger.getLogger(BufferedPrimitiveDrawingHook.class);

	/** Drawing command queue. */
	private ArrayList<RenderCommand> commandBuffer;

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
	 * @param receiver Renderer to use
	 */
	public void renderAll(EventReceiver receiver) {
		Object graphicsObject = null;
		switch (AnimatedBackgroundHook.getResourceHook()) {
			case AnimatedBackgroundHook.HOLDER_SLICK:
				// Slick graphics object
				graphicsObject = ResourceHolderCustomAssetExtension.getGraphicsSlick((RendererSlick) receiver);
				break;
			case AnimatedBackgroundHook.HOLDER_SWING:
				// Swing graphics object
				graphicsObject = ResourceHolderCustomAssetExtension.getGraphicsSwing((RendererSwing) receiver);
			break;
			case AnimatedBackgroundHook.HOLDER_SDL:
				// SDL graphics object
				graphicsObject = ResourceHolderCustomAssetExtension.getGraphicsSDL((RendererSDL) receiver);
				break;
			default:
				log.error("Could not extract renderer.");
				break;
		}

		if (graphicsObject == null) return;

		while (true) {
			try {
				if (commandBuffer.size() <= 0) break;
				else {
					RenderCommand cmd = commandBuffer.get(0);

					switch (cmd.renderType) {
						case Rectangle:
							PrimitiveDrawingHook.drawRectangleFast(
									graphicsObject,
									(int)cmd.args[0],
									(int)cmd.args[1],
									(int)cmd.args[2],
									(int)cmd.args[3],
									(int)cmd.args[4],
									(int)cmd.args[5],
									(int)cmd.args[6],
									(int)cmd.args[7],
									(boolean)cmd.args[8]
							);
							break;
						case Arc:
							PrimitiveDrawingHook.drawArcFast(
									graphicsObject,
									(int)cmd.args[0],
									(int)cmd.args[1],
									(int)cmd.args[2],
									(int)cmd.args[3],
									(int)cmd.args[4],
									(int)cmd.args[5],
									(int)cmd.args[6],
									(int)cmd.args[7],
									(int)cmd.args[8],
									(int)cmd.args[9],
									(boolean)cmd.args[10]
							);
							break;
						case Oval:
							PrimitiveDrawingHook.drawOvalFast(
									graphicsObject,
									(int)cmd.args[0],
									(int)cmd.args[1],
									(int)cmd.args[2],
									(int)cmd.args[3],
									(int)cmd.args[4],
									(int)cmd.args[5],
									(int)cmd.args[6],
									(int)cmd.args[7],
									(boolean)cmd.args[8]
							);
							break;
						default:
							log.error("Invalid render type.");
							break;
					}

					commandBuffer.remove(0);
				}
			} catch (Exception e) {
				log.error("Error encountered.", e);
				break;
			}
		}

		switch (AnimatedBackgroundHook.getResourceHook()) {
			case AnimatedBackgroundHook.HOLDER_SLICK:
				// Slick graphics object
				assert receiver instanceof RendererSlick;
				ResourceHolderCustomAssetExtension.setGraphicsSlick((RendererSlick) receiver, (Graphics) graphicsObject);
				break;
			case AnimatedBackgroundHook.HOLDER_SWING:
				// Swing graphics object
				assert receiver instanceof RendererSwing;
				ResourceHolderCustomAssetExtension.setGraphicsSwing((RendererSwing) receiver, (Graphics2D) graphicsObject);
				break;
			case AnimatedBackgroundHook.HOLDER_SDL:
				// SDL graphics object
				assert receiver instanceof RendererSDL;
				ResourceHolderCustomAssetExtension.setGraphicsSDL((RendererSDL) receiver, (SDLSurface) graphicsObject);
				break;
			default:
				log.error("Invalid renderer. Cannot replace graphics object.");
				break;
		}
	}

	/** Storage type for enqueued commands. */
	private static class RenderCommand {
		public RenderType renderType;
		public Object[] args;

		private RenderCommand() {
			// Use other constructor.
		}

		public RenderCommand(RenderType renderType, Object[] args) {
		    this.renderType = renderType;
		    this.args = args;
		}

		public enum RenderType {
			Rectangle, Arc, Oval
		}
	}
}
