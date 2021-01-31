package zeroxfc.nullpo.custom.libs;

import java.awt.*;
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

public class PrimitiveDrawingHook {
    // Log object for debugging
    private static final Logger log = Logger.getLogger(PrimitiveDrawingHook.class);

    /**
     * Draws a rectangle using a set of coordinates and its dimensions.
     *
     * @param receiver Renderer to use
     * @param x        X-coordinate of the top-left corner
     * @param y        Y-coordinate of the top-left corner
     * @param sizeX    X-size of the rectangle
     * @param sizeY    Y-size of the rectangle
     * @param red      Red component of colour
     * @param green    Green component of colour
     * @param blue     Blue component of colour
     * @param alpha    Alpha component of colour
     * @param fill     Fill rectangle?
     */
    public static void drawRectangle(EventReceiver receiver, int x, int y, int sizeX, int sizeY, int red, int green, int blue, int alpha, boolean fill) {
        switch (AnimatedBackgroundHook.getResourceHook()) {
            case AnimatedBackgroundHook.HOLDER_SLICK:
                // Slick graphics object
                Graphics graphicsSlick = ResourceHolderCustomAssetExtension.getGraphicsSlick((RendererSlick) receiver);
                if (graphicsSlick == null) return;

                graphicsSlick.setColor(new Color(red, green, blue, alpha));
                if (fill) graphicsSlick.fillRect(x, y, sizeX, sizeY);
                else graphicsSlick.drawRect(x, y, sizeX, sizeY);
                graphicsSlick.setColor(Color.white);

                ResourceHolderCustomAssetExtension.setGraphicsSlick((RendererSlick) receiver, graphicsSlick);
                break;
            case AnimatedBackgroundHook.HOLDER_SWING:
                // Swing graphics object
                Graphics2D graphicsSwing = ResourceHolderCustomAssetExtension.getGraphicsSwing((RendererSwing) receiver);
                if (graphicsSwing == null) return;

                graphicsSwing.setColor(new java.awt.Color(red, green, blue, alpha));
                if (fill) graphicsSwing.fillRect(x, y, sizeX, sizeY);
                else graphicsSwing.drawRect(x, y, sizeX, sizeY);
                graphicsSwing.setColor(java.awt.Color.white);

                ResourceHolderCustomAssetExtension.setGraphicsSwing((RendererSwing) receiver, graphicsSwing);
                break;
            case AnimatedBackgroundHook.HOLDER_SDL:
                // SDL graphics object
                SDLSurface graphicsSDL = ResourceHolderCustomAssetExtension.getGraphicsSDL((RendererSDL) receiver);
                if (graphicsSDL == null) return;

                SDLRect rectDst = new SDLRect(x, y, sizeX, sizeY);
                long total = ((long) red << 24L) + ((long) green << 16L) + ((long) blue << 8L) + (long) alpha;

                try {
                    graphicsSDL.fillRect(rectDst, total);
                    ResourceHolderCustomAssetExtension.setGraphicsSDL((RendererSDL) receiver, graphicsSDL);
                } catch (Exception e) {
                    log.error("SDL Exception. Cannot draw rectangle.", e);
                }
                break;
            default:
                log.error("Invalid renderer. Cannot draw rectangle.");
                break;
        }
    }

    /**
     * Draws an arc using a set of coordinates and its dimensions.<br />
     * <strong>Warning: SDL cannot use this feature.</strong>
     *
     * @param receiver   Renderer to use
     * @param x          X-coordinate of the centre
     * @param y          Y-coordinate of the centre
     * @param sizeX      X-size of the arc
     * @param sizeY      Y-size of the arc
     * @param angleStart Start angle of arc in circle (0 degrees = top)
     * @param angleSize  Total angular size of arc (360 degrees = full turn)
     * @param red        Red component of colour
     * @param green      Green component of colour
     * @param blue       Blue component of colour
     * @param alpha      Alpha component of colour
     * @param fill       Fill arc?
     */
    public static void drawArc(EventReceiver receiver, int x, int y, int sizeX, int sizeY, int angleStart, int angleSize, int red, int green, int blue, int alpha, boolean fill) {
        switch (AnimatedBackgroundHook.getResourceHook()) {
            case AnimatedBackgroundHook.HOLDER_SLICK:
                // Slick graphics object
                Graphics graphicsSlick = ResourceHolderCustomAssetExtension.getGraphicsSlick((RendererSlick) receiver);
                if (graphicsSlick == null) return;

                graphicsSlick.setColor(new Color(red, green, blue, alpha));
                if (fill) graphicsSlick.fillArc(x, y, sizeX, sizeY, angleStart, angleSize);
                else graphicsSlick.drawArc(x, y, sizeX, sizeY, angleStart, angleSize);
                graphicsSlick.setColor(Color.white);

                ResourceHolderCustomAssetExtension.setGraphicsSlick((RendererSlick) receiver, graphicsSlick);
                break;
            case AnimatedBackgroundHook.HOLDER_SWING:
                // Swing graphics object
                Graphics2D graphicsSwing = ResourceHolderCustomAssetExtension.getGraphicsSwing((RendererSwing) receiver);
                if (graphicsSwing == null) return;

                graphicsSwing.setColor(new java.awt.Color(red, green, blue, alpha));
                if (fill) graphicsSwing.fillArc(x, y, sizeX, sizeY, angleStart, angleSize);
                else graphicsSwing.drawArc(x, y, sizeX, sizeY, angleStart, angleSize);
                graphicsSwing.setColor(java.awt.Color.white);

                ResourceHolderCustomAssetExtension.setGraphicsSwing((RendererSwing) receiver, graphicsSwing);
                break;
            default:
                log.error("Invalid renderer. Cannot draw arc.");
                break;
        }
    }

