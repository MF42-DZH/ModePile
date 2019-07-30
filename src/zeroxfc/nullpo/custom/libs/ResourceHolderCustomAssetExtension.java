package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.gui.sdl.*;
import mu.nu.nullpo.gui.slick.*;
import mu.nu.nullpo.gui.swing.*;
import sdljava.mixer.MixMusic;
import sdljava.mixer.SDLMixer;
// import sdljava.SDLException;
import sdljava.video.SDLRect;
import sdljava.video.SDLSurface;

import java.awt.Graphics2D;
import java.lang.reflect.Field;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.newdawn.slick.Music;

public class ResourceHolderCustomAssetExtension {
	static Logger log = Logger.getLogger(ResourceHolderCustomAssetExtension.class);
	
	/** Internal used ResourceHolder class. */
	private static final int HOLDER_SLICK = 0,
							HOLDER_SWING = 1,
							HOLDER_SDL = 2;
	
	private HashMap<String, org.newdawn.slick.Image> slickImages;  // Slick Images
	private HashMap<String, java.awt.Image> swingImages;           // Swing Images
	private HashMap<String, sdljava.video.SDLSurface> sdlImages;   // SDL Images
	
	private Graphics2D localSwingGraphics;
	private sdljava.video.SDLSurface localSDLGraphics;
	
	private int holderType;
	
	public ResourceHolderCustomAssetExtension(GameEngine engine) {
		holderType = -1;
		if (ResourceHolder.imgNormalBlockList != null) holderType = HOLDER_SLICK;
		else if (ResourceHolderSwing.imgNormalBlockList != null) holderType = HOLDER_SWING;
		else if (ResourceHolderSDL.imgNormalBlockList != null) holderType = HOLDER_SDL;
		
		switch (holderType) {
		case HOLDER_SLICK:
			slickImages = new HashMap<String, org.newdawn.slick.Image>(8);
			break;
		case HOLDER_SWING:
			swingImages = new HashMap<String, java.awt.Image>(8);
			break;
		case HOLDER_SDL:
			sdlImages = new HashMap<String, sdljava.video.SDLSurface>(8);
			break;
		}
	}
	
	/**
	 * Adds an image to the custom image library
	 * @param filePath Path of image file
	 * @param name Identifier name
	 */
	public void loadImage(String filePath, String name) {
		switch (holderType) {
		case HOLDER_SLICK:
			slickImages.put(name, ResourceHolder.loadImage(filePath));
			break;
		case HOLDER_SWING:
			swingImages.put(name, ResourceHolderSwing.loadImage(ResourceHolderSwing.getURL(filePath)));
			break;
		case HOLDER_SDL:
			sdlImages.put(name, ResourceHolderSDL.loadImage(filePath));
			break;
		}
	}
	
	/**
	 * Draws image to game.
	 * @param engine GameEngine to draw with.
	 * @param name Identifier of image.
	 * @param x X position
	 * @param y Y position
	 * @param srcX Source X position
	 * @param srcY Source Y position
	 * @param srcSizeX Source X size
	 * @param srcSizeY Source Y size
	 * @param red Red component
	 * @param green Green component
	 * @param blue Blue component
	 * @param alpha Alpha component
	 * @param scale Image scale
	 */
	public void drawImage(GameEngine engine, String name, int x, int y, int srcX, int srcY, int srcSizeX, int srcSizeY, int red, int green, int blue, int alpha, float scale) {
		switch (holderType) {
		case HOLDER_SLICK:
			org.newdawn.slick.Image toDraw = slickImages.get(name);
			
			int fx = x + (int)(srcSizeX * scale);
			int fy = y + (int)(srcSizeY * scale);
			
			org.newdawn.slick.Color filter = new org.newdawn.slick.Color(red, green, blue, alpha);
			
			toDraw.draw(x, y, fx, fy, srcX, srcY, srcX + srcSizeX, srcY + srcSizeY, filter);
			
			break;
		case HOLDER_SWING:
			localSwingGraphics = getGraphicsSwing((RendererSwing)engine.owner.receiver);
			java.awt.Image toDrawSwing = swingImages.get(name);
			
			int fxSw = x + (int)(srcSizeX * scale);
			int fySw = y + (int)(srcSizeY * scale);
			
			localSwingGraphics.setColor(new java.awt.Color(red, green, blue, alpha));
			localSwingGraphics.drawImage(toDrawSwing, x, y, fxSw, fySw, srcX, srcY, srcX + srcSizeX, srcY + srcSizeY, null);
			localSwingGraphics.setColor(new java.awt.Color(255, 255, 255, 255));
			break;
		case HOLDER_SDL:
			localSDLGraphics = getGraphicsSDL((RendererSDL)engine.owner.receiver);
			sdljava.video.SDLSurface toDrawSDL = sdlImages.get(name);
			int dx = (int)(srcSizeX * scale);
			int dy = (int)(srcSizeY * scale);
			try {
				toDrawSDL.blitSurface(new SDLRect(srcX, srcY, srcSizeX, srcSizeY), localSDLGraphics, new SDLRect(x, y, dx, dy));
			} catch (Exception e) {
				// DO NOTHING AT ALL.
			}
			break;
		}
	}

