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

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
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
import zeroxfc.nullpo.custom.libs.backgroundtypes.AnimatedBackgroundHook;
import zeroxfc.nullpo.custom.libs.types.RuntimeImage;
import zeroxfc.nullpo.custom.libs.types.RuntimeMusic;

public class CustomResourceHolder {
    /**
     * Internal used ResourceHolder class.
     */
    private static final int HOLDER_SLICK = 0,
        HOLDER_SWING = 1,
        HOLDER_SDL = 2;
    private static final Logger log = Logger.getLogger(CustomResourceHolder.class);
    private static int bgmPrevious = -1;

    private static String mainClassName = "";

    private final HashMap<String, RuntimeImage<?>> loadedImages;
    private final HashMap<String, RuntimeMusic<?>> loadedMusic;

    private Graphics2D localSwingGraphics;
    private sdljava.video.SDLSurface localSDLGraphics;

    private int holderType;

    /**
     * Creates a new custom resource holder with 8 initial capacity.
     */
    public CustomResourceHolder() {
        this(8);
    }

    /**
     * Creates a new custom resource holder.
     *
     * @param initialCapacity Start capacity of the internal hashmaps.
     */
    public CustomResourceHolder(int initialCapacity) {
        String mainClass = getMainClassName();

        holderType = -1;
        if (mainClass.contains("Slick")) holderType = HOLDER_SLICK;
        else if (mainClass.contains("Swing")) holderType = HOLDER_SWING;
        else if (mainClass.contains("SDL")) holderType = HOLDER_SDL;

        loadedImages = new HashMap<>(initialCapacity);
        loadedMusic = new HashMap<>(initialCapacity);
    }

    /**
     * Gets the current instance's main class name.
     *
     * @return Main class name.
     */
    public static String getMainClassName() {
        if (mainClassName.length() < 1 || mainClassName.equals("Unknown")) {
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

                if (mainClassName.length() >= 1) break;
            }
            if (mainClassName.length() < 1) mainClassName = "Unknown";
        }