    /**
     * Draws an oval using a set of coordinates and its dimensions.<br />
     * <strong>Warning: SDL cannot use this feature.</strong>
     *
     * @param receiver Renderer to use
     * @param x        X-coordinate of the top-left corner
     * @param y        Y-coordinate of the top-left corner
     * @param sizeX    X-size of the oval
     * @param sizeY    Y-size of the oval
     * @param red      Red component of colour
     * @param green    Green component of colour
     * @param blue     Blue component of colour
     * @param alpha    Alpha component of colour
     * @param fill     Fill oval?
     */
    public static void drawOval(EventReceiver receiver, int x, int y, int sizeX, int sizeY, int red, int green, int blue, int alpha, boolean fill) {
        switch (AnimatedBackgroundHook.getResourceHook()) {
            case AnimatedBackgroundHook.HOLDER_SLICK:
                // Slick graphics object
                Graphics graphicsSlick = ResourceHolderCustomAssetExtension.getGraphicsSlick((RendererSlick) receiver);
                if (graphicsSlick == null) return;

                graphicsSlick.setColor(new Color(red, green, blue, alpha));
                if (fill) graphicsSlick.fillOval(x, y, sizeX, sizeY);
                else graphicsSlick.drawOval(x, y, sizeX, sizeY);
                graphicsSlick.setColor(Color.white);

                ResourceHolderCustomAssetExtension.setGraphicsSlick((RendererSlick) receiver, graphicsSlick);
                break;
            case AnimatedBackgroundHook.HOLDER_SWING:
                // Swing graphics object
                Graphics2D graphicsSwing = ResourceHolderCustomAssetExtension.getGraphicsSwing((RendererSwing) receiver);
                if (graphicsSwing == null) return;

                graphicsSwing.setColor(new java.awt.Color(red, green, blue, alpha));
                if (fill) graphicsSwing.fillOval(x, y, sizeX, sizeY);
                else graphicsSwing.drawOval(x, y, sizeX, sizeY);
                graphicsSwing.setColor(java.awt.Color.white);

                ResourceHolderCustomAssetExtension.setGraphicsSwing((RendererSwing) receiver, graphicsSwing);
                break;
            default:
                log.error("Invalid renderer. Cannot draw rectangle.");
                break;
        }
    }

    /**
     * Draws a rectangle using a set of coordinates and its dimensions. No reflection.
     *
     * @param graphicsObject Graphics object to draw on
     * @param x              X-coordinate of the top-left corner
     * @param y              Y-coordinate of the top-left corner
     * @param sizeX          X-size of the rectangle
     * @param sizeY          Y-size of the rectangle
     * @param red            Red component of colour
     * @param green          Green component of colour
     * @param blue           Blue component of colour
     * @param alpha          Alpha component of colour
     * @param fill           Fill rectangle?
     */
    public static void drawRectangleFast(Object graphicsObject, int x, int y, int sizeX, int sizeY, int red, int green, int blue, int alpha, boolean fill) {
        if (!(graphicsObject instanceof Graphics) &&
            !(graphicsObject instanceof Graphics2D) &&
            !(graphicsObject instanceof SDLSurface)) return;

        switch (AnimatedBackgroundHook.getResourceHook()) {
            case AnimatedBackgroundHook.HOLDER_SLICK:
                // Slick graphics object
                assert graphicsObject instanceof Graphics;
                Graphics graphicsSlick = (Graphics) graphicsObject;

                graphicsSlick.setColor(new Color(red, green, blue, alpha));
                if (fill) graphicsSlick.fillRect(x, y, sizeX, sizeY);
                else graphicsSlick.drawRect(x, y, sizeX, sizeY);
                graphicsSlick.setColor(Color.white);
                break;
            case AnimatedBackgroundHook.HOLDER_SWING:
                // Swing graphics object
                assert graphicsObject instanceof Graphics2D;
                Graphics2D graphicsSwing = (Graphics2D) graphicsObject;

                graphicsSwing.setColor(new java.awt.Color(red, green, blue, alpha));
                if (fill) graphicsSwing.fillRect(x, y, sizeX, sizeY);
                else graphicsSwing.drawRect(x, y, sizeX, sizeY);
                graphicsSwing.setColor(java.awt.Color.white);
                break;
            case AnimatedBackgroundHook.HOLDER_SDL:
                // SDL graphics object
                assert graphicsObject instanceof SDLSurface;
                SDLSurface graphicsSDL = (SDLSurface) graphicsObject;

                SDLRect rectDst = new SDLRect(x, y, sizeX, sizeY);
                long total = ((long) red << 24L) + ((long) green << 16L) + ((long) blue << 8L) + (long) alpha;

                try {
                    graphicsSDL.fillRect(rectDst, total);
                } catch (Exception e) {
                    log.error("SDL Exception. Cannot draw rectangle.", e);
                }
                break;
            default:
                log.error("Invalid renderer. Cannot draw rectangle.");
                break;
        }
    }

