package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.gui.EffectObject;
import mu.nu.nullpo.gui.sdl.NullpoMinoSDL;
import mu.nu.nullpo.gui.sdl.RendererSDL;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.gui.slick.RendererSlick;
import mu.nu.nullpo.gui.swing.RendererSwing;
// import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.backgroundtypes.AnimatedBackgroundHook;

import java.lang.reflect.*;
import java.util.ArrayList;

public class RendererExtension {
	/** Debug logger */
	// private static Logger log = Logger.getLogger(RendererExtension.class);

	/** Run class in debug mode? */
	// private static final boolean DEBUG = true;

	/** Block break effect types used in addBlockBreakEffect() */
	public static final int TYPE_NORMAL_BREAK = 1, TYPE_GEM_BREAK = 2;

	/**
	 * Draw piece
	 * @param x X-Coordinate of piece
	 * @param y Y-Coordinate of piece
	 * @param piece The piece to draw
	 * @param scale Scale factor at which the piece is drawn in
	 * @param darkness How dark is the piece
	 */
	public static void drawPiece(EventReceiver receiver, int x, int y, Piece piece, float scale, float darkness) {
		int renderer = AnimatedBackgroundHook.getResourceHook();

		Class<?> local;
		Method localMethod;

		switch (renderer) {
			case AnimatedBackgroundHook.HOLDER_SLICK:
				local = RendererSlick.class;
				break;
			case AnimatedBackgroundHook.HOLDER_SWING:
				local = RendererSwing.class;
				break;
			case AnimatedBackgroundHook.HOLDER_SDL:
				local = RendererSDL.class;
				break;
			default:
				return;
		}

		try {
			localMethod = local.getDeclaredMethod("drawPiece", int.class, int.class, Piece.class, float.class, float.class);
			localMethod.setAccessible(true);
			localMethod.invoke(receiver, x, y, piece, scale, darkness);
		} catch (Exception e) {
			// DO NOTHING
		}
	}

	/**
	 * Add block break effect at custom location given
	 * @param receiver Current renderer in game
	 * @param x X-Coordinate of top left corner of 16x16 block
	 * @param y Y-Coordinate of top left corner of 16x16 block
	 * @param blk Block to break
	 */
	public static void addBlockBreakEffect(EventReceiver receiver, int x, int y, Block blk) {
		if (receiver == null || blk == null) return;
		addBlockBreakEffect(receiver, blk.isGemBlock() ? 2 : 1, x, y, blk.getDrawColor());
	}

	/**
	 * Draw a block break effect at a custom position where coordinates stood for the left-hand corner of a block.
	 * @param receiver Current renderer in game
	 * @param effectType Block break effect type
	 * @param x X-Coordinate of top left corner of 16x16 block
	 * @param y Y-Coordinate of top left corner of 16x16 block
	 * @param color Effect colour
	 */
	public static void addBlockBreakEffect(EventReceiver receiver, int effectType, int x, int y, int color) {
		if (receiver == null) return;

		int renderer = AnimatedBackgroundHook.getResourceHook();
		if (renderer == AnimatedBackgroundHook.HOLDER_SWING) return;

		Class<?> local;
		Field effectList;
		ArrayList<EffectObject> list;
		boolean show;

		switch (renderer) {
			case AnimatedBackgroundHook.HOLDER_SLICK:
				local = RendererSlick.class;
				show = NullpoMinoSlick.propConfig.getProperty("option.showlineeffect", true);
				break;
			case AnimatedBackgroundHook.HOLDER_SDL:
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

				list = (ArrayList<EffectObject>)(effectList.get(receiver));

				list.add(new EffectObject(effectType, x, y, color));
				effectList.set(receiver, list);
			}
		} catch (Exception e) {
			// DO NOTHING
		}
	}
}