        return mainClassName;
    }

    /**
     * Gets the number of currently loaded block-skins inside the game.
     *
     * @return Number of block skins.
     */
    public static int getNumberLoadedBlockSkins() {
        switch (AnimatedBackgroundHook.getResourceHook()) {
            case AnimatedBackgroundHook.HOLDER_SLICK:
                return ResourceHolder.imgNormalBlockList.size();
            case AnimatedBackgroundHook.HOLDER_SWING:
                return ResourceHolderSwing.imgNormalBlockList.size();
            case AnimatedBackgroundHook.HOLDER_SDL:
                return ResourceHolderSDL.imgNormalBlockList.size();
            default:
                return 0;
        }
    }

    public static SDLSurface getGraphicsSDL(RendererSDL renderer) {
        Class<RendererSDL> local = RendererSDL.class;
        Field localField;
        try {
            localField = local.getDeclaredField("graphics");
            localField.setAccessible(true);
            return (SDLSurface) localField.get(renderer);
        } catch (Exception e) {
            log.error("Failed to extract graphics from SDL renderer.");
            return null;
        }
    }

    public static Graphics2D getGraphicsSwing(RendererSwing renderer) {
        Class<RendererSwing> local = RendererSwing.class;
        Field localField;
        try {
            localField = local.getDeclaredField("graphics");
            localField.setAccessible(true);
            return (Graphics2D) localField.get(renderer);
        } catch (Exception e) {
            log.error("Failed to extract graphics from Swing renderer.");
            return null;
        }
    }

    public static Graphics getGraphicsSlick(RendererSlick renderer) {
        Class<RendererSlick> local = RendererSlick.class;
        Field localField;
        try {
            localField = local.getDeclaredField("graphics");
            localField.setAccessible(true);
            return (Graphics) localField.get(renderer);
        } catch (Exception e) {
            log.error("Failed to extract graphics from Slick renderer.");
            return null;
        }
    }

    /**
     * Adds an image to the custom image library
     *
     * @param filePath Path of image file
     * @param name     Identifier name
     */
    public void loadImage(String filePath, String name) {
        switch (holderType) {
            case HOLDER_SLICK:
                loadedImages.put(name, new RuntimeImage.Slick(ResourceHolder.loadImage(filePath)));
                break;
            case HOLDER_SWING:
                loadedImages.put(name, new RuntimeImage.Swing(ResourceHolderSwing.loadImage(ResourceHolderSwing.getURL(filePath))));
                break;
            case HOLDER_SDL:
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

    /**
     * Sets rotation centre for an image when using Slick renderer.
     *
     * @param name Image name
     * @param x    X-coordinate relative to image's top-left corner
     * @param y    Y-coordinate relative to image's top-left corner
     */
    public void setRotationCentre(String name, float x, float y) {
        final RuntimeImage<?> image = getImageAt(name);

        if (holderType == HOLDER_SLICK && image instanceof RuntimeImage.Slick) {
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

        if (holderType == HOLDER_SLICK && image instanceof RuntimeImage.Slick) {
            ((RuntimeImage.Slick) image).image.setRotation(a);
        }
    }

    /**
     * Draws image to game.
     *
     * @param engine   GameEngine to draw with.
     * @param name     Identifier of image.
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
        final RuntimeImage<?> runtimeImage = getImageAt(name);

        switch (holderType) {
            case HOLDER_SLICK:
                if (!(runtimeImage instanceof RuntimeImage.Slick)) {
                    log.error("Image '" + name + "' is not a Slick image!");
                    return;
                }

                org.newdawn.slick.Image toDrawSlick = ((RuntimeImage.Slick) runtimeImage).image;

                int fx = x + (int) (srcSizeX * scale);
                int fy = y + (int) (srcSizeY * scale);

                org.newdawn.slick.Color filter = new org.newdawn.slick.Color(red, green, blue, alpha);

                toDrawSlick.draw(x, y, fx, fy, srcX, srcY, srcX + srcSizeX, srcY + srcSizeY, filter);

                break;
            case HOLDER_SWING:
                if (!(runtimeImage instanceof RuntimeImage.Swing)) {
                    log.error("Image '" + name + "' is not a Swing image!");
                    return;
                }

                localSwingGraphics = getGraphicsSwing((RendererSwing) engine.owner.receiver);
                if (localSwingGraphics == null) {
                    log.error("Swing graphics is null!");
                    return;
                }

                java.awt.Image toDrawSwing = ((RuntimeImage.Swing) runtimeImage).image;

                int fxSw = x + (int) (srcSizeX * scale);
                int fySw = y + (int) (srcSizeY * scale);

                localSwingGraphics.setColor(new java.awt.Color(red, green, blue, alpha));
                localSwingGraphics.drawImage(toDrawSwing, x, y, fxSw, fySw, srcX, srcY, srcX + srcSizeX, srcY + srcSizeY, null);
                localSwingGraphics.setColor(new java.awt.Color(255, 255, 255, 255));
                break;
            case HOLDER_SDL:
                if (!(runtimeImage instanceof RuntimeImage.SDL)) {
                    log.error("Image '" + name + "' is not a SDL image!");
                    return;
                }

                localSDLGraphics = getGraphicsSDL((RendererSDL) engine.owner.receiver);
                sdljava.video.SDLSurface toDrawSDL = ((RuntimeImage.SDL) runtimeImage).image;

                int dx = (int) (srcSizeX * scale);
                int dy = (int) (srcSizeY * scale);
                try {
                    toDrawSDL.blitSurface(new SDLRect(srcX, srcY, srcSizeX, srcSizeY), localSDLGraphics, new SDLRect(x, y, dx, dy));
                } catch (Exception e) {
                    // DO NOTHING AT ALL.
                }
                break;
        }
    }


    /**
     * Draws image to game.
     *
     * @param engine   GameEngine to draw with.
     * @param name     Identifier of image.
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
        if (x < x2 || y < y2) return;
        final RuntimeImage<?> runtimeImage = getImageAt(name);

        switch (holderType) {
            case HOLDER_SLICK:
                if (!(runtimeImage instanceof RuntimeImage.Slick)) {
                    log.error("Image '" + name + "' is not a Slick image!");
                    return;
                }

                org.newdawn.slick.Image toDrawSlick = ((RuntimeImage.Slick) runtimeImage).image;

                org.newdawn.slick.Color filter = new org.newdawn.slick.Color(red, green, blue, alpha);

                toDrawSlick.draw(x, y, x2, y2, srcX, srcY, srcX + srcSizeX, srcY + srcSizeY, filter);
                break;
            case HOLDER_SWING:
                if (!(runtimeImage instanceof RuntimeImage.Swing)) {
                    log.error("Image '" + name + "' is not a Swing image!");
                    return;
                }

                localSwingGraphics = getGraphicsSwing((RendererSwing) engine.owner.receiver);
                if (localSwingGraphics == null) {
                    log.error("Swing graphics is null!");
                    return;
                }

                java.awt.Image toDrawSwing = ((RuntimeImage.Swing) runtimeImage).image;

                localSwingGraphics.setColor(new java.awt.Color(red, green, blue, alpha));
                localSwingGraphics.drawImage(toDrawSwing, x, y, x2, y2, srcX, srcY, srcX + srcSizeX, srcY + srcSizeY, null);
                localSwingGraphics.setColor(new java.awt.Color(255, 255, 255, 255));
                break;
            case HOLDER_SDL:
                if (!(runtimeImage instanceof RuntimeImage.SDL)) {
                    log.error("Image '" + name + "' is not a SDL image!");
                    return;
                }

                localSDLGraphics = getGraphicsSDL((RendererSDL) engine.owner.receiver);
                sdljava.video.SDLSurface toDrawSDL = ((RuntimeImage.SDL) runtimeImage).image;

                int dx = x2 - x;
                int dy = y2 - y;
                try {
                    toDrawSDL.blitSurface(new SDLRect(srcX, srcY, srcSizeX, srcSizeY), localSDLGraphics, new SDLRect(x, y, dx, dy));
                } catch (Exception e) {
                    // DO NOTHING AT ALL.
                }
                break;
        }
    }

    /**
     * Draws image to game.
     *
     * @param engine   GameEngine to draw with.
     * @param name     Identifier of image.
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
        if (sx <= 0 || sy <= 0) return;
        final RuntimeImage<?> runtimeImage = getImageAt(name);

        switch (holderType) {
            case HOLDER_SLICK:
                if (!(runtimeImage instanceof RuntimeImage.Slick)) {
                    log.error("Image '" + name + "' is not a Slick image!");
                    return;
                }

                org.newdawn.slick.Image toDrawSlick = ((RuntimeImage.Slick) runtimeImage).image;

                int fx = x + sx;
                int fy = y + sy;

                org.newdawn.slick.Color filter = new org.newdawn.slick.Color(red, green, blue, alpha);

                toDrawSlick.draw(x, y, fx, fy, srcX, srcY, srcX + srcSizeX, srcY + srcSizeY, filter);

                break;
            case HOLDER_SWING:
                if (!(runtimeImage instanceof RuntimeImage.Swing)) {
                    log.error("Image '" + name + "' is not a Swing image!");
                    return;
                }

                localSwingGraphics = getGraphicsSwing((RendererSwing) engine.owner.receiver);
                if (localSwingGraphics == null) {
                    log.error("Swing graphics is null!");
                    return;
                }

                java.awt.Image toDrawSwing = ((RuntimeImage.Swing) runtimeImage).image;

                int fxSw = x + sx;
                int fySw = y + sy;

                localSwingGraphics.setColor(new java.awt.Color(red, green, blue, alpha));
                localSwingGraphics.drawImage(toDrawSwing, x, y, fxSw, fySw, srcX, srcY, srcX + srcSizeX, srcY + srcSizeY, null);
                localSwingGraphics.setColor(new java.awt.Color(255, 255, 255, 255));
                break;
            case HOLDER_SDL:
                if (!(runtimeImage instanceof RuntimeImage.SDL)) {
                    log.error("Image '" + name + "' is not a SDL image!");
                    return;
                }

                localSDLGraphics = getGraphicsSDL((RendererSDL) engine.owner.receiver);
                sdljava.video.SDLSurface toDrawSDL = ((RuntimeImage.SDL) runtimeImage).image;

                try {
                    toDrawSDL.blitSurface(new SDLRect(srcX, srcY, srcSizeX, srcSizeY), localSDLGraphics, new SDLRect(x, y, sx, sy));
                } catch (Exception e) {
                    // DO NOTHING AT ALL.
                }
                break;
        }
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
     * Draws image to game with string colour definition.
     * 8 character RRGGBBAA hex code or 6 character RRGGBB hex code.
     *
     * @param engine   GameEngine to draw with.
     * @param name     Identifier of image.
     * @param x        X position
     * @param y        Y position
     * @param srcX     Source X position
     * @param srcY     Source Y position
     * @param srcSizeX Source X size
     * @param srcSizeY Source Y size
     * @param color    Hex. code string of colour.
     * @param scale    Image scale
     */
    public void drawImage(GameEngine engine, String name, int x, int y, int srcX, int srcY, int srcSizeX, int srcSizeY, String color, float scale) {
        String lc;
        lc = color.replace("#", "");
        lc = lc.toLowerCase();

        String red, green, blue, alpha = "FF";

        if (lc.length() != 8 && lc.length() != 6) {
            return;
        }

        red = lc.substring(0, 2);
        green = lc.substring(2, 4);
        blue = lc.substring(4, 6);
        if (lc.length() == 8) alpha = lc.substring(6, 8);

        int r, g, b, a;
        r = Integer.parseInt(red, 16);
        g = Integer.parseInt(green, 16);
        b = Integer.parseInt(blue, 16);
        a = lc.length() == 8 ? Integer.parseInt(alpha, 16) : 255;

        drawImage(engine, name, x, y, srcX, srcY, srcSizeX, srcSizeY, r, g, b, a, scale);
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
        if (holderType == HOLDER_SWING) {
            log.warn("BGM is not supported on Swing.");
        } else if (holderType == HOLDER_SLICK) {
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
        } else if (holderType == HOLDER_SDL) {
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
            case HOLDER_SLICK:
                if (!NullpoMinoSlick.propConfig.getProperty("option.bgm", false)) return;

                int no = ResourceHolder.bgm.length - 1;
                Music[] newBGM = new Music[no];

                System.arraycopy(ResourceHolder.bgm, 0, newBGM, 0, newBGM.length);

                ResourceHolder.bgm = newBGM.clone();
                if (showerr) log.info("Removed BGM at " + no);
                return;
            case HOLDER_SWING:
                log.warn("BGM is not supported on Swing.");
                return;
            case HOLDER_SDL:
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
            if (holderType == HOLDER_SLICK) {
                loadedMusic.put(name, new RuntimeMusic.Slick(new Music(fileName, true)));
            } else if (holderType == HOLDER_SDL) {
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
                case HOLDER_SLICK:
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
                case HOLDER_SWING:
                    log.warn("BGM is not supported on Swing.");
                    return;
                case HOLDER_SDL:
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
            case HOLDER_SLICK:
                return ResourceHolder.bgm.length - BGMStatus.BGM_COUNT;
            case HOLDER_SDL:
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
        if (holderType == HOLDER_SLICK) {
            for (RuntimeMusic<?> mus : loadedMusic.values()) {
                if (mus instanceof RuntimeMusic.Slick) {
                    ((RuntimeMusic.Slick) mus).music.pause();
                    ((RuntimeMusic.Slick) mus).music.stop();
                }
            }
        } else if (holderType == HOLDER_SDL) {
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
