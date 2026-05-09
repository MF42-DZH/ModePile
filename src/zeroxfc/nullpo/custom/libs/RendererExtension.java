package zeroxfc.nullpo.custom.libs;

import java.awt.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.function.BiPredicate;
import java.util.function.IntBinaryOperator;
import java.util.function.IntSupplier;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.gui.EffectObject;
import mu.nu.nullpo.gui.sdl.NullpoMinoSDL;
import mu.nu.nullpo.gui.sdl.RendererSDL;
import mu.nu.nullpo.gui.sdl.ResourceHolderSDL;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.gui.slick.RendererSlick;
import mu.nu.nullpo.gui.slick.ResourceHolder;
import mu.nu.nullpo.gui.swing.NullpoMinoSwing;
import mu.nu.nullpo.gui.swing.RendererSwing;
import mu.nu.nullpo.gui.swing.ResourceHolderSwing;
import org.apache.log4j.Logger;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import sdljava.video.SDLRect;
import sdljava.video.SDLSurface;
import sdljava.video.SDLVideo;
import zeroxfc.nullpo.custom.libs.backgroundtypes.AnimatedBackgroundHook;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomFieldDrawing;
import zeroxfc.nullpo.custom.libs.types.ImageChunk;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;
import zeroxfc.nullpo.custom.libs.types.RuntimeImage;
import zeroxfc.nullpo.custom.libs.types.tuples.FloatPair;
import zeroxfc.nullpo.custom.libs.types.tuples.IntPair;

public class RendererExtension {
    /** Type of break effect. */
    public enum BreakEffect {
        NORMAL(1), GEM(2);

        private final int value;

