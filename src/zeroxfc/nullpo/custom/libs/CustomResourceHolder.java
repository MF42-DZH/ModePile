package zeroxfc.nullpo.custom.libs;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.gui.sdl.NullpoMinoSDL;
import mu.nu.nullpo.gui.sdl.RendererSDL;
import mu.nu.nullpo.gui.sdl.ResourceHolderSDL;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.gui.slick.RendererSlick;
import mu.nu.nullpo.gui.slick.ResourceHolder;
import mu.nu.nullpo.gui.swing.RendererSwing;
import mu.nu.nullpo.gui.swing.ResourceHolderSwing;
import org.apache.log4j.Logger;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Music;
import sdljava.mixer.MixMusic;
import sdljava.mixer.SDLMixer;
import sdljava.video.SDLRect;
import sdljava.video.SDLSurface;
import zeroxfc.nullpo.custom.libs.types.ImageChunk;
import zeroxfc.nullpo.custom.libs.types.RuntimeImage;
import zeroxfc.nullpo.custom.libs.types.RuntimeMusic;

// Create instances of this class during playerInit / methods called when a mode is started.
// Do not create static instances or instances created only once during mode instance initialisation.
public class CustomResourceHolder {
    public enum Runtime {
        SLICK, SWING, SDL, UNKNOWN
    }

    private static final Logger log = Logger.getLogger(CustomResourceHolder.class);
    private static int bgmPrevious = -1;

    private static String mainClassName = "";

    private final HashMap<String, RuntimeImage<?>> loadedImages;
    private final HashMap<String, RuntimeMusic<?>> loadedMusic;

    private final Runtime holderType;

    /**
     * Creates a new custom resource holder with 8 initial capacity.
     * Do <b>NOT</b> create a static instance of this class, as it caches graphics objects.
     */
    public CustomResourceHolder() {
        this(8);
    }

    /**
     * Creates a new custom resource holder.
     * Do <b>NOT</b> create a static instance of this class, as it caches graphics objects.
     *
     * @param initialCapacity Start capacity of the internal hashmaps.
     */
    public CustomResourceHolder(int initialCapacity) {
        holderType = getCurrentNullpominoRuntime();

        loadedImages = new HashMap<>(initialCapacity);
        loadedMusic = new HashMap<>(initialCapacity);
        getBigFont(); // To eliminate stutter from File I/O in Slick and Swing.
    }

    /**
     * Gets the current NullpoMino runtime.<br />
     * Useful for selecting different renderers, sound engines or input handlers.
     *
     * @return Enum that represents the runtime type (NullpoMinoSlick, NullpoMinoSwing or NullpoMinoSDL).
     */
    public static Runtime getCurrentNullpominoRuntime() {
        Runtime resouceHolderType = Runtime.UNKNOWN;

        final String mainClass = CustomResourceHolder.getMainClassName();

        if (mainClass.contains("Slick")) resouceHolderType = Runtime.SLICK;
        else if (mainClass.contains("Swing")) resouceHolderType = Runtime.SWING;
        else if (mainClass.contains("SDL")) resouceHolderType = Runtime.SDL;

        return resouceHolderType;
    }

    /**
     * Gets the current instance's main class name.
     *
     * @return Main class name.
     */
    public static String getMainClassName() {
        if (mainClassName.isEmpty() || mainClassName.equals("Unknown")) {
            // Thread-safe code used for when more threads are being used.
            // Warning: slower.
            Collection<StackTraceElement[]> allStackTraces = Thread.getAllStackTraces().values();
            for (StackTraceElement[] traceElements : allStackTraces) {
                for (StackTraceElement element : traceElements) {
                    String name = element.getClassName();
                    if (name.contains("NullpoMinoSlick") || name.contains("NullpoMinoSwing") || name.contains("NullpoMinoSDL")) {
                        mainClassName = name;
                        break;
                    }
                }

                if (!mainClassName.isEmpty()) break;
            }
            if (mainClassName.isEmpty()) mainClassName = "Unknown";
        }

        return mainClassName;
    }

