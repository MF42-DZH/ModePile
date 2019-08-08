package zeroxfc.nullpo.custom.libs.backgroundtypes;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.gui.sdl.NullpoMinoSDL;
import mu.nu.nullpo.gui.sdl.ResourceHolderSDL;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.gui.slick.ResourceHolder;
import mu.nu.nullpo.gui.swing.NullpoMinoSwing;
import mu.nu.nullpo.gui.swing.ResourceHolderSwing;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;

public abstract class AnimatedBackgroundHook {
	protected static Logger log = Logger.getLogger(AnimatedBackgroundHook.class);
	private static int LAST_BG = -1;
	private static int LAST_FADE_BG = -1;

	/** Animation type ID */
	public static final int ANIMATION_NONE = 0,
			                ANIMATION_FRAME_ANIM = 1,
	                        ANIMATION_PULSE_HORIZONTAL_BARS = 2,
	                        ANIMATION_PULSE_VERTICAL_BARS = 3,
	                        ANIMATION_CIRCULAR_PULSE = 4,
	                        ANIMATION_DIAGONAL_RIPPLE = 5,
	                        ANIMATION_SLIDING_TILES = 6;

	/** ResourceHolder--- types */
	public static final int HOLDER_SLICK = 0,
			                HOLDER_SWING = 1,
			                HOLDER_SDL = 2;

	/**
	 * Gets the current ResourceHolder--- type.
	 * @return Integer that represents the holder type.
	 */
	public static int getResourceHook() {
		if (ResourceHolder.imgNormalBlockList != null) return HOLDER_SLICK;
		else if (ResourceHolderSwing.imgNormalBlockList != null) return HOLDER_SWING;
		else if (ResourceHolderSDL.imgNormalBlockList != null) return HOLDER_SDL;
		else return -1;
	}

	public static int getBGState(GameManager owner) {
		int bg = owner.backgroundStatus.bg;
		if (bg < 0 || bg > 19) return LAST_BG;
		else {
			LAST_BG = bg;
			return bg;
		}
	}

	public static int getFadeBGState(GameManager owner) {
		int bg = owner.backgroundStatus.fadebg;
		if (bg < 0 || bg > 19) return LAST_FADE_BG;
		else {
			LAST_FADE_BG = bg;
			return bg;
		}
	}

	public static boolean getInitialBGState() {
		switch (getResourceHook()) {
			case HOLDER_SLICK:
				return NullpoMinoSlick.propConfig.getProperty("option.showbg", true);
			case HOLDER_SWING:
				return NullpoMinoSwing.propConfig.getProperty("option.showbg", true);
			case HOLDER_SDL:
				return NullpoMinoSDL.propConfig.getProperty("option.showbg", true);
			default:
				return false;
		}
	}

	public static void setBGState(EventReceiver receiver, boolean bool) {
		try {
			Class<EventReceiver> renderer = EventReceiver.class;
			Field localField;
			localField = renderer.getDeclaredField("showbg");
			localField.setAccessible(true);
			if (localField.getBoolean(receiver) != bool) {
				localField.setBoolean(receiver, bool);
				log.info("showbg in current EventReceiver set successfully to " + bool);
			}
		} catch (Exception e) {
			log.error("Access to showbg in current EventReceiver failed.");
		}
	}

	protected int ID;

	public abstract void update();
	public abstract void reset();
	public abstract void draw(GameEngine engine);
	public abstract void setBG(int bg);
	public abstract void setBG(String filePath);
	public abstract int getID();
}