	/**
	 * Draws whole image to game with no tint.
	 * @param engine GameEngine to draw with.
	 * @param name Identifier of image.
	 * @param x X position
	 * @param y Y position
	 */
	public void drawImage(GameEngine engine, String name, int x, int y) {
		switch (holderType) {
			case HOLDER_SLICK:
				org.newdawn.slick.Image toDraw = slickImages.get(name);

				int fx = toDraw.getWidth();
				int fy = toDraw.getHeight();

				drawImage(engine, name, x, y, 0, 0, fx, fy, 255, 255, 255, 255, 1f);
				break;
			case HOLDER_SWING:
				localSwingGraphics = getGraphicsSwing((RendererSwing)engine.owner.receiver);
				java.awt.Image toDrawSwing = swingImages.get(name);

				int fxSw = toDrawSwing.getWidth(null) ;
				int fySw = toDrawSwing.getHeight(null);

				drawImage(engine, name, x, y, 0, 0, fxSw, fySw, 255, 255, 255, 255, 1f);
				break;
			case HOLDER_SDL:
				localSDLGraphics = getGraphicsSDL((RendererSDL)engine.owner.receiver);
				sdljava.video.SDLSurface toDrawSDL = sdlImages.get(name);

				int dx = toDrawSDL.getWidth();
				int dy = toDrawSDL.getHeight();

				drawImage(engine, name, x, y, 0, 0, dx, dy, 255, 255, 255, 255, 1f);
				break;
		}
	}