    /**
     * Draws an arc using a set of coordinates and its dimensions. No reflection.<br />
     * <strong>Warning: SDL cannot use this feature.</strong>
     *
     * @param graphicsObject Graphics object to draw on
     * @param x              X-coordinate of the centre
     * @param y              Y-coordinate of the centre
     * @param sizeX          X-size of the arc
     * @param sizeY          Y-size of the arc
     * @param angleStart     Start angle of arc in circle (0 degrees = top)
     * @param angleSize      Total angular size of arc (360 degrees = full turn)
     * @param red            Red component of colour
     * @param green          Green component of colour
     * @param blue           Blue component of colour
     * @param alpha          Alpha component of colour
     * @param fill           Fill arc?
     */
    public static void drawArcFast(Object graphicsObject, int x, int y, int sizeX, int sizeY, int angleStart, int angleSize, int red, int green, int blue, int alpha, boolean fill) {
        if (!(graphicsObject instanceof Graphics) &&
            !(graphicsObject instanceof Graphics2D) &&
            !(graphicsObject instanceof SDLSurface)) return;

        switch (AnimatedBackgroundHook.getResourceHook()) {
            case AnimatedBackgroundHook.HOLDER_SLICK:
                // Slick graphics object
                assert graphicsObject instanceof Graphics;
                Graphics graphicsSlick = (Graphics) graphicsObject;

                graphicsSlick.setColor(new Color(red, green, blue, alpha));
                if (fill) graphicsSlick.fillArc(x, y, sizeX, sizeY, angleStart, angleSize);
                else graphicsSlick.drawArc(x, y, sizeX, sizeY, angleStart, angleSize);
                graphicsSlick.setColor(Color.white);
                break;
            case AnimatedBackgroundHook.HOLDER_SWING:
                // Swing graphics object
                assert graphicsObject instanceof Graphics2D;
                Graphics2D graphicsSwing = (Graphics2D) graphicsObject;

                graphicsSwing.setColor(new java.awt.Color(red, green, blue, alpha));
                if (fill) graphicsSwing.fillArc(x, y, sizeX, sizeY, angleStart, angleSize);
                else graphicsSwing.drawArc(x, y, sizeX, sizeY, angleStart, angleSize);
                graphicsSwing.setColor(java.awt.Color.white);
                break;
            default:
                log.error("Invalid renderer. Cannot draw arc.");
                break;
        }
    }

    /**
     * Draws an oval using a set of coordinates and its dimensions. No reflection.<br />
     * <strong>Warning: SDL cannot use this feature.</strong>
     *
     * @param graphicsObject Graphics object to draw on
     * @param x              X-coordinate of the top-left corner
     * @param y              Y-coordinate of the top-left corner
     * @param sizeX          X-size of the oval
     * @param sizeY          Y-size of the oval
     * @param red            Red component of colour
     * @param green          Green component of colour
     * @param blue           Blue component of colour
     * @param alpha          Alpha component of colour
     * @param fill           Fill oval?
     */
    public static void drawOvalFast(Object graphicsObject, int x, int y, int sizeX, int sizeY, int red, int green, int blue, int alpha, boolean fill) {
        if (!(graphicsObject instanceof Graphics) &&
            !(graphicsObject instanceof Graphics2D) &&
            !(graphicsObject instanceof SDLSurface)) return;

        switch (AnimatedBackgroundHook.getResourceHook()) {
            case AnimatedBackgroundHook.HOLDER_SLICK:
                // Slick graphics object
                assert graphicsObject instanceof Graphics;
                Graphics graphicsSlick = (Graphics) graphicsObject;

                graphicsSlick.setColor(new Color(red, green, blue, alpha));
                if (fill) graphicsSlick.fillOval(x, y, sizeX, sizeY);
                else graphicsSlick.drawOval(x, y, sizeX, sizeY);
                graphicsSlick.setColor(Color.white);
                break;
            case AnimatedBackgroundHook.HOLDER_SWING:
                // Swing graphics object
                assert graphicsObject instanceof Graphics2D;
                Graphics2D graphicsSwing = (Graphics2D) graphicsObject;

                graphicsSwing.setColor(new java.awt.Color(red, green, blue, alpha));
                if (fill) graphicsSwing.fillOval(x, y, sizeX, sizeY);
                else graphicsSwing.drawOval(x, y, sizeX, sizeY);
                graphicsSwing.setColor(java.awt.Color.white);
                break;
            default:
                log.error("Invalid renderer. Cannot draw rectangle.");
                break;
        }
    }
}