        BreakEffect(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Debug logger
     */
    private static final Logger log = Logger.getLogger(RendererExtension.class);
    /**
     * Run class in debug mode?
     */
    private static final boolean DEBUG = true;

    // Default speed meter colours.
    public static final int[] SPEED_METER_GREEN = { 0, 255, 0 };
    public static final int[] SPEED_METER_RED = { 255, 0, 0 };

    // Default outline colours.
    public static final int[] YELLOW_OUTLINE = { 255, 255, 0 };
    public static final int[] WHITE_OUTLINE = { 255, 255, 255 };
    public static final int[] DARK_GREY_OUTLINE = { 128, 128, 128 };

    // Local instance for custom resource holder.
    private final CustomResourceHolder customGraphics;
    private final PrimitiveDrawingHook drawing;

    private static final String WHITE_OUTER_FRAME_NAME = "frame_outer_white";
    private static final String WHITE_INNER_FRAME_NAME = "frame_inner_white";

    private enum FrameChunk {
        // There is no MIDDLE_MIDDLE as that is filled by the field background.
        TOP_LEFT(0, 0),
        TOP_MIDDLE(4, 0),
        TOP_RIGHT(8, 0),
        MIDDLE_LEFT(0, 4),
        MIDDLE_RIGHT(8, 4),
        BOTTOM_LEFT(0, 8),
        BOTTOM_MIDDLE(4, 8),
        BOTTOM_RIGHT(8, 8),
        METER_SEP_TOP(12, 0),
        METER_SEP_MIDDLE(12, 4),
        METER_SEP_BOTTOM(12, 8);

        private final IntPair sourceLocation;
        private static final IntPair DIMS = IntPair.of(4, 4);
        private static final FloatPair SIZE_S = FloatPair.of(0.5f, 0.5f);
        private static final FloatPair SIZE_N = FloatPair.of(1f, 1f);
        private static final FloatPair SIZE_L = FloatPair.of(2f, 2f);

        FrameChunk(int sx, int sy) {
            this.sourceLocation = IntPair.of(sx, sy);
        }

        /**
         * Gets this frame's chunk in image form.
         * DO NOT MODIFY ANY OF THE ARRAYS IN THE CHUNK.
         *
         * @param x           Top left X coordinate
         * @param y           Top left Y coordinate
         * @param displaySize Field display size
         * @return Image chunk for drawing that part of the frame
         */
        public ImageChunk atLocation(int x, int y, int displaySize) {
            final FloatPair size = displaySize == 0 ? SIZE_N : (displaySize == -1 ? SIZE_S : SIZE_L);

            return new ImageChunk(
                ObjectAlignment.TOP_LEFT,
                x, y,
                sourceLocation.valL, sourceLocation.valR,
                DIMS.valL, DIMS.valR,
                size.valL, size.valR
            );
        }
    }

    /** Use this constructor when no custom resource holder is used by the gamemode. */
    public RendererExtension() {
        this(new CustomResourceHolder(2));
    }

    public RendererExtension(CustomResourceHolder customGraphics) {
        this.customGraphics = customGraphics;
        this.drawing = new PrimitiveDrawingHook(customGraphics);

        // Load the white frame image, to eliminate possible stutter from lazy-loading it.
        final String customSkinDirProp = "custom.skin.directory";
        final String whiteOuterFrame = "/graphics/" + WHITE_OUTER_FRAME_NAME + ".png";
        final String whiteInnerFrame = "/graphics/" + WHITE_INNER_FRAME_NAME + ".png";

        switch (CustomResourceHolder.getCurrentNullpominoRuntime()) {
            case SLICK:
                customGraphics.loadImage(
                    NullpoMinoSlick.propConfig.getProperty(customSkinDirProp, "res") + whiteOuterFrame,
                    WHITE_OUTER_FRAME_NAME
                );
                customGraphics.loadImage(
                    NullpoMinoSlick.propConfig.getProperty(customSkinDirProp, "res") + whiteInnerFrame,
                    WHITE_INNER_FRAME_NAME
                );
                break;
            case SWING:
                customGraphics.loadImage(
                    NullpoMinoSwing.propConfig.getProperty(customSkinDirProp, "res") + whiteOuterFrame,
                    WHITE_OUTER_FRAME_NAME
                );
                customGraphics.loadImage(
                    NullpoMinoSwing.propConfig.getProperty(customSkinDirProp, "res") + whiteInnerFrame,
                    WHITE_INNER_FRAME_NAME
                );
                break;
            case SDL:
                customGraphics.loadImage(
                    NullpoMinoSDL.propConfig.getProperty(customSkinDirProp, "res") + whiteOuterFrame,
                    WHITE_OUTER_FRAME_NAME
                );
                customGraphics.loadImage(
                    NullpoMinoSDL.propConfig.getProperty(customSkinDirProp, "res") + whiteInnerFrame,
                    WHITE_INNER_FRAME_NAME
                );
                break;
            default:
                break;
        }
    }

    /**
     * Draw a custom-scaled piece that does not have to have a scale of 0.5f, 1f or 2f.
     *
     * @param receiver Renderer to draw with
     * @param x        X-coordinate of piece's top-left corner
     * @param y        Y-coordinate of piece's top-left corner
     * @param piece    The piece to draw
     * @param alpha    Opacity
     * @param scale    Scale factor at which the piece is drawn in
     * @param darkness Darkness value (0f = None, negative = lighter, positive = darker)
     */
    public void drawScaledPiece(EventReceiver receiver, int x, int y, Piece piece, float alpha, float scale, float darkness) {
        if (piece.big) {
            for (int i = 0; i < piece.block.length; i++) {
                int x2 = x + (int) (piece.dataX[piece.direction][i] * 32 * scale);
                int y2 = y + (int) (piece.dataY[piece.direction][i] * 32 * scale);

                Block blkTemp = new Block(piece.block[i]);
                blkTemp.darkness = darkness;

                drawScaledBlock(receiver, x2, y2, blkTemp.getDrawColor(), blkTemp.skin, blkTemp.getAttribute(Block.BLOCK_ATTRIBUTE_BONE), blkTemp.darkness, alpha, 2f * scale, blkTemp.attribute);
            }
        } else {
            for (int i = 0; i < piece.block.length; i++) {
                int x2 = x + (int) (piece.dataX[piece.direction][i] * 16 * scale);
                int y2 = y + (int) (piece.dataY[piece.direction][i] * 16 * scale);

                Block blkTemp = new Block(piece.block[i]);
                blkTemp.darkness = darkness;

                drawScaledBlock(receiver, x2, y2, blkTemp.getDrawColor(), blkTemp.skin, blkTemp.getAttribute(Block.BLOCK_ATTRIBUTE_BONE), blkTemp.darkness, alpha, scale, blkTemp.attribute);
            }
        }
    }

    /**
     * Draw a custom-scaled piece that does not have to have a scale of 0.5f, 1f or 2f.<br />
     * Does not draw blocks which intersect the area outside the GameEngine instance's field.
     *
     * @param receiver Renderer to draw with
     * @param x        X-coordinate of piece's top-left corner
     * @param y        Y-coordinate of piece's top-left corner
     * @param piece    The piece to draw
     * @param alpha    Opacity
     * @param scale    Scale factor at which the piece is drawn in
     * @param darkness Darkness value (0f = None, negative = lighter, positive = darker)
     */
    public void drawScaledPiece(EventReceiver receiver, GameEngine engine, int playerID, int x, int y, Piece piece, float alpha, float scale, float darkness) {
        if (piece.big) {
            for (int i = 0; i < piece.block.length; i++) {
                int x2 = x + (int) (piece.dataX[piece.direction][i] * 32 * scale);
                int y2 = y + (int) (piece.dataY[piece.direction][i] * 32 * scale);

                final int minX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
                final int minY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
                final int maxX = minX + ((engine.field.getWidth() - 1) * 16);
                final int maxY = minY + ((FieldManipulation.getFullHeight(engine.field) - 1) * 16);

                if (x2 < minX || y2 < minY || x2 > maxX || y2 > maxY) continue;

                Block blkTemp = new Block(piece.block[i]);
                blkTemp.darkness = darkness;

                drawScaledBlock(receiver, x2, y2, blkTemp.getDrawColor(), blkTemp.skin, blkTemp.getAttribute(Block.BLOCK_ATTRIBUTE_BONE), blkTemp.darkness, alpha, scale * 2f, blkTemp.attribute);
            }
        } else {
            for (int i = 0; i < piece.block.length; i++) {
                int x2 = x + (int) (piece.dataX[piece.direction][i] * 16 * scale);
                int y2 = y + (int) (piece.dataY[piece.direction][i] * 16 * scale);

                final int minX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
                final int minY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
                final int maxX = minX + ((engine.field.getWidth() - 1) * 16);
                final int maxY = minY + ((FieldManipulation.getFullHeight(engine.field) - 1) * 16);

                if (x2 < minX || y2 < minY || x2 > maxX || y2 > maxY) continue;

                Block blkTemp = new Block(piece.block[i]);
                blkTemp.darkness = darkness;

                drawScaledBlock(receiver, x2, y2, blkTemp.getDrawColor(), blkTemp.skin, blkTemp.getAttribute(Block.BLOCK_ATTRIBUTE_BONE), blkTemp.darkness, alpha, scale, blkTemp.attribute);
            }
        }
    }

    /**
     * Draw a custom-scaled piece that does not have to have a scale of 0.5f, 1f or 2f.<br />
     * <br />
     * It can be aligned to any corner, side midpoint or centre of its bounding box.
     *
     * @param receiver  Renderer to draw with
     * @param x         X-coordinate of piece's top-left corner
     * @param y         Y-coordinate of piece's top-left corner
     * @param alignment Alignment setting (use <code>ObjectAlignment</code>)
     * @param piece     The piece to draw
     * @param alpha    Opacity
     * @param scale     Scale factor at which the piece is drawn in
     * @param darkness  Darkness value (0f = None, negative = lighter, positive = darker)
     */
    public void drawAlignedScaledPiece(EventReceiver receiver, int x, int y, ObjectAlignment alignment, Piece piece, float alpha, float scale, float darkness) {
        final int baseSize = 16 * Math.max(piece.getWidth(), piece.getHeight());
        int offsetX, offsetY;

        switch (alignment) {
            case TOP_MIDDLE:
            case MIDDLE_MIDDLE:
            case BOTTOM_MIDDLE:
                offsetX = (int) (baseSize * 0.5f * scale);
                break;
            case TOP_RIGHT:
            case MIDDLE_RIGHT:
            case BOTTOM_RIGHT:
                offsetX = (int) (baseSize * scale);
                break;
            default:
                offsetX = 0;
                break;
        }

        switch (alignment) {
            case MIDDLE_LEFT:
            case MIDDLE_MIDDLE:
            case MIDDLE_RIGHT:
                offsetY = (int) (baseSize * 0.5f * scale);
                break;
            case BOTTOM_LEFT:
            case BOTTOM_MIDDLE:
            case BOTTOM_RIGHT:
                offsetY = (int) (baseSize * scale);
                break;
            default:
                offsetY = 0;
                break;
        }

        if (piece.big) {
            offsetX *= 2;
            offsetY *= 2;
        }

        drawScaledPiece(receiver, x - offsetX, y - offsetY, piece, alpha, scale, darkness);
    }

    /**
     * Add block break effect at custom location given
     *
     * @param receiver Current renderer in game
     * @param x        X-Coordinate of top left corner of 16x16 block
     * @param y        Y-Coordinate of top left corner of 16x16 block
     * @param blk      Block to break
     */
    public void addBlockBreakEffect(EventReceiver receiver, int x, int y, Block blk) {
        if (receiver == null || blk == null) return;
        addBlockBreakEffect(receiver, blk.isGemBlock() ? BreakEffect.GEM : BreakEffect.NORMAL, x, y, blk.getDrawColor());
    }

    /**
     * Draw a block break effect at a custom position where coordinates stood for the left-hand corner of a block.
     *
     * @param receiver   Current renderer in game
     * @param effectType Block break effect type
     * @param x          X-Coordinate of top left corner of 16x16 block
     * @param y          Y-Coordinate of top left corner of 16x16 block
     * @param color      Effect colour
     */
    public void addBlockBreakEffect(EventReceiver receiver, BreakEffect effectType, int x, int y, int color) {
        if (receiver == null) return;

        final CustomResourceHolder.Runtime renderer = CustomResourceHolder.getCurrentNullpominoRuntime();
        if (renderer == CustomResourceHolder.Runtime.SWING) return;

        Class<?> local;
        Field effectList;
        ArrayList<EffectObject> list;
        boolean show;

        switch (renderer) {
            case SLICK:
                local = RendererSlick.class;
                show = NullpoMinoSlick.propConfig.getProperty("option.showlineeffect", true);
                break;
            case SDL:
                local = RendererSDL.class;
                show = NullpoMinoSDL.propConfig.getProperty("option.showlineeffect", true);
                break;
            default:
                return;
        }

        try {
            if (show) {
                effectList = local.getDeclaredField("effectlist");
                effectList.setAccessible(true);

                /*
                 * This should not return anything other than ArrayList<EffectObject>,
                 * as verified in the source code (see RendererSlick.java, RendererSwing.java
                 * and RendererSDL.java).
                 *
                 * Use @SuppressWarnings("unchecked").
                 */
                list = (ArrayList<EffectObject>) (effectList.get(receiver));
                list.add(new EffectObject(effectType.getValue(), x, y, color));
            }
        } catch (Exception e) {
            if (DEBUG) log.error("Failed to extract and modify effectList.");
        }
    }

    /**
     * Draws a block that can be scaled to a scale that isn't 0.5f, 1f or 2f.
     *
     * @param receiver Renderer to draw with
     * @param x        X-coordinate of block top-left corner
     * @param y        Y-coordinate of block top-left corner
     * @param color    Block colour (use colours in <code>Block</code> class)
     * @param skin     Block skin (when in doubt use <code>getSkin()</code> on a <code>GameEngine</code> instance)
     * @param bone     Use bone block skin?
     * @param darkness Darkness value (0f = None, negative = lighter, positive = darker)
     * @param alpha    Alpha value (transparency; 1f = opaque, 0f = transparent)
     * @param scale    Scale of drawing
     * @param attr     Block attributes (use attrs in <code>Block</code> class and combine with <code>|</code>, or use <code>0</code> for none)
     */
    public void drawScaledBlock(EventReceiver receiver, int x, int y, int color, int skin, boolean bone, float darkness, float alpha, float scale, int attr) {
        final CustomResourceHolder.Runtime renderer = CustomResourceHolder.getCurrentNullpominoRuntime();

        if ((color <= Block.BLOCK_COLOR_INVALID)) return;
        if (skin >= CustomResourceHolder.getNumberLoadedBlockSkins()) skin = 0;

        final boolean isSpecialBlocks = (color >= Block.BLOCK_COLOR_COUNT);
        final boolean isSticky = customGraphics.getSkinStickyFlag(skin);

        final int size = (int) (16 * scale);

        int srcSize;
        if (scale <= 0.5f) {
            srcSize = 8;
        } else if (scale <= 1.0f) {
            srcSize = 16;
        } else {
            srcSize = 32;
        }

        int sx = color * srcSize;
        if (bone) sx += 9 * srcSize;

        int sy = 0;
        if (isSpecialBlocks) sx = ((color - Block.BLOCK_COLOR_COUNT) + 18) * srcSize;

        if (isSticky) {
            if (isSpecialBlocks) {
                sx = (color - Block.BLOCK_COLOR_COUNT) * srcSize;
                sy = 18 * srcSize;
            } else {
                sx = 0;
                if ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_UP) != 0) sx |= 0x1;
                if ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_DOWN) != 0) sx |= 0x2;
                if ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_LEFT) != 0) sx |= 0x4;
                if ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT) != 0) sx |= 0x8;
                sx *= srcSize;
                sy = color * srcSize;
                if (bone) sy += 9 * srcSize;
            }
        }

        final RuntimeImage<?> runtimeImage = customGraphics.getBlockSkin(scale, skin);

        int imageWidth = runtimeImage.getWidth();
        if ((sx >= imageWidth) && (imageWidth != -1)) sx = 0;
        int imageHeight = runtimeImage.getHeight();
        if ((sy >= imageHeight) && (imageHeight != -1)) sy = 0;

        if (renderer == CustomResourceHolder.Runtime.SLICK) {
            // region Slick Case
            final Graphics graphics = customGraphics.getGraphicsSlick((RendererSlick) receiver, true);
            if (graphics == null) return;

            final Image img = ((RuntimeImage.Slick) runtimeImage).image;

            Color filter = new Color(Color.white);
            filter.a = alpha;
            if (darkness > 0) {
                filter = filter.darker(darkness);
            }

            graphics.drawImage(img, x, y, x + size, y + size, sx, sy, sx + srcSize, sy + srcSize, filter);

            if (isSticky && !isSpecialBlocks) {
                int h = size / 2;
                int d2 = 16 * srcSize;
                int h2 = srcSize / 2;

                if (((attr & Block.BLOCK_ATTRIBUTE_CONNECT_UP) != 0) && ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_LEFT) != 0))
                    graphics.drawImage(img, x, y, x + h, y + h, d2, sy, d2 + h2, sy + h2, filter);
                if (((attr & Block.BLOCK_ATTRIBUTE_CONNECT_UP) != 0) && ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT) != 0))
                    graphics.drawImage(img, x + h, y, x + h + h, y + h, d2 + h2, sy, d2 + h2 + h2, sy + h2, filter);
                if (((attr & Block.BLOCK_ATTRIBUTE_CONNECT_DOWN) != 0) && ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_LEFT) != 0))
                    graphics.drawImage(img, x, y + h, x + h, y + h + h, d2, sy + h2, d2 + h2, sy + h2 + h2, filter);
                if (((attr & Block.BLOCK_ATTRIBUTE_CONNECT_DOWN) != 0) && ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT) != 0))
                    graphics.drawImage(img, x + h, y + h, x + h + h, y + h + h, d2 + h2, sy + h2, d2 + h2 + h2, sy + h2 + h2, filter);
            }

            if (darkness < 0) {
                Color brightfilter = new Color(Color.white);
                brightfilter.a = -darkness;
                graphics.setColor(brightfilter);
                graphics.fillRect(x, y, size, size);
            }
            // endregion Slick Case
        } else if (renderer == CustomResourceHolder.Runtime.SWING) {
            // region Swing Case
            final Graphics2D graphics = customGraphics.getGraphicsSwing((RendererSwing) receiver, true);
            if (graphics == null) return;

            final java.awt.Image img = ((RuntimeImage.Swing) runtimeImage).image;
            final Composite backupComposite = graphics.getComposite();

            boolean showbg = AnimatedBackgroundHook.getBGState(receiver);
            if ((alpha >= 0f) && (alpha < 1f) && (!showbg)) {
                final AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
                graphics.setComposite(composite);
            }

            boolean simpleblock;
            try {
                Class<RendererSwing> rs = RendererSwing.class;
                Field sb = rs.getDeclaredField("simpleblock");
                sb.setAccessible(true);
                simpleblock = sb.getBoolean(receiver);
            } catch (Exception e) {
                if (DEBUG) log.error("Failed to extract boolean from simpleblock field in Swing Renderer");
                return;
            }

            if (simpleblock) {
                switch (color) {
                    case Block.BLOCK_COLOR_GRAY:
                        graphics.setColor(java.awt.Color.lightGray);
                        break;
                    case Block.BLOCK_COLOR_RED:
                        graphics.setColor(java.awt.Color.red);
                        break;
                    case Block.BLOCK_COLOR_ORANGE:
                        graphics.setColor(java.awt.Color.orange);
                        break;
                    case Block.BLOCK_COLOR_YELLOW:
                        graphics.setColor(java.awt.Color.yellow);
                        break;
                    case Block.BLOCK_COLOR_GREEN:
                        graphics.setColor(java.awt.Color.green);
                        break;
                    case Block.BLOCK_COLOR_CYAN:
                        graphics.setColor(java.awt.Color.cyan);
                        break;
                    case Block.BLOCK_COLOR_BLUE:
                        graphics.setColor(java.awt.Color.blue);
                        break;
                    case Block.BLOCK_COLOR_PURPLE:
                        graphics.setColor(java.awt.Color.magenta);
                        break;
                    default:
                        graphics.setColor(java.awt.Color.white);
                        break;
                }
                graphics.drawRect(x, y, size - 1, size - 1);

                if (showbg) {
                    graphics.setColor(java.awt.Color.black);
                    graphics.fillRect(x + 1, y + 1, size - 2, size - 2);
                }
            } else {
                graphics.drawImage(img, x, y, x + size, y + size, sx, sy, sx + srcSize, sy + srcSize, null);

                if (isSticky && !isSpecialBlocks) {
                    int h = size / 2;
                    int d2 = 16 * srcSize;
                    int h2 = srcSize / 2;

                    if (((attr & Block.BLOCK_ATTRIBUTE_CONNECT_UP) != 0) && ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_LEFT) != 0))
                        graphics.drawImage(img, x, y, x + h, y + h, d2, sy, d2 + h2, sy + h2, null);
                    if (((attr & Block.BLOCK_ATTRIBUTE_CONNECT_UP) != 0) && ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT) != 0))
                        graphics.drawImage(img, x + h, y, x + h + h, y + h, d2 + h2, sy, d2 + h2 + h2, sy + h2, null);
                    if (((attr & Block.BLOCK_ATTRIBUTE_CONNECT_DOWN) != 0) && ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_LEFT) != 0))
                        graphics.drawImage(img, x, y + h, x + h, y + h + h, d2, sy + h2, d2 + h2, sy + h2 + h2, null);
                    if (((attr & Block.BLOCK_ATTRIBUTE_CONNECT_DOWN) != 0) && ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT) != 0))
                        graphics.drawImage(img, x + h, y + h, x + h + h, y + h + h, d2 + h2, sy + h2, d2 + h2 + h2, sy + h2 + h2, null);
                }
            }

            graphics.setComposite(backupComposite);

            if ((darkness != 0) || ((alpha >= 0f) && (alpha < 1f) && (showbg))) {
                final java.awt.Color backupColor = graphics.getColor();

                java.awt.Color filterColor;
                if ((alpha >= 0f) && (alpha < 1f) && (showbg)) {
                    filterColor = new java.awt.Color(0f, 0f, 0f, alpha);
                } else if (darkness > 0) {
                    filterColor = new java.awt.Color(0f, 0f, 0f, darkness);
                } else {
                    filterColor = new java.awt.Color(1f, 1f, 1f, -darkness);
                }

                graphics.setColor(filterColor);
                graphics.fillRect(x, y, size, size);
                graphics.setColor(backupColor);
            }
            // endregion Swing Case
        } else if (renderer == CustomResourceHolder.Runtime.SDL) {
            try {
                // region SDL Case
                final SDLSurface graphics = customGraphics.getGraphicsSDL((RendererSDL) receiver, true);
                if (graphics == null) return;

                final SDLSurface img = ((RuntimeImage.SDL) runtimeImage).image;
                final SDLRect rectSrc = new SDLRect(sx, sy, srcSize, srcSize);
                final SDLRect rectDst = new SDLRect(x, y, size, size);

                NullpoMinoSDL.fixRect(rectSrc, rectDst);

                if (alpha < 1.0f) {
                    int alphalv = (int) (255 * alpha);
                    img.setAlpha(SDLVideo.SDL_SRCALPHA | SDLVideo.SDL_RLEACCEL, alphalv);
                } else {
                    img.setAlpha(0, 255);
                }

                img.blitSurface(rectSrc, graphics, rectDst);

                if (isSticky && !isSpecialBlocks) {
                    int h = (size / 2);
                    int d2 = 16 * srcSize;
                    int h2 = srcSize / 2;

                    SDLRect rectDst2 = null;
                    SDLRect rectSrc2 = null;

                    if (((attr & Block.BLOCK_ATTRIBUTE_CONNECT_UP) != 0) && ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_LEFT) != 0)) {
                        rectDst2 = new SDLRect(x, y, h, h);
                        rectSrc2 = new SDLRect(d2, sy, h2, h2);
                        NullpoMinoSDL.fixRect(rectSrc2, rectDst2);
                        img.blitSurface(rectSrc2, graphics, rectDst2);
                    }
                    if (((attr & Block.BLOCK_ATTRIBUTE_CONNECT_UP) != 0) && ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT) != 0)) {
                        rectDst2 = new SDLRect(x + h, y, h, h);
                        rectSrc2 = new SDLRect(d2 + h2, sy, h2, h2);
                        NullpoMinoSDL.fixRect(rectSrc2, rectDst2);
                        img.blitSurface(rectSrc2, graphics, rectDst2);
                    }
                    if (((attr & Block.BLOCK_ATTRIBUTE_CONNECT_DOWN) != 0) && ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_LEFT) != 0)) {
                        rectDst2 = new SDLRect(x, y + h, h, h);
                        rectSrc2 = new SDLRect(d2, sy + h2, h2, h2);
                        NullpoMinoSDL.fixRect(rectSrc2, rectDst2);
                        img.blitSurface(rectSrc2, graphics, rectDst2);
                    }
                    if (((attr & Block.BLOCK_ATTRIBUTE_CONNECT_DOWN) != 0) && ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT) != 0)) {
                        rectDst2 = new SDLRect(x + h, y + h, h, h);
                        rectSrc2 = new SDLRect(d2 + h2, sy + h2, h2, h2);
                        NullpoMinoSDL.fixRect(rectSrc2, rectDst2);
                        img.blitSurface(rectSrc2, graphics, rectDst2);
                    }
                }

                if (darkness > 0) {
                    int alphalv = (int) (255 * darkness);
                    ResourceHolderSDL.imgBlankBlack.setAlpha(SDLVideo.SDL_SRCALPHA | SDLVideo.SDL_RLEACCEL, alphalv);
                    ResourceHolderSDL.imgBlankBlack.blitSurface(new SDLRect(0, 0, size, size), graphics, rectDst);
                } else if (darkness < 0) {
                    int alphalv = (int) (255 * -darkness);
                    ResourceHolderSDL.imgBlankWhite.setAlpha(SDLVideo.SDL_SRCALPHA | SDLVideo.SDL_RLEACCEL, alphalv);
                    ResourceHolderSDL.imgBlankWhite.blitSurface(new SDLRect(0, 0, size, size), graphics, rectDst);
                }
                // endregion SDL Case
            } catch (Exception e) {
                if (DEBUG) log.error("Failed to draw block using SDL renderer:\n", e);
            }
        }
    }

    /**
     * Draws a block that can be scaled to a scale that isn't 0.5f, 1f or 2f.<br />
     * <br />
     * It can be aligned to a corner, the midpoint of one of its sides or its centre.
     *
     * @param receiver  Renderer to draw with
     * @param x         X-coordinate of block anchor point
     * @param y         Y-coordinate of block anchor point
     * @param alignment Alignment setting (use <code>ObjectAlignment</code>)
     * @param color     Block colour (use colours in <code>Block</code> class)
     * @param skin      Block skin (when in doubt use <code>getSkin()</code> on a <code>GameEngine</code> instance)
     * @param bone      Use bone block skin?
     * @param darkness  Darkness value (0f = None, negative = lighter, positive = darker)
     * @param alpha     Alpha value (transparency; 1f = opaque, 0f = transparent)
     * @param scale     Scale of drawing
     * @param attr      Block attributes (use attrs in <code>Block</code> class and combine with <code>|</code>, or use <code>0</code> for none)
     */
    public void drawAlignedScaledBlock(EventReceiver receiver, int x, int y, ObjectAlignment alignment, int color, int skin, boolean bone, float darkness, float alpha, float scale, int attr) {
        final int baseSize = 16;
        int offsetX, offsetY;

        switch (alignment) {
            case TOP_MIDDLE:
            case MIDDLE_MIDDLE:
            case BOTTOM_MIDDLE:
                offsetX = (int) (baseSize * 0.5f * scale);
                break;
            case TOP_RIGHT:
            case MIDDLE_RIGHT:
            case BOTTOM_RIGHT:
                offsetX = (int) (baseSize * scale);
                break;
            default:
                offsetX = 0;
                break;
        }

        switch (alignment) {
            case MIDDLE_LEFT:
            case MIDDLE_MIDDLE:
            case MIDDLE_RIGHT:
                offsetY = (int) (baseSize * 0.5f * scale);
                break;
            case BOTTOM_LEFT:
            case BOTTOM_MIDDLE:
            case BOTTOM_RIGHT:
                offsetY = (int) (baseSize * scale);
                break;
            default:
                offsetY = 0;
                break;
        }

        drawScaledBlock(receiver, x - offsetX, y - offsetY, color, skin, bone, darkness, alpha, scale, attr);
    }

    public void drawDirectSpeedMeter(EventReceiver receiver, int x, int y, float value, float scaleX, float scaleY) {
        drawDirectSpeedMeter(receiver, x, y, value, scaleX, scaleY, SPEED_METER_GREEN, SPEED_METER_RED);
    }

    /**
     * Draws a speed meter at any pixel location on the screen, with any scale.
     *
     * @param receiver   Renderer to draw with
     * @param x          X-coordinate of top-left corner
     * @param y          Y-coordinate of top-left corner
     * @param value      Float in the range <code>0 <= value <= 1</code> that denotes how full the meter is
     * @param scaleX     Horizontal scale factor of speed meter drawn
     * @param scaleY     Vertical scale factor of speed meter drawn
     * @param colorBack  Base color (default: green)
     * @param colorFront Fill color (default: red)
     */
    public void drawDirectSpeedMeter(EventReceiver receiver, int x, int y, float value, float scaleX, float scaleY, int[] colorBack, int[] colorFront) {
        final CustomResourceHolder.Runtime renderer = CustomResourceHolder.getCurrentNullpominoRuntime();

        final int baseWidth = (int) (42 * scaleX);
        final int meterMax = baseWidth - 2;
        final int baseHeight = (int) (4 * scaleY);

        if (renderer == CustomResourceHolder.Runtime.SLICK) {
            //region Slick Case
            Graphics graphics = customGraphics.getGraphicsSlick((RendererSlick) receiver, true);
            final Color colorBase = new org.newdawn.slick.Color(colorBack[0], colorBack[1], colorBack[2]);
            final Color colorFill = new org.newdawn.slick.Color(colorFront[0], colorFront[1], colorFront[2]);

            if (graphics == null) return;

            graphics.setColor(Color.black);
            graphics.fillRect(x, y, baseWidth, baseHeight);
            graphics.setColor(colorBase);
            graphics.fillRect(x + 1, y + 1, baseWidth - 2, baseHeight - 2);

            int tempSpeedMeter = (int) (value * meterMax);
            if ((tempSpeedMeter < 0) || (tempSpeedMeter > meterMax)) tempSpeedMeter = meterMax;

            if (tempSpeedMeter > 0) {
                graphics.setColor(colorFill);
                graphics.fillRect(x + 1, y + 1, tempSpeedMeter, baseHeight - 2);
            }

            graphics.setColor(Color.white);
            //endregion Slick Case
        } else if (renderer == CustomResourceHolder.Runtime.SWING) {
            //region Swing Case
            Graphics2D graphics = customGraphics.getGraphicsSwing((RendererSwing) receiver, true);
            final java.awt.Color colorBase = new java.awt.Color(colorBack[0], colorBack[1], colorBack[2]);
            final java.awt.Color colorFill = new java.awt.Color(colorFront[0], colorFront[1], colorFront[2]);

            if (graphics == null) return;

            graphics.setColor(java.awt.Color.black);
            graphics.fillRect(x, y, baseWidth, baseHeight);
            graphics.setColor(colorBase);
            graphics.fillRect(x + 1, y + 1, baseWidth - 2, baseHeight - 2);

            int tempSpeedMeter = (int) (value * meterMax);
            if ((tempSpeedMeter < 0) || (tempSpeedMeter > meterMax)) tempSpeedMeter = meterMax;

            if (tempSpeedMeter > 0) {
                graphics.setColor(colorFill);
                graphics.fillRect(x + 1, y + 1, tempSpeedMeter + 1, baseHeight - 1);
            }

            graphics.setColor(java.awt.Color.white);
            //endregion Swing Case
        } else if (renderer == CustomResourceHolder.Runtime.SDL) {
            //region SDL Case
            SDLSurface graphics = customGraphics.getGraphicsSDL((RendererSDL) receiver, true);

            if (graphics == null) return;

            SDLRect rectSrc = new SDLRect(0, 0, 42, 4);
            SDLRect rectDst = new SDLRect(x, y, baseWidth, baseHeight);

            try {
                ResourceHolderSDL.imgSprite.blitSurface(rectSrc, graphics, rectDst);
            } catch (Exception e) {
                log.debug("SDLException thrown", e);
            }

            int tempSpeedMeter = (int) (value * meterMax);
            if ((tempSpeedMeter < 0) || (tempSpeedMeter > meterMax)) tempSpeedMeter = meterMax;
            int tempSpeedMeter2 = (int) (value * 40);
            if ((tempSpeedMeter2 < 0) || (tempSpeedMeter2 > 40)) tempSpeedMeter2 = 40;

            if (tempSpeedMeter > 0) {
                SDLRect rectSrc2 = new SDLRect(0, 4, tempSpeedMeter2, 2);
                SDLRect rectDst2 = new SDLRect(x + 1, y + 1, tempSpeedMeter, baseHeight - 2);

                try {
                    ResourceHolderSDL.imgSprite.blitSurface(rectSrc2, graphics, rectDst2);
                } catch (Exception e) {
                    log.debug("SDLException thrown", e);
                }
            }
            //endregion SDL Case
        } else {
            log.error("Invalid renderer detected (Type -1)");
        }
    }

    public void drawAlignedSpeedMeter(EventReceiver receiver, int x, int y, ObjectAlignment alignment, float value, float scaleX, float scaleY) {
        drawAlignedSpeedMeter(receiver, x, y, alignment, value, scaleX, scaleY, SPEED_METER_GREEN, SPEED_METER_RED);
    }

    /**
     * Draws a speed meter aligned to one of its corners, a midpoint of one of its sides or its centre.
     *
     * @param receiver  Renderer to draw with
     * @param x         X-coordinate of anchor point
     * @param y         Y-coordinate of anchor point
     * @param alignment Alignment setting (use <code>ObjectAlignment</code>)
     * @param value     Float in the range <code>0 <= value <= 1</code> that denotes how full the meter is
     * @param scaleX   Horizontal scale factor of speed meter drawn
     * @param scaleY   Vertical scale factor of speed meter drawn
     * @param colorBack  Base color (default: green)
     * @param colorFront Fill color (default: red)
     */
    public void drawAlignedSpeedMeter(EventReceiver receiver, int x, int y, ObjectAlignment alignment, float value, float scaleX, float scaleY, int[] colorBack, int[] colorFront) {
        final int baseWidth = (int) (42 * scaleX);
        final int baseHeight = (int) (4 * scaleY);

        int offsetX, offsetY;
        switch (alignment) {
            case TOP_MIDDLE:
            case MIDDLE_MIDDLE:
            case BOTTOM_MIDDLE:
                offsetX = (int) (baseWidth * 0.5f * scaleX);
                break;
            case TOP_RIGHT:
            case MIDDLE_RIGHT:
            case BOTTOM_RIGHT:
                offsetX = (int) (baseWidth * scaleX);
                break;
            default:
                offsetX = 0;
                break;
        }

        switch (alignment) {
            case MIDDLE_LEFT:
            case MIDDLE_MIDDLE:
            case MIDDLE_RIGHT:
                offsetY = (int) (baseHeight * 0.5f * scaleY);
                break;
            case BOTTOM_LEFT:
            case BOTTOM_MIDDLE:
            case BOTTOM_RIGHT:
                offsetY = (int) (baseHeight * scaleY);
                break;
            default:
                offsetY = 0;
                break;
        }

        drawDirectSpeedMeter(receiver, x - offsetX, y - offsetY, value, scaleX, scaleY, colorBack, colorFront);
    }

    /**
     * Improved "Hebo Hidden" curtain effect with support for more than just skin 0. Recommended for use with
     * {@code HasCustomFieldDrawing}'s field drawing overrides.
     *
     * @param receiver Current renderer
     * @param engine   Current game engine
     * @param playerID Current player ID
     * @param height   Hebo Hidden curtain height
     * @param template Block to use as template for drawing
     */
    public void drawImprovedHeboHidden(EventReceiver receiver, GameEngine engine, int playerID, int height, Block template) {
        if (engine.field == null) return;

        final int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
        final int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;

        for (int y = engine.field.getHeight() - 1; y > engine.field.getHeight() - height - 1; --y) {
            for (int x = 0; x < engine.field.getWidth(); ++x) {
                drawScaledBlock(
                    receiver,
                    baseX + (16 * x), baseY + (16 * y),
                    template.color,
                    template.skin,
                    template.getAttribute(Block.BLOCK_ATTRIBUTE_BONE),
                    template.darkness,
                    template.alpha,
                    1f,
                    template.attribute
                );
            }
        }
    }

    /**
     * Draws a Heboris-style post-hold outline.
     *
     * @param receiver  Current renderer
     * @param engine    Current game engine
     * @param playerID  Current player ID
     */
    public void drawPostHoldOutline(EventReceiver receiver, GameEngine engine, int playerID) {
        if (engine.nowPieceObject == null) return;

        if (engine.gameActive && engine.stat == GameEngine.STAT_MOVE && engine.holdDisable && engine.ruleopt.holdEnable && (engine.statc[0] > 1 || engine.ruleopt.moveFirstFrame)) {
            final int select = (engine.statc[0] / 5) % 3;
            final int[] outline = select == 0 ? YELLOW_OUTLINE : (select == 1 ? WHITE_OUTLINE : DARK_GREY_OUTLINE);
            drawPieceOutline(receiver, engine, playerID, engine.nowPieceObject.big ? 4 : 2, outline);
        }
    }

    /**
     * Draws a coloured outline around a game's current piece. Useful for TI-like post-hold outlines.
     *
     * @param receiver  Current renderer
     * @param engine    Current game engine
     * @param playerID  Current player ID
     * @param thickness Outline thickness (px, suggested range: [1, 8])
     * @param color     Outline colour (RGB)
     */
    public void drawPieceOutline(EventReceiver receiver, GameEngine engine, int playerID, int thickness, int[] color) {
        if (engine.nowPieceObject == null) return;
        if (color.length != 3 || thickness <= 0) return;

        final Piece piece = engine.nowPieceObject;
        final int baseScale = piece.big ? 32 : 16;

        final int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
        final int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;

        final BiPredicate<Integer, Integer> hasBlockAt = (x, y) -> {
            for (int i = 0; i < piece.getMaxBlock(); ++i) {
                final int pdX = piece.dataX[piece.direction][i];
                final int pdY = piece.dataY[piece.direction][i];

                if (pdX == x && pdY == y) return true;
            }

            return false;
        };

        int tlX, tlY;
        for (int i = 0; i < piece.getMaxBlock(); ++i) {
            final Block blk = piece.block[i];
            final int pdX = piece.dataX[piece.direction][i];
            final int pdY = piece.dataY[piece.direction][i];
            final int fX = (piece.big ? pdX * 2 : pdX) + engine.nowPieceX;
            final int fY = (piece.big ? pdY * 2 : pdY) + engine.nowPieceY;

            if (fX < 0 || fX >= engine.field.getWidth() || fY < 0 || fY >= engine.field.getHeight()) continue;

            // Sides
            if (!blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
                tlX = baseX + (fX * 16);
                tlY = baseY + (fY * 16);

                drawing.drawRectangle(
                    receiver,
                    tlX, tlY,
                    baseScale, thickness,
                    color[0], color[1], color[2], 255,
                    true
                );
            }

            if (!blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)) {
                tlX = baseX + (fX * 16);
                tlY = baseY + (fY * 16);

                drawing.drawRectangle(
                    receiver,
                    tlX, tlY,
                    thickness, baseScale,
                    color[0], color[1], color[2], 255,
                    true
                );
            }

            if (!blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)) {
                tlX = baseX + (fX * 16) + (baseScale - thickness);
                tlY = baseY + (fY * 16);

                drawing.drawRectangle(
                    receiver,
                    tlX, tlY,
                    thickness, baseScale,
                    color[0], color[1], color[2], 255,
                    true
                );
            }

            if (!blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
                tlX = baseX + (fX * 16);
                tlY = baseY + (fY * 16) + (baseScale - thickness);

                drawing.drawRectangle(
                    receiver,
                    tlX, tlY,
                    baseScale, thickness,
                    color[0], color[1], color[2], 255,
                    true
                );
            }

            // Corners
            if (blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)
                && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)
                && !hasBlockAt.test(pdX - 1, pdY - 1)) {
                tlX = baseX + (fX * 16);
                tlY = baseY + (fY * 16);

                drawing.drawRectangle(
                    receiver,
                    tlX, tlY,
                    thickness, thickness,
                    color[0], color[1], color[2], 255,
                    true
                );
            }

            if (blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)
                && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)
                && !hasBlockAt.test(pdX + 1, pdY - 1)) {
                tlX = baseX + (fX * 16) + (baseScale - thickness);
                tlY = baseY + (fY * 16);

                drawing.drawRectangle(
                    receiver,
                    tlX, tlY,
                    thickness, thickness,
                    color[0], color[1], color[2], 255,
                    true
                );
            }

            if (blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)
                && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)
                && !hasBlockAt.test(pdX - 1, pdY + 1)) {
                tlX = baseX + (fX * 16);
                tlY = baseY + (fY * 16) + (baseScale - thickness);

                drawing.drawRectangle(
                    receiver,
                    tlX, tlY,
                    thickness, thickness,
                    color[0], color[1], color[2], 255,
                    true
                );
            }

            if (blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)
                && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)
                && !hasBlockAt.test(pdX + 1, pdY + 1)) {
                tlX = baseX + (fX * 16) + (baseScale - thickness);
                tlY = baseY + (fY * 16) + (baseScale - thickness);

                drawing.drawRectangle(
                    receiver,
                    tlX, tlY,
                    thickness, thickness,
                    color[0], color[1], color[2], 255,
                    true
                );
            }
        }
    }

    private static RuntimeImage<?> fieldBgSmall;
    private static RuntimeImage<?> fieldBgNormal;
    private static RuntimeImage<?> fieldBgLarge;

    // Slick-only.
    private static final Mirror.FieldAccessor<EventReceiver, Float> fieldBrightAccessor;
    private static final Mirror.FieldAccessor<EventReceiver, Boolean> showBgAccessor;
    private static final Mirror.FieldAccessor<EventReceiver, Boolean> showMeterAccessor;

    static {
        if (CustomResourceHolder.getCurrentNullpominoRuntime() == CustomResourceHolder.Runtime.SLICK) {
            fieldBrightAccessor = Mirror.getFieldAccessor(RendererSlick.class, "fieldbgbright");
        } else {
            fieldBrightAccessor = null;
        }

        showBgAccessor = Mirror.getFieldAccessor(EventReceiver.class, "showbg");
        showMeterAccessor = Mirror.getFieldAccessor(EventReceiver.class, "showmeter");
    }

    /** Checks if the field frame meter is showing. */
    public static boolean getShowMeter(EventReceiver receiver) {
        return showMeterAccessor.get(receiver);
    }

    // Get the field images from the resource holders.
    private static void findFieldImages() {
        if (fieldBgNormal != null) return;
        final CustomResourceHolder.Runtime renderer = CustomResourceHolder.getCurrentNullpominoRuntime();

        if (renderer == CustomResourceHolder.Runtime.SLICK) {
            fieldBgSmall = new RuntimeImage.Slick(ResourceHolder.imgFieldbg2Small);
            fieldBgNormal = new RuntimeImage.Slick(ResourceHolder.imgFieldbg2);
            fieldBgLarge = new RuntimeImage.Slick(ResourceHolder.imgFieldbg2Big);
        } else if (renderer == CustomResourceHolder.Runtime.SWING) {
            fieldBgSmall = new RuntimeImage.Swing(ResourceHolderSwing.imgFieldbg2Small);
            fieldBgNormal = new RuntimeImage.Swing(ResourceHolderSwing.imgFieldbg2);
            fieldBgLarge = new RuntimeImage.Swing(ResourceHolderSwing.imgFieldbg2Big);
        } else if (renderer == CustomResourceHolder.Runtime.SDL) {
            fieldBgSmall = new RuntimeImage.SDL(ResourceHolderSDL.imgFieldbg2Small);
            fieldBgNormal = new RuntimeImage.SDL(ResourceHolderSDL.imgFieldbg2);
            fieldBgLarge = new RuntimeImage.SDL(ResourceHolderSDL.imgFieldbg2Big);
        } else {
            fieldBgSmall = null;
            fieldBgNormal = null;
            fieldBgLarge = null;
        }
    }

    @FunctionalInterface
    private static interface FrameDraw {
        void drawAt(int chunkX, int chunkY, FrameChunk chunk, int atX, int atY);
    }

    /**
     * Draw a custom playfield frame. Does not work in the SDL renderer. The speed meter's colour will use the default
     * colours as specified by the current game engine.
     * <p>
     * The colouring functions are expected to return an RGB24 colour as a single int in the lower 24 bits, and its two parameters will
     * be fed the current chunk of the field frame's relative coordinates to the field. {@code x} (first param) ranges from 0 until
     * {@code field width * 4 + 2} (and {@code + 2} again to the maximum if the meter is drawn too), and {@code y} (second param) ranges
     * from 0 until {@code field height * 4 + 2}.
     *
     * @param receiver    Current renderer
     * @param engine      Current game engine
     * @param x           Top left X-coordinate
     * @param y           Top left Y-coodinate
     * @param displaySize Display size
     * @param frameParams Frame and meter colouring parameters
     */
    public void drawCustomFrame(EventReceiver receiver, GameEngine engine, int x, int y, int displaySize, HasCustomFieldDrawing.FrameDrawingParameters frameParams) {
        final CustomResourceHolder.Runtime renderer = CustomResourceHolder.getCurrentNullpominoRuntime();

        final IntBinaryOperator outer = frameParams.outerFrameColouringFunction;
        final IntBinaryOperator inner = frameParams.innerFrameColouringFunction;
        IntSupplier meter = frameParams.meterColouringFunction;

        if (renderer == CustomResourceHolder.Runtime.SLICK && meter == null) {
            org.newdawn.slick.Color color;
            switch (engine.meterColor) {
                case GameEngine.METER_COLOR_GREEN:
                    color = org.newdawn.slick.Color.green;
                    break;
                case GameEngine.METER_COLOR_YELLOW:
                    color = org.newdawn.slick.Color.yellow;
                    break;
                case GameEngine.METER_COLOR_ORANGE:
                    color = org.newdawn.slick.Color.orange;
                    break;
                case GameEngine.METER_COLOR_RED:
                    color = org.newdawn.slick.Color.red;
                    break;
                default:
                    color = org.newdawn.slick.Color.white;
                    break;
            }

            meter = () -> (color.getRedByte() << 16) | (color.getGreenByte() << 8) | color.getBlueByte();
        } else if (renderer == CustomResourceHolder.Runtime.SWING && meter == null) {
            java.awt.Color color;
            switch (engine.meterColor) {
                case GameEngine.METER_COLOR_GREEN:
                    color = java.awt.Color.green;
                    break;
                case GameEngine.METER_COLOR_YELLOW:
                    color = java.awt.Color.yellow;
                    break;
                case GameEngine.METER_COLOR_ORANGE:
                    color = java.awt.Color.orange;
                    break;
                case GameEngine.METER_COLOR_RED:
                    color = java.awt.Color.red;
                    break;
                default:
                    color = java.awt.Color.white;
                    break;
            }

            meter = color::getRGB;
        } else if (renderer == CustomResourceHolder.Runtime.SDL) {
            // Yeah, this isn't happening due to how the SDL renderer uses an image sprite for the meter (?????).
            // Tinting images also seems to be particularly difficult, there doesn't seem to be an exposed API
            // for tint-by-multiply on SDLJava.

            drawFrame(receiver, engine, x, y, displaySize);
            return;
        }

        // Make sure we do have the field images.
        findFieldImages();
        if (fieldBgNormal == null) return;

        // See comment in above overload of drawCustomFrame.
        if (CustomResourceHolder.getCurrentNullpominoRuntime() == CustomResourceHolder.Runtime.SDL) {
            drawFrame(receiver, engine, x, y, displaySize);
            return;
        }

        int baseSize = 4;
        if (displaySize == -1) baseSize = 2;
        else if (displaySize == 1) baseSize = 8;

        final boolean showBg = showBgAccessor != null && showBgAccessor.get(receiver);
        final boolean showMeter = showMeterAccessor.get(receiver);

        int width = 10;
        int height = 20;

        if (engine != null && engine.field != null) {
            width = engine.field.getWidth();
            height = engine.field.getHeight();
        }

        // Field Background.
        final int fieldBgBright = (fieldBrightAccessor == null) ? 255 : (int) Math.round(255f * fieldBrightAccessor.get(receiver));
        if (fieldBgBright > 0) {
            if (width <= 10 && height <= 20) {
                RuntimeImage<?> fieldImage = fieldBgNormal;
                if (displaySize == -1) fieldImage = fieldBgSmall;
                else if (displaySize == 1) fieldImage = fieldBgLarge;

                customGraphics.drawImage(
                    engine, "fieldbg2", fieldImage,
                    x + 4, y + 4,
                    (x + 4) + (width * baseSize * 4), (y + 4) + (height * baseSize * 4),
                    0, 0, width * baseSize * 4, height * baseSize * 4,
                    255, 255, 255, fieldBgBright,
                    true
                );
            } else if (showBg) {
                drawing.drawRectangle(
                    receiver,
                    x + 4, y + 4,
                    width * baseSize * 4, height * baseSize * 4,
                    0, 0, 0, fieldBgBright,
                    true
                );
            }
        }

        // Draws the frame.
        int fullWidth = (width * 4) + 2;
        if (showMeter) fullWidth += 2;

        int fullHeight = (height * 4) + 2;

        final FrameDraw drawFrameChunk = (cX, cY, chunk, atX, atY) -> {
            final int outerColour = frameParams.outerFrameColouringFunction.applyAsInt(cX, cY);
            final int innerColour = frameParams.innerFrameColouringFunction.applyAsInt(cX, cY);

            customGraphics.drawOffsetImage(
                engine, WHITE_OUTER_FRAME_NAME,
                chunk.atLocation(atX, atY, displaySize),
                (outerColour >>> 16) & 0xFF, (outerColour >>> 8) & 0xFF, outerColour & 0xFF, 255
            );

            customGraphics.drawOffsetImage(
                engine, WHITE_INNER_FRAME_NAME,
                chunk.atLocation(atX, atY, displaySize),
                (innerColour >>> 16) & 0xFF, (innerColour >>> 8) & 0xFF, innerColour & 0xFF, 255
            );
        };

        for (int bY = 0; bY < fullHeight; ++bY) {
            for (int bX = 0; bX < fullWidth; ++bX) {
                int dX = x + (baseSize * bX);
                int dY = y + (baseSize * bY);

                if (bY == 0) {
                    // Top Row
                    if (bX == 0) {
                        // Top Left
                        drawFrameChunk.drawAt(bX, bY, FrameChunk.TOP_LEFT, dX, dY);
                    } else if (bX == fullWidth - 1) {
                        // Top Right
                        drawFrameChunk.drawAt(bX, bY, FrameChunk.TOP_RIGHT, dX, dY);
                    } else if (showMeter && bX == fullWidth - 3) {
                        // Meter Top
                        drawFrameChunk.drawAt(bX, bY, FrameChunk.METER_SEP_TOP, dX, dY);
                    } else {
                        // Top Middle
                        drawFrameChunk.drawAt(bX, bY, FrameChunk.TOP_MIDDLE, dX, dY);
                    }
                } else if (bY == fullHeight - 1) {
                    // Bottom Row
                    if (bX == 0) {
                        // Bottom Left
                        drawFrameChunk.drawAt(bX, bY, FrameChunk.BOTTOM_LEFT, dX, dY);
                    } else if (bX == fullWidth - 1) {
                        // Bottom Right
                        drawFrameChunk.drawAt(bX, bY, FrameChunk.BOTTOM_RIGHT, dX, dY);
                    } else if (showMeter && bX == fullWidth - 3) {
                        // Meter Bottom
                        drawFrameChunk.drawAt(bX, bY, FrameChunk.METER_SEP_BOTTOM, dX, dY);
                    } else {
                        // Bottom Middle
                        drawFrameChunk.drawAt(bX, bY, FrameChunk.BOTTOM_MIDDLE, dX, dY);
                    }
                } else {
                    // All Other Rows
                    // Bottom Row
                    if (bX == 0) {
                        // Middle Left
                        drawFrameChunk.drawAt(bX, bY, FrameChunk.MIDDLE_LEFT, dX, dY);

                        // Skip the middle columns.
                        bX += width * 4;
                    } else if (bX == fullWidth - 1) {
                        // Middle Right
                        drawFrameChunk.drawAt(bX, bY, FrameChunk.MIDDLE_RIGHT, dX, dY);
                    } else if (showMeter && bX == fullWidth - 3) {
                        // Meter Middle
                        drawFrameChunk.drawAt(bX, bY, FrameChunk.METER_SEP_MIDDLE, dX, dY);
                    }
                }
            }
        }

        // Draws the meter.
        if (showMeter) {
            int maxHeight = height * baseSize * 4;
            if (engine != null && engine.meterValue > 0) maxHeight -= engine.meterValue;

            if (maxHeight > 0) {
                drawing.drawRectangle(
                    receiver,
                    x + (width * baseSize * 4) + 8, y + 4,
                    4, maxHeight,
                    0, 0, 0, 255,
                    true
                );
            }

            if (engine != null && engine.meterValue > 0) {
                final int value = Math.min(height * baseSize * 4, engine.meterValue);
                if (value > 0) {
                    final int meterColour = meter.getAsInt();

                    drawing.drawRectangle(
                        receiver,
                        x + (width * baseSize * 4) + 8, y + (height * baseSize * 4) + 3 - (value - 1),
                        4, value,
                        (meterColour >>> 16) & 0xFF, (meterColour >>> 8) & 0xFF, meterColour & 0xFF, 255,
                        true
                    );
                }
            }
        }
    }

    private static final Mirror.MethodInvoker<EventReceiver, Object> drawFieldMI;
    private static final Mirror.MethodInvoker<EventReceiver, Object> drawFrameMI;
    private static final Mirror.MethodInvoker<EventReceiver, Object> drawNextMI;

    static {
        final CustomResourceHolder.Runtime renderer = CustomResourceHolder.getCurrentNullpominoRuntime();

        if (renderer == CustomResourceHolder.Runtime.SLICK) {
            drawFieldMI = Mirror.getMethodInvoker(RendererSlick.class, "drawField", int.class, int.class, GameEngine.class, int.class);
            drawFrameMI = Mirror.getMethodInvoker(RendererSlick.class, "drawFrame", int.class, int.class, GameEngine.class, int.class);
            drawNextMI = Mirror.getMethodInvoker(RendererSlick.class, "drawNext", int.class, int.class, GameEngine.class);
        } else if (renderer == CustomResourceHolder.Runtime.SWING) {
            drawFieldMI = Mirror.getMethodInvoker(RendererSwing.class, "drawField", int.class, int.class, GameEngine.class, int.class);
            drawFrameMI = Mirror.getMethodInvoker(RendererSwing.class, "drawFrame", int.class, int.class, GameEngine.class, int.class);
            drawNextMI = Mirror.getMethodInvoker(RendererSwing.class, "drawNext", int.class, int.class, GameEngine.class);
        } else if (renderer == CustomResourceHolder.Runtime.SDL) {
            drawFieldMI = Mirror.getMethodInvoker(RendererSDL.class, "drawField", int.class, int.class, GameEngine.class, int.class);
            drawFrameMI = Mirror.getMethodInvoker(RendererSDL.class, "drawFrame", int.class, int.class, GameEngine.class, int.class);
            drawNextMI = Mirror.getMethodInvoker(RendererSDL.class, "drawNext", int.class, int.class, GameEngine.class);
        } else {
            drawFieldMI = null;
            drawFrameMI = null;
            drawNextMI = null;
        }
    }

    /**
     * Draw a game engine's field.
     *
     * @param receiver    Current renderer
     * @param engine      Current game engine
     * @param x           X-coordinate of top-left corner
     * @param y           Y-coordinate of top-left corner
     * @param displaySize Field display size
     */
    public void drawField(EventReceiver receiver, GameEngine engine, int x, int y, int displaySize) {
        if (drawFieldMI == null) return;
        drawFieldMI.invoke(receiver, x, y,  engine, displaySize);
    }

    /**
     * Draw a game engine's frame.
     *
     * @param receiver    Current renderer
     * @param engine      Current game engine
     * @param x           X-coordinate of top-left corner
     * @param y           Y-coordinate of top-left corner
     * @param displaySize Field display size
     */
    public void drawFrame(EventReceiver receiver, GameEngine engine, int x, int y, int displaySize) {
        if (drawFrameMI == null) return;
        drawFrameMI.invoke(receiver, x, y, engine, displaySize);
    }

    /**
     * Draw a game engine's next queues.
     *
     * @param receiver    Current renderer
     * @param engine      Current game engine
     * @param x           X-coordinate of top-left corner
     * @param y           Y-coordinate of top-left corner
     */
    public void drawNext(EventReceiver receiver, GameEngine engine, int x, int y) {
        if (drawNextMI == null) return;
        drawNextMI.invoke(receiver, x, y, engine);
    }

    /**
     * Draws a background image of the default background images that NullpoMino loads.
     *
     * @param engine Current game engine
     * @param bg     Background [0, 19]
     */
    public void drawDefaultBackground(GameEngine engine, int bg) {
        final CustomResourceHolder.Runtime renderer = CustomResourceHolder.getCurrentNullpominoRuntime();
        if (bg < 0 || bg > 19) return;

        RuntimeImage<?> image = null;
        switch (renderer) {
            case SLICK:
                image = new RuntimeImage.Slick(ResourceHolder.imgPlayBG[bg]);
                break;
            case SWING:
                image = new RuntimeImage.Swing(ResourceHolderSwing.imgPlayBG[bg]);
                break;
            case SDL:
                image = new RuntimeImage.SDL(ResourceHolderSDL.imgPlayBG[bg]);
                break;
            default:
                break;
        }

        if (image == null) return;
        customGraphics.drawImage(
            engine,
            "bg" + bg,
            image,
            0, 0, 640, 480,
            0, 0, 640, 480,
            255, 255, 255, 255,
            true
        );
    }

    private static final Mirror.FieldAccessor<EventReceiver, Boolean> heavyEffectAccessor;

    static {
        final CustomResourceHolder.Runtime renderer = CustomResourceHolder.getCurrentNullpominoRuntime();

        if (renderer == CustomResourceHolder.Runtime.SLICK) {
            heavyEffectAccessor = Mirror.getFieldAccessor(RendererSlick.class, "heavyeffect");
        } else {
            heavyEffectAccessor = null;
        }
    }

    /**
     * Checks the current renderer to see if the user has enabled the performance-heavier background fade effect.
     *
     * @param receiver Current renderer
     * @return Fade setting enabled or not
     */
    public static boolean hasUserEnabledFadeEffect(EventReceiver receiver) {
        return heavyEffectAccessor != null && heavyEffectAccessor.get(receiver);
    }

    /**
     * Draw animated backgrounds that fade between the two. The fade effect only works in the Slick renderer if the
     * user has fades enabled. Will draw non-fading backgrounds if the user has fades disabled.
     *
     * @param receiver Current renderer
     * @param engine   Current game engine
     * @param playerID Current player id
     * @param from     Fade from this background
     * @param to       Fade into this background
     * @param progress Fade progress
     */
    public void drawFadingAnimatedBackground(EventReceiver receiver, GameEngine engine, int playerID, AnimatedBackgroundHook from, AnimatedBackgroundHook to, float progress) {
        if (progress < 0f) progress = 0f;
        else if (progress > 1f) progress = 1f;

        if (heavyEffectAccessor == null || !heavyEffectAccessor.get(receiver)) {
            if (progress > 0f) to.draw(engine, playerID);
            else from.draw(engine, playerID);
            return;
        }

        if (progress < 0.5) from.draw(engine, playerID);
        else to.draw(engine, playerID);

        drawing.drawRectangle(
            receiver,
            0, 0, 640, 480,
            0, 0, 0,
            progress < 0.5 ? Interpolation.lerp(0, 255, progress * 2) : Interpolation.lerp(255, 0, (progress - 0.5) * 2),
            true
        );
    }

    /**
     * Draws a fading background. Only works on the Slick renderer. Will draw non-fading backgrounds if the user has this disabled.
     *
     * @param receiver   Current renderer
     * @param engine     Current game engine
     * @param bgFadeFrom Fade from this background
     * @param bgFadeTo   Fade into this background
     * @param progress   Fade progress
     */
    public void drawFadingBackground(EventReceiver receiver, GameEngine engine, int bgFadeFrom, int bgFadeTo, float progress) {
        final CustomResourceHolder.Runtime renderer = CustomResourceHolder.getCurrentNullpominoRuntime();
        if (bgFadeFrom < 0 || bgFadeFrom > 19 || bgFadeTo < 0 || bgFadeTo > 19) return;

        if (progress < 0f) progress = 0f;
        else if (progress > 1f) progress = 1f;

        if (heavyEffectAccessor == null || !heavyEffectAccessor.get(receiver)) {
            drawDefaultBackground(engine, progress > 0f ? bgFadeTo : bgFadeFrom);
            return;
        }

        RuntimeImage<?> fadeFrom = null, fadeTo = null;
        switch (renderer) {
            case SLICK:
                fadeFrom = new RuntimeImage.Slick(ResourceHolder.imgPlayBG[bgFadeFrom]);
                fadeTo = new RuntimeImage.Slick(ResourceHolder.imgPlayBG[bgFadeTo]);
                break;
            case SWING:
                fadeFrom = new RuntimeImage.Swing(ResourceHolderSwing.imgPlayBG[bgFadeFrom]);
                fadeTo = new RuntimeImage.Swing(ResourceHolderSwing.imgPlayBG[bgFadeTo]);
                break;
            case SDL:
                fadeFrom = new RuntimeImage.SDL(ResourceHolderSDL.imgPlayBG[bgFadeFrom]);
                fadeTo = new RuntimeImage.SDL(ResourceHolderSDL.imgPlayBG[bgFadeTo]);
                break;
            default:
                break;
        }

        customGraphics.drawImage(
            engine,
            "bgF" + bgFadeFrom + "TO" + bgFadeTo,
            progress < 0.5 ? fadeFrom : fadeTo,
            0, 0, 640, 480,
            0, 0, 640, 480,
            255, 255, 255, 255,
            true
        );

        drawing.drawRectangle(
            receiver,
            0, 0, 640, 480,
            0, 0, 0,
            progress < 0.5 ? Interpolation.lerp(0, 255, progress * 2) : Interpolation.lerp(255, 0, (progress - 0.5) * 2),
            true
        );
    }

    /**
     * Draws a fading background. Only works on the Slick renderer. Will draw non-fading backgrounds if the user has this disabled.
     * This version does not have the black-screen tween-fade, and will crossfade between the two backgrounds instead.
     *
     * @param receiver   Current renderer
     * @param engine     Current game engine
     * @param bgFadeFrom Fade from this background
     * @param bgFadeTo   Fade into this background
     * @param progress   Fade progress
     */
    public void drawCrossfadingBackground(EventReceiver receiver, GameEngine engine, int bgFadeFrom, int bgFadeTo, float progress) {
        final CustomResourceHolder.Runtime renderer = CustomResourceHolder.getCurrentNullpominoRuntime();
        if (bgFadeFrom < 0 || bgFadeFrom > 19 || bgFadeTo < 0 || bgFadeTo > 19) return;

        if (progress < 0f) progress = 0f;
        else if (progress > 1f) progress = 1f;

        if (heavyEffectAccessor == null || !heavyEffectAccessor.get(receiver)) {
            drawDefaultBackground(engine, progress > 0f ? bgFadeTo : bgFadeFrom);
            return;
        }

        RuntimeImage<?> fadeFrom = null, fadeTo = null;
        switch (renderer) {
            case SLICK:
                fadeFrom = new RuntimeImage.Slick(ResourceHolder.imgPlayBG[bgFadeFrom]);
                fadeTo = new RuntimeImage.Slick(ResourceHolder.imgPlayBG[bgFadeTo]);
                break;
            case SWING:
                fadeFrom = new RuntimeImage.Swing(ResourceHolderSwing.imgPlayBG[bgFadeFrom]);
                fadeTo = new RuntimeImage.Swing(ResourceHolderSwing.imgPlayBG[bgFadeTo]);
                break;
            case SDL:
                fadeFrom = new RuntimeImage.SDL(ResourceHolderSDL.imgPlayBG[bgFadeFrom]);
                fadeTo = new RuntimeImage.SDL(ResourceHolderSDL.imgPlayBG[bgFadeTo]);
                break;
            default:
                break;
        }

        if (progress < 1f) {
            customGraphics.drawImage(
                engine,
                "bgF" + bgFadeFrom,
                fadeFrom,
                0, 0, 640, 480,
                0, 0, 640, 480,
                255, 255, 255, Interpolation.lerp(255, 0, progress),
                true
            );
        }

        if (progress > 0f) {
            customGraphics.drawImage(
                engine,
                "bgT" + bgFadeTo,
                fadeTo,
                0, 0, 640, 480,
                0, 0, 640, 480,
                255, 255, 255, Interpolation.lerp(0, 255, progress),
                true
            );
        }
    }
}