	/**
	 * Draws image to game with string colour definition.
	 * 8 character RRGGBBAA hex code or 6 character RRGGBB hex code.
	 * @param engine GameEngine to draw with.
	 * @param name Identifier of image.
	 * @param x X position
	 * @param y Y position
	 * @param srcX Source X position
	 * @param srcY Source Y position
	 * @param srcSizeX Source X size
	 * @param srcSizeY Source Y size
	 * @param color Hex. code string of colour.
	 * @param scale Image scale
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
		a = Integer.parseInt(alpha, 16);

		drawImage(engine, name, x, y, srcX, srcY, srcSizeX, srcSizeY, r, g, b, a, scale);
	}
	
	/**
	 * Appends a new BGM number into the BGM array. On Swing, this has no effect.
	 * @param filename File path of music file to import.
	 * @param showerr Show in log?
	 */
	public void loadNewBGMAppend(String filename, boolean noLoop, boolean showerr) {
		switch (holderType) {
		case HOLDER_SLICK:
			if(NullpoMinoSlick.propConfig.getProperty("option.bgm", false) == false) return;
			
			int no = ResourceHolder.bgm.length + 1;
			Music[] newArr = new Music[no];
			for (int i = 0; i < ResourceHolder.bgm.length; i++) {
				newArr[i] = ResourceHolder.bgm[i];
			}
			
			if(newArr[no - 1] == null) {
				if(showerr) log.info("Loading BGM at " + filename);

				try {
					// String filename = NullpoMinoSlick.propMusic.getProperty("music.filename." + no, null);
					if((filename == null) || (filename.length() < 1)) {
						if(showerr) log.info("BGM at " + filename + " not available");
						return;
					}
					
					NullpoMinoSlick.propMusic.setProperty("music.noloop." + no, noLoop);
					// boolean streaming = NullpoMinoSlick.propConfig.getProperty("option.bgmstreaming", true);

					newArr[no - 1] = new Music(filename, true);
					ResourceHolder.bgm = newArr.clone();

					if(!showerr) log.info("Loaded BGM at " + filename);
				} catch(Throwable e) {
					if(showerr)
						log.error("BGM at " + filename + " load failed", e);
					else
						log.warn("BGM at " + filename + " load failed");
				}
			}
			return;
		case HOLDER_SWING:
			log.warn("BGM is not supported on Swing.");
			return;
		case HOLDER_SDL:
			if(NullpoMinoSDL.propConfig.getProperty("option.bgm", false) == false) return;
			
			int no2 = ResourceHolderSDL.bgm.length + 1;
			MixMusic[] newArr2 = new MixMusic[no2];
			for (int i = 0; i < ResourceHolderSDL.bgm.length; i++) {
				newArr2[i] = ResourceHolderSDL.bgm[i];
			}
			
			if(newArr2[no2 - 1] == null) {
				if(showerr) {
					log.info("Loading BGM at " + filename);
				}

				try {
					// String filename = NullpoMinoSDL.propMusic.getProperty("music.filename." + no, null);
					if((filename == null) || (filename.length() < 1)) {
						if(showerr) log.info("BGM at " + filename + " not available");
						return;
					}
					
					NullpoMinoSDL.propMusic.setProperty("music.noloop." + no2, noLoop);
					newArr2[no2 - 1] = SDLMixer.loadMUS(filename);
					ResourceHolderSDL.bgm = newArr2.clone();
					
					if(!showerr) {
						log.info("Loaded BGM at " + filename);
					}
				} catch(Throwable e) {
					if(showerr) {
						log.warn("BGM at " + filename + " load failed", e);
					} else {
						log.warn("BGM at " + filename + " load failed");
					}
				}
			}
			return;
		}
	}
	
	/**
	 * Removes a loaded BGM file from the end of the BGM array. On Swing, this has no effect.
	 * @param showerr Show in log?
	 */
	public void removeBGMFromEnd(boolean showerr) {
		switch (holderType) {
		case HOLDER_SLICK:
			if(NullpoMinoSlick.propConfig.getProperty("option.bgm", false) == false) return;
			
			int no = ResourceHolder.bgm.length - 1;
			Music[] newBGM = new Music[no];
			
			for (int i = 0; i < newBGM.length; i++) {
				newBGM[i] = ResourceHolder.bgm[i];
			}
			
			ResourceHolder.bgm = newBGM.clone();
			if (showerr) log.info("Removed BGM at " + no);
			return;
		case HOLDER_SWING:
			log.warn("BGM is not supported on Swing.");
			return;
		case HOLDER_SDL:
			if(NullpoMinoSDL.propConfig.getProperty("option.bgm", false) == false) return;
			
			int no2 = ResourceHolderSDL.bgm.length - 1;
			MixMusic[] newBGM2 = new MixMusic[no2];
			
			for (int i = 0; i < newBGM2.length; i++) {
				newBGM2[i] = ResourceHolderSDL.bgm[i];
			}
			
			ResourceHolderSDL.bgm = newBGM2.clone();
			if (showerr) log.info("Removed BGM at " + no2);
			return;
		}
	}
	
