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
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;

import java.lang.reflect.Field;

public abstract class AnimatedBackgroundHook {
	protected static Logger log = Logger.getLogger(AnimatedBackgroundHook.class);
	private static int LAST_BG = -1;
	private static int LAST_FADE_BG = -1;

	/** Animation type ID
	 *
	 * Please add a new one for every new background type made.
	 */
	public static final int ANIMATION_NONE = 0,
			                ANIMATION_FRAME_ANIM = 1,
	                        ANIMATION_PULSE_HORIZONTAL_BARS = 2,
	                        ANIMATION_PULSE_VERTICAL_BARS = 3,
	                        ANIMATION_CIRCULAR_RIPPLE = 4,
	                        ANIMATION_DIAGONAL_RIPPLE = 5,
	                        ANIMATION_SLIDING_TILES = 6,  // NOTE: SDL window handling is gross.
	                        ANIMATION_TGM3TI_STYLE = 7;  // NOTE: Swing and SDL will not be able to use rotations.

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

	public static void disableDefaultBG(GameManager owner) {
		getBGState(owner);
		getFadeBGState(owner);

		owner.backgroundStatus.bg = -1;
		owner.backgroundStatus.fadebg = -1;
	}

	public static void enableDefaultBG(GameManager owner) {
		owner.backgroundStatus.bg = LAST_BG;
		owner.backgroundStatus.fadebg = LAST_FADE_BG;
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

	public static boolean getBGState(EventReceiver receiver) {
		try {
			Class<EventReceiver> renderer = EventReceiver.class;
			Field localField;
			localField = renderer.getDeclaredField("showbg");
			localField.setAccessible(true);
			final boolean b = localField.getBoolean(receiver);
			log.info("showbg in current EventReceiver gotten successfully as " + b);
			return b;
		} catch (Exception e) {
			log.error("Access to showbg in current EventReceiver failed.");
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

	// Fuzzy equals.
	protected static boolean almostEqual(double a, double b, double eps){
		return Math.abs(a - b) < eps;
	}

	public void setExternalHolder(ResourceHolderCustomAssetExtension holder) {
		customHolder = holder;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	protected int ID;
	protected ResourceHolderCustomAssetExtension customHolder;
	protected String imageName;

	/**
	 * Performs an update tick on the background. Advisably used in onLast.
	 */
	public abstract void update();

	/**
	 * Resets the background to its base state.
	 */
	public abstract void reset();

	/**
	 * Draws the background to the game screen.
	 * @param engine Current GameEngine instance
	 * @param playerID Current player ID (1P = 0)
	 */
	public abstract void draw(GameEngine engine, int playerID);

	/**
	 * Change BG to one of the default ones.
	 * @param bg New BG number
	 */
	public abstract void setBG(int bg);

	/**
	 * Change BG to a custom BG using its file path.
	 * @param filePath File path of new background
	 */
	public abstract void setBG(String filePath);

	/**
	 * This last one is important. In the case that any of the child types are used, it allows identification.
	 * The identification can be used to allow casting during operations.
	 * @return Identification number of child class.
	 */
	public abstract int getID();
}
