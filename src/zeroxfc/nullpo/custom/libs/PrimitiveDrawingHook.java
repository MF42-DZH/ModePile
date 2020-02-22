package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.gui.sdl.RendererSDL;
import mu.nu.nullpo.gui.slick.RendererSlick;
import mu.nu.nullpo.gui.swing.RendererSwing;
import org.apache.log4j.Logger;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import sdljava.video.SDLRect;
import sdljava.video.SDLSurface;
import zeroxfc.nullpo.custom.libs.backgroundtypes.AnimatedBackgroundHook;

import java.awt.*;

public class PrimitiveDrawingHook {
	// Log object for debugging
	private static final Logger log = Logger.getLogger(PrimitiveDrawingHook.class);

	private final int RENDERER_HOOK;   // Current renderer ID
	private Graphics graphicsSlick;    // Slick graphics object
	private Graphics2D graphicsSwing;  // Swing graphics object
	private SDLSurface graphicsSDL;    // SDL graphics object

	/**
	 * Constructor method. Need this to hook into the renderer.
	 */
	public PrimitiveDrawingHook(EventReceiver receiver) {
		RENDERER_HOOK = AnimatedBackgroundHook.getResourceHook();

		switch (RENDERER_HOOK) {
			case AnimatedBackgroundHook.HOLDER_SLICK:
				graphicsSlick = ResourceHolderCustomAssetExtension.getGraphicsSlick((RendererSlick) receiver);
				break;
			case AnimatedBackgroundHook.HOLDER_SWING:
				graphicsSwing = ResourceHolderCustomAssetExtension.getGraphicsSwing((RendererSwing) receiver);
				break;
			case AnimatedBackgroundHook.HOLDER_SDL:
				graphicsSDL = ResourceHolderCustomAssetExtension.getGraphicsSDL((RendererSDL) receiver);
				break;
			default:
				log.error("Invalid renderer. Cannot initialise drawing object.");
				break;
		}
	}

	/**
	 * Draws a rectangle using two sets of coordinates.
	 * @param x X-coordinate of the top-left corner
	 * @param y Y-coordinate of the top-left corner
	 * @param sizeX X-size of the rectangle
	 * @param sizeY Y-size of the rectangle
	 * @param red Red component of colour
	 * @param green Green component of colour
	 * @param blue Blue component of colour
	 * @param alpha Alpha component of colour
	 * @param fill Fill rectangle?
	 */
	public void drawRectangle(int x, int y, int sizeX, int sizeY, byte red, byte green, byte blue, byte alpha, boolean fill) {
		switch (RENDERER_HOOK) {
			case AnimatedBackgroundHook.HOLDER_SLICK:
				graphicsSlick.setColor(new Color(red, green, blue, alpha));
				if (fill) graphicsSlick.fillRect(x, y, sizeX, sizeY);
				else graphicsSlick.drawRect(x, y, sizeX, sizeY);
				graphicsSlick.setColor(Color.white);
				break;
			case AnimatedBackgroundHook.HOLDER_SWING:
				graphicsSwing.setColor(new java.awt.Color(red, green, blue, alpha));
				if (fill) graphicsSwing.fillRect(x, y, sizeX, sizeY);
				else graphicsSwing.drawRect(x, y, sizeX, sizeY);
				graphicsSwing.setColor(java.awt.Color.white);
				break;
			case AnimatedBackgroundHook.HOLDER_SDL:
				SDLRect rectDst = new SDLRect(x, y, sizeX, sizeY);
				long total = ((long) red << 24L) + ((long) green << 16L) + ((long) blue << 8L) + (long) alpha;

				try {
					graphicsSDL.fillRect(rectDst,total);
				} catch (Exception e) {
					log.error("SDL Exception. Cannot draw rectangle.", e);
				}
				break;
			default:
				log.error("Invalid renderer. Cannot draw rectangle.");
				break;
		}
	}
}