	/**
	 * Starts the play of a BGM. Use the relative index of the added music. (0 = first added track.)
	 * There is a limitation that the song can't be paused.
	 * @param no Custom track number.
	 * @param noLoop Set true if it shouldn't loop.
	 */
	public void playCustomBGM(int no, boolean noLoop) {
		int number = no + BGMStatus.BGM_COUNT;
		switch (holderType) {
		case HOLDER_SLICK:
			if(NullpoMinoSlick.propConfig.getProperty("option.bgm", false) == false) return;

			stopCustomBGM();

			int bgmvolume = NullpoMinoSlick.propConfig.getProperty("option.bgmvolume", 128);
			NullpoMinoSlick.appGameContainer.setMusicVolume(bgmvolume / (float)128);

			if(number >= 0) {
				if(ResourceHolder.bgm[number] != null) {
					try {
						if(noLoop)
							ResourceHolder.bgm[number].play();
						else
							ResourceHolder.bgm[number].loop();
					} catch(Throwable e) {
						log.error("Failed to play music " + number, e);
					}
				}

				ResourceHolder.bgmPlaying = number;
			} else {
				ResourceHolder.bgmPlaying = -1;
			}
			return;
		case HOLDER_SWING:
			log.warn("BGM is not supported on Swing.");
			return;
		case HOLDER_SDL:
			if(NullpoMinoSDL.propConfig.getProperty("option.bgm", false) == false) return;

			stopCustomBGM();
			ResourceHolderSDL.bgmPlaying = number;

			if(number < 0) return;

			if(ResourceHolderSDL.bgm[number] != null) {
				try {
					if(noLoop)
						SDLMixer.playMusic(ResourceHolderSDL.bgm[number], 1);
					else
						SDLMixer.playMusic(ResourceHolderSDL.bgm[number], -1);

					SDLMixer.volumeMusic(NullpoMinoSDL.propConfig.getProperty("option.bgmvolume", 128));
				} catch (Exception e) {
					log.warn("BGM " + number + " start failed", e);
				}
			}
			return;
		}
	}
	
	/**
	 * Gets number of loaded BGM files.
	 * @return Number of loaded BGM files.
	 */
	public int getAmountLoadedBGM() {
		switch (holderType) {
		case HOLDER_SLICK:
			return ResourceHolder.bgm.length - BGMStatus.BGM_COUNT;
		case HOLDER_SWING:
			return 0;
		case HOLDER_SDL:
			return ResourceHolderSDL.bgm.length - BGMStatus.BGM_COUNT;
		default:
			return 0;
		}
	}
	
	/**
	 * Stops all BGM.
	 */
	public void stopCustomBGM() {
		switch (holderType) {
		case HOLDER_SLICK:
			if(NullpoMinoSlick.propConfig.getProperty("option.bgm", false) == false) return;
			
			for(int i = 0; i < ResourceHolder.bgm.length; i++) {
				if(ResourceHolder.bgm[i] != null) {
					ResourceHolder.bgm[i].pause();
					ResourceHolder.bgm[i].stop();
				}
			}
			
			return;
		case HOLDER_SWING:
			log.warn("BGM is not supported on Swing.");
			return;
		case HOLDER_SDL:
			if(NullpoMinoSDL.propConfig.getProperty("option.bgm", false) == false) return;
			
			try {
				if(ResourceHolderSDL.bgmIsPlaying()) {
					SDLMixer.haltMusic();
					ResourceHolderSDL.bgmPlaying = -1;
				}
			} catch (Exception e) {
				log.debug("BGM stop failed", e);
			}
			return;
		}
	}
	
	public static SDLSurface getGraphicsSDL(RendererSDL renderer) {
		Class<RendererSDL> local = RendererSDL.class;
		Field localField;
		try {
			localField = local.getDeclaredField("graphics");
			localField.setAccessible(true);
			return (SDLSurface)localField.get(renderer);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Graphics2D getGraphicsSwing(RendererSwing renderer) {
		Class<RendererSwing> local = RendererSwing.class;
		Field localField;
		try {
			localField = local.getDeclaredField("graphics");
			localField.setAccessible(true);
			return (Graphics2D)localField.get(renderer);
		} catch (Exception e) {
			return null;
		}
	}
}
