package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.gui.sdl.ResourceHolderSDL;
import mu.nu.nullpo.gui.slick.ResourceHolder;
import mu.nu.nullpo.gui.swing.ResourceHolderSwing;

public class SoundLoader {
	public static final int LOADTYPE_FIREWORKS = 0,
							LOADTYPE_SCANNER = 1,
							LOADTYPE_MINESWEEPER = 2,
							LOADTYPE_COLLAPSE = 3;
	
	private static final int HOLDER_SLICK = 0,
							 HOLDER_SWING = 1,
							 HOLDER_SDL = 2;
	
	public static void loadSoundset(int loadType) {
		switch (loadType) {
		case LOADTYPE_FIREWORKS:
			importSound("fireworklaunch");
			importSound("fireworkexplode");
			break;
		case LOADTYPE_SCANNER:
			importSound("linescanned");
			importSound("linescannermove");
		case LOADTYPE_MINESWEEPER:
			importSound("explosion1");
			importSound("explosion2");
			importSound("explosion3");
			importSound("explosion4");
		case LOADTYPE_COLLAPSE:
			importSound("bombexplode");
			importSound("landing");
			importSound("rise");
			importSound("bonus");
			importSound("bigclear");
			importSound("normalclear");
			importSound("nolanding");
			importSound("noclear");
		default:
			break;
		}
	}
	
	private static void importSound(String soundName) {
		String skindir = null;
		int holderType = -1;
		
		if (ResourceHolder.imgNormalBlockList != null) holderType = HOLDER_SLICK;
		else if (ResourceHolderSwing.imgNormalBlockList != null) holderType = HOLDER_SWING;
		else if (ResourceHolderSDL.imgNormalBlockList != null) holderType = HOLDER_SDL;
		
		switch (holderType) {
		case HOLDER_SLICK:
			skindir = mu.nu.nullpo.gui.slick.NullpoMinoSlick.propConfig.getProperty("custom.skin.directory", "res");
			break;
		case HOLDER_SWING:
			skindir = mu.nu.nullpo.gui.swing.NullpoMinoSwing.propConfig.getProperty("custom.skin.directory", "res");
			break;
		case HOLDER_SDL:
			skindir = mu.nu.nullpo.gui.sdl.NullpoMinoSDL.propConfig.getProperty("custom.skin.directory", "res");
			break;
		}
		
		if (ResourceHolderSwing.soundManager != null) {
			ResourceHolderSwing.soundManager.load(soundName, skindir + "/se/zeroxfc/" + soundName + ".wav");
		} else if (ResourceHolder.soundManager != null) {
			ResourceHolder.soundManager.load(soundName, skindir + "/se/zeroxfc/" + soundName + ".wav");
		} else if (ResourceHolderSDL.soundManager != null) {
			ResourceHolderSDL.soundManager.load(soundName, skindir + "/se/zeroxfc/" + soundName + ".wav");
		}
	}

	public static void importSound(String filePath, String soundName) {
		if (ResourceHolderSwing.soundManager != null) {
			ResourceHolderSwing.soundManager.load(soundName, filePath);
		} else if (ResourceHolder.soundManager != null) {
			ResourceHolder.soundManager.load(soundName, filePath);
		} else if (ResourceHolderSDL.soundManager != null) {
			ResourceHolderSDL.soundManager.load(soundName, filePath);
		}
	}
}