    /** Perform a value-returning action depending on the current runtime. */
    public static <T> T doForRuntime(Callable<T> ifSlick, Callable<T> ifSwing, Callable<T> ifSDL) {
        try {
            switch (getCurrentNullpominoRuntime()) {
                case SLICK:
                    return ifSlick.call();
                case SWING:
                    return ifSwing.call();
                case SDL:
                    return ifSDL.call();
                default:
                    return null;
            }
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }

    /** Perform an action depending on the current runtime. */
    public static void doForRuntime(Runnable ifSlick, Runnable ifSwing, Runnable ifSDL) {
        try {
            switch (getCurrentNullpominoRuntime()) {
                case SLICK:
                    ifSlick.run();
                    break;
                case SWING:
                    ifSwing.run();
                    break;
                case SDL:
                    ifSDL.run();
                    break;
                default:
                    return;
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * Gets the number of currently loaded block-skins inside the game.
     *
     * @return Number of block skins.
     */
    public static int getNumberLoadedBlockSkins() {
        switch (getCurrentNullpominoRuntime()) {
            case SLICK:
                return ResourceHolder.imgNormalBlockList.size();
            case SWING:
                return ResourceHolderSwing.imgNormalBlockList.size();
            case SDL:
                return ResourceHolderSDL.imgNormalBlockList.size();
            default:
                return 0;
        }
    }

    // There is a race condition between these variables being set and the get from WeakReference being called, but
    // there should be no risk of a (harmful) data race as this code will usually never run past a gamemode's lifetime.
    private static final Mirror.FieldAccessor<RendererSlick, Graphics> graphicsSlickAccessor;
    private static final Mirror.FieldAccessor<RendererSwing, Graphics2D> graphicsSwingAccessor;
    private static final Mirror.FieldAccessor<RendererSDL, SDLSurface> graphicsSDLAccessor;

    static {
        graphicsSlickAccessor = Mirror.getFieldAccessor(RendererSlick.class, "graphics");
        graphicsSwingAccessor = Mirror.getFieldAccessor(RendererSwing.class, "graphics");

        // XXX: This is needed because the other runtimes don't load the SDL libraries, in which
        //      getDeclaredField on the graphics field of the SDL renderer specifically needs SDLException to
        //      be in the runtime classpath (which the other renderers won't load, duh).
        if (getCurrentNullpominoRuntime() == Runtime.SDL) {
            graphicsSDLAccessor = Mirror.getFieldAccessor(RendererSDL.class, "graphics");
        } else {
            graphicsSDLAccessor = null;
        }
    }

    private WeakReference<SDLSurface> graphicsSDL = null;
    private WeakReference<Graphics2D> graphicsSwing = null;
    private WeakReference<Graphics> graphicsSlick = null;

    public SDLSurface getGraphicsSDL(RendererSDL renderer, boolean useCache) {
        if (graphicsSDLAccessor == null) return null;

        if (!useCache || graphicsSDL == null || graphicsSDL.get() == null) {
            if (useCache) graphicsSDL = new WeakReference<>(graphicsSDLAccessor.get(renderer));
            else return graphicsSDLAccessor.get(renderer);
        }

        return graphicsSDL.get();
    }

    public Graphics2D getGraphicsSwing(RendererSwing renderer, boolean useCache) {
        if (!useCache || graphicsSwing == null || graphicsSwing.get() == null) {
            if (useCache) graphicsSwing = new WeakReference<>(graphicsSwingAccessor.get(renderer));
            else return graphicsSwingAccessor.get(renderer);
        }

        return graphicsSwing.get();
    }

    public Graphics getGraphicsSlick(RendererSlick renderer, boolean useCache) {
        if (!useCache || graphicsSlick == null || graphicsSlick.get() == null) {
            if (useCache) graphicsSlick = new WeakReference<>(graphicsSlickAccessor.get(renderer));
            else return graphicsSlickAccessor.get(renderer);
        }

        return graphicsSlick.get();
    }

    /**
     * Adds an image to the custom image library
     *
     * @param filePath Path of image file
     * @param name     Identifier name
     */
    public void loadImage(String filePath, String name) {
        switch (holderType) {
            case SLICK:
                loadedImages.put(name, new RuntimeImage.Slick(ResourceHolder.loadImage(filePath)));
                break;
            case SWING:
                loadedImages.put(name, new RuntimeImage.Swing(ResourceHolderSwing.loadImage(ResourceHolderSwing.getURL(filePath))));
                break;
            case SDL:
                loadedImages.put(name, new RuntimeImage.SDL(ResourceHolderSDL.loadImage(filePath)));
                break;
        }
    }

    /**
     * Copies an image from the HashMap key to another key.
     *
     * @param source Source key
     * @param dest   Destination key
     */
    public void copyImage(String source, String dest) {
        loadedImages.replace(dest, loadedImages.get(source));
    }

    /**
     * Gets the pixel dimensions of the named image.
     *
     * @param name Image name in holder dictionary.
     * @return int[] { width, height } (both in pixels).
     */
    public int[] getImageDimensions(String name) {
        try {
            int[] dim = new int[] { 0, 0 };
            final RuntimeImage<?> image = loadedImages.get(name);

            if (image != null) {
                if (image instanceof RuntimeImage.Slick) {
                    dim[0] = ((RuntimeImage.Slick) image).image.getWidth();
                    dim[1] = ((RuntimeImage.Slick) image).image.getHeight();
                } else if (image instanceof RuntimeImage.Swing) {
                    dim[0] = ((RuntimeImage.Swing) image).image.getWidth(null);
                    dim[1] = ((RuntimeImage.Swing) image).image.getHeight(null);
                } else if (image instanceof RuntimeImage.SDL) {
                    dim[0] = ((RuntimeImage.SDL) image).image.getWidth();
                    dim[1] = ((RuntimeImage.SDL) image).image.getHeight();
                }
            }

            return dim;
        } catch (Exception e) {
            return new int[] { 0, 0 };
        }
    }

    /**
     * Puts image in the holder at name.
     *
     * @param name Image name
     */
    public void putImageAt(RuntimeImage<?> image, String name) {
        if (image == null) return;
        try {
            loadedImages.put(name, image);
        } catch (Exception e) {
            log.error("Unable to insert image " + image + " at " + name);
        }
    }

    /**
     * Gets object that is an image instance.
     * <code>A CAST IS STRICTLY NECESARRY!</code>
     *
     * @param name Image name
     * @return Image at name
     */
    public RuntimeImage<?> getImageAt(String name) {
        try {
            return loadedImages.get(name);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Replaces an image.
     *
     * @param name  Key of image to replace
     * @param image Image object to replace with.
     */
    public void setImageAt(String name, RuntimeImage<?> image) {
        try {
            loadedImages.replace(name, image);
            log.info("Image " + name + " replaced.");
        } catch (Exception e) {
            log.error("Image does not exist or invalid cast attempted.");
        }
    }

    /** Represents a blockskin size. */
    public enum BlockSize {
        SMALL, NORMAL, BIG;

        public static BlockSize fromScale(float scale) {
            if (scale <= 0.5f) return SMALL;
            else if (scale > 1.0f) return BIG;
            else return NORMAL;
        }
    }

    /**
     * Get a block skin for a particular scale of rendering.
     *
     * @param scale Render scale
     * @param skin  Block skin number
     * @return Runtime image of block skin, or null if runtime is undefined.
     */
    public RuntimeImage<?> getBlockSkin(float scale, int skin) {
        return getBlockSkin(BlockSize.fromScale(scale), skin);
    }

    /**
     * Get a block skin for a particular size of block.
     *
     * @param size Block size
     * @param skin Block skin number
     * @return Runtime image of block skin, or null if runtime is undefined.
     */
    public RuntimeImage<?> getBlockSkin(BlockSize size, int skin) {
        switch (holderType) {
            case SLICK:
                if (size == BlockSize.SMALL) return new RuntimeImage.Slick(ResourceHolder.imgSmallBlockList.get(skin));
                else if (size == BlockSize.NORMAL) return new RuntimeImage.Slick(ResourceHolder.imgNormalBlockList.get(skin));
                else return new RuntimeImage.Slick(ResourceHolder.imgBigBlockList.get(skin));
            case SWING:
                if (size == BlockSize.SMALL) return new RuntimeImage.Swing(ResourceHolderSwing.imgSmallBlockList.get(skin));
                else if (size == BlockSize.NORMAL) return new RuntimeImage.Swing(ResourceHolderSwing.imgNormalBlockList.get(skin));
                else return new RuntimeImage.Swing(ResourceHolderSwing.imgBigBlockList.get(skin));
            case SDL:
                if (size == BlockSize.SMALL) return new RuntimeImage.SDL(ResourceHolderSDL.imgSmallBlockList.get(skin));
                else if (size == BlockSize.NORMAL) return new RuntimeImage.SDL(ResourceHolderSDL.imgNormalBlockList.get(skin));
                else return new RuntimeImage.SDL(ResourceHolderSDL.imgBigBlockList.get(skin));
            default:
                return null;
        }
    }

    /**
     * Get if a particular block skin supports connected textures.
     *
     * @param skin Block skin number
     * @return If block skin supports connected blocks.
     */
    public boolean getSkinStickyFlag(int skin) {
        switch (holderType) {
            case SLICK:
                return ResourceHolder.blockStickyFlagList.get(skin);
            case SWING:
                return ResourceHolderSwing.blockStickyFlagList.get(skin);
            case SDL:
                return ResourceHolderSDL.blockStickyFlagList.get(skin);
            default:
                return false;
        }
    }

    /**
     * Sets rotation centre for an image when using Slick renderer.
     *
     * @param name Image name
     * @param x    X-coordinate relative to image's top-left corner
     * @param y    Y-coordinate relative to image's top-left corner
     */
    public void setRotationCentre(String name, float x, float y) {
        final RuntimeImage<?> image = getImageAt(name);

        if (holderType == Runtime.SLICK && image instanceof RuntimeImage.Slick) {
            ((RuntimeImage.Slick) image).image.setCenterOfRotation(x, y);
        }
    }

    /**
     * Sets rotation for an image when using Slick renderer.
     *
     * @param name Image name
     * @param a    Angle, degrees.
     */
    public void setRotation(String name, float a) {
        final RuntimeImage<?> image = getImageAt(name);

        if (holderType == Runtime.SLICK && image instanceof RuntimeImage.Slick) {
            ((RuntimeImage.Slick) image).image.setRotation(a);
        }
    }

    private RuntimeImage<?> smallFont;
    private RuntimeImage<?> normalFont;
    private RuntimeImage<?> bigFont;

    private RuntimeImage<?> getSmallFont() {
        if (smallFont != null) return smallFont;

        smallFont = doForRuntime(
            () -> new RuntimeImage.Slick(ResourceHolder.imgFontSmall),
            () -> new RuntimeImage.Swing(ResourceHolderSwing.imgFontSmall),
            () -> new RuntimeImage.SDL(ResourceHolderSDL.imgFontSmall)
        );

        return smallFont;
    }

    private RuntimeImage<?> getNormalFont() {
        if (normalFont != null) return normalFont;

        normalFont = doForRuntime(
            () -> new RuntimeImage.Slick(ResourceHolder.imgFont),
            () -> new RuntimeImage.Swing(ResourceHolderSwing.imgFont),
            () -> new RuntimeImage.SDL(ResourceHolderSDL.imgFont)
        );

        return normalFont;
    }

    private RuntimeImage<?> getBigFont() {
        if (bigFont != null) return bigFont;

        final String CUSTOM_SKIN_DIRECTORY = "custom.skin.directory";
        String skinDir = null;

        switch (holderType) {
            case SLICK:
                skinDir = mu.nu.nullpo.gui.slick.NullpoMinoSlick.propConfig.getProperty(CUSTOM_SKIN_DIRECTORY, "res");
                break;
            case SWING:
                skinDir = mu.nu.nullpo.gui.swing.NullpoMinoSwing.propConfig.getProperty(CUSTOM_SKIN_DIRECTORY, "res");
                break;
            case SDL:
                skinDir = mu.nu.nullpo.gui.sdl.NullpoMinoSDL.propConfig.getProperty(CUSTOM_SKIN_DIRECTORY, "res");
                break;
            default:
                return null;
        }

        final String usedSkinDir = skinDir;

        bigFont = doForRuntime(
            () -> new RuntimeImage.Slick(ResourceHolder.loadImage(usedSkinDir + "/graphics/font_big.png")),
            () -> new RuntimeImage.Swing(ResourceHolderSwing.loadImage(ResourceHolderSwing.getURL(usedSkinDir + "/graphics/font_big.png"))),
            () -> new RuntimeImage.SDL(ResourceHolderSDL.imgFontBig)
        );

        return bigFont;
    }


    /**
     * Improved implementation of <code>NormalFont.printFont</code>, that uses all three
     * font sizes at appropriate times, and has better support for newlines at different
     * scale factors.
     *
     * @param engine <code>GameEngine</code> to draw with
     * @param x      X-coordinate (Top-Left Corner)
     * @param y      Y-coordinate (Top-Left Corner)
     * @param str    String to draw
     * @param colour Character colour code (from <code>EventReceiver</code>)
     * @param scale  Character scale
     */
    public void drawString(GameEngine engine, int x, int y, String str, int colour, float scale) {
        drawString(engine, x, y, str, colour, 255, 255, 255, 255, scale);
    }

    /**
     * Improved implementation of <code>NormalFont.printFont</code>, that uses all three
     * font sizes at appropriate times, and has better support for newlines at different
     * scale factors.
     * <p>
     * This version also supports post-multiplying the colour of the text.
     *
     * @param engine             <code>GameEngine</code> to draw with
     * @param x                  X-coordinate (Top-Left Corner)
     * @param y                  Y-coordinate (Top-Left Corner)
     * @param str                String to draw
     * @param receiverTextColour Character colour code (from <code>EventReceiver</code>)
     * @param red                Colour multiplication red component
     * @param green              Colour multiplication green component
     * @param blue               Colour multiplication blue component
     * @param alpha              Render alpha
     * @param scale              Character scale
     */
    public void drawString(GameEngine engine, int x, int y, String str, int receiverTextColour, int red, int green, int blue, int alpha, float scale) {
        RuntimeImage<?> font;
        int fontBaseScale;

        // Get font and base scale based on scale factor for best quality.
        // Base unit length is 16px on screen.
        final int baseUnit = 16;

        if (scale <= 0.5f) {
            font = getSmallFont();
            fontBaseScale = baseUnit >>> 1;
        } else if (scale > 1f) {
            font = getBigFont();
            fontBaseScale = baseUnit * 2;
        } else {
            font = getNormalFont();
            fontBaseScale = baseUnit;
        }

        final int strLength = str.length();

        float dx = x;
        float dy = y;

        for (int i = 0; i < strLength; ++i) {
            final int chrAt = str.charAt(i);

            if (chrAt == 0x0A) {
                dx = x;
                dy += baseUnit * scale;
            } else {
                final int sx = ((chrAt - 32) % 32) * fontBaseScale;
                final int sy = ((chrAt - 32) / 32) * fontBaseScale + (receiverTextColour * 3 * fontBaseScale);

                drawImage(
                    engine,
                    "font",
                    font,
                    dx,
                    dy,
                    dx + (baseUnit * scale),
                    dy + (baseUnit * scale),
                    sx,
                    sy,
                    fontBaseScale,
                    fontBaseScale,
                    red,
                    green,
                    blue,
                    alpha,
                    true
                );

                dx += baseUnit * scale;
            }
        }
    }

    /**
     * Improved implementation of <code>NormalFont.printFont</code>, that uses all three
     * font sizes at appropriate times, and has better support for newlines at different
     * scale factors.
     * <p>
     * This version also supports post-multiplying the colour of the text.
     * <p>
     * This version also lets you set clip boundaries for drawing, where text will be
     * partially cut-off if overlapping those boundaries.
     *
     * @param engine             <code>GameEngine</code> to draw with
     * @param x                  X-coordinate (Top-Left Corner)
     * @param y                  Y-coordinate (Top-Left Corner)
     * @param minX               Minimum X-coordinate for drawing
     * @param minY               Minimum Y-coordinate for drawing
     * @param maxX               Maximum X-coordinate for drawing
     * @param maxY               Maximum Y-coordinate for drawing
     * @param str                String to draw
     * @param receiverTextColour Character colour code (from <code>EventReceiver</code>)
     * @param red                Colour multiplication red component
     * @param green              Colour multiplication green component
     * @param blue               Colour multiplication blue component
     * @param alpha              Render alpha
     * @param scale              Character scale
     */
    public void drawClippedString(GameEngine engine, int x, int y, int minX, int minY, int maxX, int maxY, String str, int receiverTextColour, int red, int green, int blue, int alpha, float scale) {
        RuntimeImage<?> font;
        int fontBaseScale;

        // Prevents a div-by-zero.
        if (scale == 0f) return;

        // Get font and base scale based on scale factor for best quality.
        // Base unit length is 16px on screen.
        final int baseUnit = 16;

        if (scale <= 0.5f) {
            font = getSmallFont();
            fontBaseScale = baseUnit >>> 1;
        } else if (scale > 1f) {
            font = getBigFont();
            fontBaseScale = baseUnit * 2;
        } else {
            font = getNormalFont();
            fontBaseScale = baseUnit;
        }

        final int strLength = str.length();

        float dx = x;
        float dy = y;

        for (int i = 0; i < strLength; ++i) {
            final int chrAt = str.charAt(i);

            if (chrAt == 0x0A) {
                dx = x;
                dy += baseUnit * scale;
            } else {
                final int sx = ((chrAt - 32) % 32) * fontBaseScale;
                final int sy = ((chrAt - 32) / 32) * fontBaseScale + (receiverTextColour * 3 * fontBaseScale);

                final float drawTLX = Math.max(dx, minX);
                final float drawTLY = Math.max(dy, minY);
                final float drawBRX = Math.min(dx + (baseUnit * scale), maxX);
                final float drawBRY = Math.min(dy + (baseUnit * scale), maxY);
                final float offsetSrcTLX = (drawTLX - dx) / (scale / (fontBaseScale / (float) baseUnit));
                final float offsetSrcTLY = (drawTLY - dy) / (scale / (fontBaseScale / (float) baseUnit));
                final float offsetSrcBRX = fontBaseScale - ((drawBRX - dx) / (scale / (fontBaseScale / (float) baseUnit)));
                final float offsetSrcBRY = fontBaseScale - ((drawBRY - dy) / (scale / (fontBaseScale / (float) baseUnit)));
                final float srcSizeX = fontBaseScale - offsetSrcTLX - offsetSrcBRX;
                final float srcSizeY = fontBaseScale - offsetSrcTLY - offsetSrcBRY;

                if (srcSizeX >= 0 && srcSizeY >= 0) {
                    drawImage(
                        engine,
                        "font",
                        font,
                        drawTLX,
                        drawTLY,
                        drawBRX,
                        drawBRY,
                        sx + offsetSrcTLX,
                        sy + offsetSrcTLY,
                        srcSizeX,
                        srcSizeY,
                        red,
                        green,
                        blue,
                        alpha,
                        true
                    );
                }

                dx += baseUnit * scale;
            }
        }
    }

    /**
     * Draws an image based on runtime to the screen.
     *
     * @param engine       GameEngine to draw with
     * @param logName      Name of image when in logs
     * @param runtimeImage The instance of the image to draw
     * @param x            X-coordinate (Top-Left Corner)
     * @param y            Y-coordinate (Top-Left Corner)
     * @param x2           X-coordinate (Bottom-Right Corner)
     * @param y2           Y-coordinate (Bottom-Right Corner)
     * @param srcX         Source X-coordinate (Top-Left Corner)
     * @param srcY         Source Y-coordinate (Top-Left Corner)
     * @param srcSizeX     Horizontal size in Source
     * @param srcSizeY     Vertical size in Source
     * @param red          Red filter colour
     * @param green        Green filter colour
     * @param blue         Blue filter colour
     * @param alpha        Image alpha multiplier
     */
    public void drawImage(GameEngine engine, String logName, RuntimeImage<?> runtimeImage, float x, float y, float x2, float y2, float srcX, float srcY, float srcSizeX, float srcSizeY, int red, int green, int blue, int alpha, boolean useCachedRenderer) {
        switch (holderType) {
            case SLICK:
                if (!(runtimeImage instanceof RuntimeImage.Slick)) {
                    log.error("Image '" + logName + "' is not a Slick image!");
                    return;
                }

                org.newdawn.slick.Image toDrawSlick = ((RuntimeImage.Slick) runtimeImage).image;

                org.newdawn.slick.Color filter = new org.newdawn.slick.Color(red, green, blue, alpha);

                toDrawSlick.draw(x, y, x2, y2, srcX, srcY, srcX + srcSizeX, srcY + srcSizeY, filter);
                break;
            case SWING:
                if (!(runtimeImage instanceof RuntimeImage.Swing)) {
                    log.error("Image '" + logName + "' is not a Swing image!");
                    return;
                }

                final Graphics2D localSwingGraphics = getGraphicsSwing((RendererSwing) engine.owner.receiver, useCachedRenderer);
                if (localSwingGraphics == null) {
                    log.error("Swing graphics is null!");
                    return;
                }

                java.awt.Image toDrawSwing = ((RuntimeImage.Swing) runtimeImage).image;

                localSwingGraphics.setColor(new java.awt.Color(red, green, blue, alpha));
                localSwingGraphics.drawImage(toDrawSwing, (int) x, (int) y, (int) x2, (int) y2, (int) srcX, (int) srcY, (int) (srcX + srcSizeX), (int) (srcY + srcSizeY), null);
                localSwingGraphics.setColor(new java.awt.Color(255, 255, 255, 255));
                break;
            case SDL:
                if (!(runtimeImage instanceof RuntimeImage.SDL)) {
                    log.error("Image '" + logName + "' is not a SDL image!");
                    return;
                }

                final SDLSurface localSDLGraphics = getGraphicsSDL((RendererSDL) engine.owner.receiver, useCachedRenderer);
                final sdljava.video.SDLSurface toDrawSDL = ((RuntimeImage.SDL) runtimeImage).image;

                int dx = (int) (x2 - x);
                int dy = (int) (y2 - y);
                try {
                    toDrawSDL.blitSurface(new SDLRect((int) srcX, (int) srcY, (int) srcSizeX, (int) srcSizeY), localSDLGraphics, new SDLRect((int) x, (int) y, dx, dy));
                } catch (Exception e) {
                    // DO NOTHING AT ALL.
                }
                break;
        }
    }

    /**
     * Draws image to game.
     *
     * @param engine   GameEngine to draw with
     * @param name     Identifier of image
     * @param x        X position of top-left
     * @param y        Y position of top-left
     * @param x2       X position of bottom-right
     * @param y2       Y position of bottom-right
     * @param srcX     Source X position
     * @param srcY     Source Y position
     * @param srcSizeX Source X size
     * @param srcSizeY Source Y size
     * @param red      Red component
     * @param green    Green component
     * @param blue     Blue component
     * @param alpha    Alpha component
     */
    public void drawImage(GameEngine engine, String name, int x, int y, int x2, int y2, int srcX, int srcY, int srcSizeX, int srcSizeY, int red, int green, int blue, int alpha) {
        final RuntimeImage<?> runtimeImage = getImageAt(name);

        drawImage(
            engine,
            name,
            runtimeImage,
            x,
            y,
            x2,
            y2,
            srcX,
            srcY,
            srcSizeX,
            srcSizeY,
            red,
            green,
            blue,
            alpha,
            true
        );
    }

    /**
     * Draws image to game.
     *
     * @param engine   GameEngine to draw with
     * @param name     Identifier of image
     * @param x        X position
     * @param y        Y position
     * @param srcX     Source X position
     * @param srcY     Source Y position
     * @param srcSizeX Source X size
     * @param srcSizeY Source Y size
     * @param red      Red component
     * @param green    Green component
     * @param blue     Blue component
     * @param alpha    Alpha component
     * @param scale    Image scale
     */
    public void drawImage(GameEngine engine, String name, int x, int y, int srcX, int srcY, int srcSizeX, int srcSizeY, int red, int green, int blue, int alpha, float scale) {
        drawImage(
            engine,
            name,
            x,
            y,
            x + (int) (srcSizeX * scale),
            y + (int) (srcSizeY * scale),
            srcX,
            srcY,
            srcSizeX,
            srcSizeY,
            red,
            green,
            blue,
            alpha
        );
    }

    /**
     * Draws image to game.
     *
     * @param engine   GameEngine to draw with
     * @param name     Identifier of image
     * @param x        X position
     * @param y        Y position
     * @param sx       X size
     * @param sy       Y size
     * @param srcX     Source X position
     * @param srcY     Source Y position
     * @param srcSizeX Source X size
     * @param srcSizeY Source Y size
     * @param red      Red component
     * @param green    Green component
     * @param blue     Blue component
     * @param alpha    Alpha component
     */
    public void drawOffsetImage(GameEngine engine, String name, int x, int y, int sx, int sy, int srcX, int srcY, int srcSizeX, int srcSizeY, int red, int green, int blue, int alpha) {
        drawImage(
            engine,
            name,
            x,
            y,
            x + sx,
            y + sy,
            srcX,
            srcY,
            srcSizeX,
            srcSizeY,
            red,
            green,
            blue,
            alpha
        );
    }


    /**
     * Draws image to game via the <code>ImageChunk</code> coordinate handler.
     *
     * @param engine GameEngine to draw with
     * @param name   Identifier of image
     * @param chunk  Chunk of image to draw
     * @param red    Red component
     * @param green  Green component
     * @param blue   Blue component
     * @param alpha  Alpha componena
     */
    public void drawOffsetImage(GameEngine engine, String name, ImageChunk chunk, int red, int green, int blue, int alpha) {
        final int[] dpos = chunk.getDrawLocation();
        final int[] ddim = chunk.getDrawDimensions();
        final int[] sloc = chunk.getSourceLocation();
        final int[] sdim = chunk.getSourceDimensions();

        drawOffsetImage(engine, name, dpos[0], dpos[1], ddim[0], ddim[1], sloc[0], sloc[1], sdim[0], sdim[1], red, green, blue, alpha);
    }

    /**
     * Draws whole image to game with no tint.
     *
     * @param engine GameEngine to draw with.
     * @param name   Identifier of image.
     * @param x      X position
     * @param y      Y position
     */
    public void drawImage(GameEngine engine, String name, int x, int y) {
        final int[] dims = getImageDimensions(name);
        drawImage(engine, name, x, y, 0, 0, dims[0], dims[1], 255, 255, 255, 255, 1f);
    }

    /**
     * Turns a 8 character RRGGBBAA hex code or 6 character RRGGBB hex code into a colour array.
     *
     * @param color Hex. code string of colour.
     * @return Array representing RGBA or <code>null</code> if string is invalid.
     */
    public static int[] parseColor(String color) {
        String lc;
        lc = color.replace("#", "");
        lc = lc.toLowerCase();

        String red, green, blue, alpha = "FF";

        if (lc.length() != 8 && lc.length() != 6) {
            return null;
        }

        red = lc.substring(0, 2);
        green = lc.substring(2, 4);
        blue = lc.substring(4, 6);
        if (lc.length() == 8) alpha = lc.substring(6, 8);

        try {
            int r, g, b, a;
            r = Integer.parseInt(red, 16);
            g = Integer.parseInt(green, 16);
            b = Integer.parseInt(blue, 16);
            a = lc.length() == 8 ? Integer.parseInt(alpha, 16) : 255;

            return new int[] { r, g, b, a };
        } catch (NumberFormatException e) {
            log.error("Failed to parse '" + color + "' as a valid colour!");
            log.error(e);

            return null;
        }
    }

    // Generic load music worker function.
    private static <M> void loadMusicWorker(Supplier<Boolean> bgmGetter, BiConsumer<Integer, Boolean> noLoopSetter, Consumer<M[]> bgmArraySetter, Class<M> musicClass, Function<String, M> musicLoader, String fileName, boolean noLoop, boolean showErr) {
        if (!bgmGetter.get()) return;

        int no = ResourceHolder.bgm.length + 1;
        final M[] newArr = (M[]) java.lang.reflect.Array.newInstance(musicClass, no);
        System.arraycopy(ResourceHolder.bgm, 0, newArr, 0, ResourceHolder.bgm.length);

        if (newArr[no - 1] == null) {
            if (showErr) log.info("Loading BGM at " + fileName);

            try {
                if ((fileName == null) || (fileName.length() < 1)) {
                    if (showErr) log.info("BGM at " + fileName + " not available");
                    return;
                }

                noLoopSetter.accept(no, noLoop);
                newArr[no - 1] = musicLoader.apply(fileName);
                bgmArraySetter.accept(newArr);

                if (!showErr) log.info("Loaded BGM at " + fileName);
            } catch (Exception e) {
                if (showErr)
                    log.error("BGM at " + fileName + " load failed", e);
                else
                    log.warn("BGM at " + fileName + " load failed");
            }
        }
    }

    /**
     * Appends a new BGM number into the BGM array. On Swing, this has no effect.
     *
     * @param fileName File path of music file to import.
     * @param showErr  Show in log?
     */
    public void loadNewBGMAppend(String fileName, boolean noLoop, boolean showErr) {
        if (holderType == Runtime.SWING) {
            log.warn("BGM is not supported on Swing.");
        } else if (holderType == Runtime.SLICK) {
            loadMusicWorker(
                () -> NullpoMinoSlick.propConfig.getProperty("option.bgm", false),
                (no, nl) -> NullpoMinoSlick.propConfig.setProperty("music.noloop." + no, nl),
                arr -> ResourceHolder.bgm = arr.clone(),
                Music.class,
                fn -> {
                    try {
                        return new Music(fn, true);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                },
                fileName,
                noLoop,
                showErr
            );
        } else if (holderType == Runtime.SDL) {
            loadMusicWorker(
                () -> NullpoMinoSDL.propConfig.getProperty("option.bgm", false),
                (no, nl) -> NullpoMinoSDL.propConfig.setProperty("music.noloop." + no, nl),
                arr -> ResourceHolderSDL.bgm = arr.clone(),
                MixMusic.class,
                fn -> {
                    try {
                        return SDLMixer.loadMUS(fn);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                },
                fileName,
                noLoop,
                showErr
            );
        }
    }

    /**
     * Removes a loaded BGM file from the end of the BGM array. On Swing, this has no effect.
     *
     * @param showerr Show in log?
     */
    public void removeBGMFromEnd(boolean showerr) {
        switch (holderType) {
            case SLICK:
                if (!NullpoMinoSlick.propConfig.getProperty("option.bgm", false)) return;

                int no = ResourceHolder.bgm.length - 1;
                Music[] newBGM = new Music[no];

                System.arraycopy(ResourceHolder.bgm, 0, newBGM, 0, newBGM.length);

                ResourceHolder.bgm = newBGM.clone();
                if (showerr) log.info("Removed BGM at " + no);
                return;
            case SWING:
                log.warn("BGM is not supported on Swing.");
                return;
            case SDL:
                if (!NullpoMinoSDL.propConfig.getProperty("option.bgm", false)) return;

                int no2 = ResourceHolderSDL.bgm.length - 1;
                MixMusic[] newBGM2 = new MixMusic[no2];

                System.arraycopy(ResourceHolderSDL.bgm, 0, newBGM2, 0, newBGM2.length);

                ResourceHolderSDL.bgm = newBGM2.clone();
                if (showerr) log.info("Removed BGM at " + no2);
        }
    }

    /**
     * Adds a named custom BGM.
     * There is a limitation that the song can't be paused.
     *
     * @param name     Track name.
     * @param fileName File path of track.
     */
    public void addCustomBGM(String name, String fileName) {
        try {
            if (holderType == Runtime.SLICK) {
                loadedMusic.put(name, new RuntimeMusic.Slick(new Music(fileName, true)));
            } else if (holderType == Runtime.SDL) {
                loadedMusic.put(name, new RuntimeMusic.SDL(SDLMixer.loadMUS(fileName)));
            } else {
                log.warn("BGM is not supported on Swing.");
            }
        } catch (Exception e) {
            log.error("Unable to load file '" + fileName + "' as BGM.");
        }
    }

    /**
     * Starts the play of a discrete custom BGM. Uses the key name of an added custom BGM.
     * There is a limitation that the song can't be paused.
     *
     * @param name   Track name.
     * @param noLoop Set true if it shouldn't loop.
     */
    public void playCustomBGM(String name, boolean noLoop) {
        final RuntimeMusic<?> mus =  loadedMusic.get(name);

        if (mus != null) {
            switch (holderType) {
                case SLICK:
                    if (!NullpoMinoSlick.propConfig.getProperty("option.bgm", false)) return;

                    stopCustomBGM();

                    int bgmvolume = NullpoMinoSlick.propConfig.getProperty("option.bgmvolume", 128);
                    NullpoMinoSlick.appGameContainer.setMusicVolume(bgmvolume / (float) 128);

                    try {
                        if (mus instanceof RuntimeMusic.Slick) {
                            if (noLoop) ((RuntimeMusic.Slick) mus).music.play();
                            else ((RuntimeMusic.Slick) mus).music.loop();
                        }
                    } catch (Exception e) {
                        log.error("Failed to play music " + name, e);
                    }

                    return;
                case SWING:
                    log.warn("BGM is not supported on Swing.");
                    return;
                case SDL:
                    if (!NullpoMinoSDL.propConfig.getProperty("option.bgm", false)) return;

                    stopCustomBGM();

                    try {
                        if (mus instanceof RuntimeMusic.SDL) {
                            if (noLoop) SDLMixer.playMusic(((RuntimeMusic.SDL) mus).music, 1);
                            else SDLMixer.playMusic(((RuntimeMusic.SDL) mus).music, -1);

                            SDLMixer.volumeMusic(NullpoMinoSDL.propConfig.getProperty("option.bgmvolume", 128));
                        }
                    } catch (Exception e) {
                        log.warn("BGM " + name + " start failed", e);
                    }
            }
        } else {
            log.warn("Music named '" + name + "' does not exist.");
        }
    }

    /**
     * Remove internal custom BGM by name
     *
     * @param name BGM name
     */
    public void removeCustomInternalBGM(String name) {
        loadedMusic.remove(name);
    }

    /**
     * Gets number of loaded, appended BGM files.
     *
     * @return Number of loaded, appended BGM files.
     */
    public int getAmountManagedLoadedBGM() {
        switch (holderType) {
            case SLICK:
                return ResourceHolder.bgm.length - BGMStatus.BGM_COUNT;
            case SDL:
                return ResourceHolderSDL.bgm.length - BGMStatus.BGM_COUNT;
            default:
                return 0;
        }
    }

    /**
     * Gets number of loaded BGM files in holder.
     *
     * @return Number of loaded BGM files in holder.
     */
    public int getAmountDiscreteLoadedBGM() {
        return loadedMusic.size();
    }

    /**
     * Stops all custom BGM.
     */
    public void stopCustomBGM() {
        if (holderType == Runtime.SLICK) {
            for (RuntimeMusic<?> mus : loadedMusic.values()) {
                if (mus instanceof RuntimeMusic.Slick) {
                    ((RuntimeMusic.Slick) mus).music.pause();
                    ((RuntimeMusic.Slick) mus).music.stop();
                }
            }
        } else if (holderType == Runtime.SDL) {
            try {
                SDLMixer.haltMusic();
            } catch (Exception e) {
                log.debug("BGM stop failed", e);
            }
        }
    }

    /**
     * Stops game-managed BGM.
     *
     * @param owner Current GameManager
     */
    public void stopDefaultBGM(GameManager owner) {
        if (owner.bgmStatus.bgm != -1) bgmPrevious = owner.bgmStatus.bgm;
        owner.bgmStatus.bgm = -1;
    }

    /**
     * Restarts previously playing default BGM.
     *
     * @param owner Current GameManager
     */
    public void restartDefaultBGM(GameManager owner) {
        if (owner.bgmStatus.bgm == -1) {
            owner.bgmStatus.bgm = bgmPrevious;
            owner.bgmStatus.fadesw = false;
        }
    }
}
