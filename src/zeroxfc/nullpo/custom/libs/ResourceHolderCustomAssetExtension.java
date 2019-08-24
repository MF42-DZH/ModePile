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
 *     - Only binaries of this library are allowed to be distributed unless at the
 *       permission of the Library Creator.
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

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.gui.sdl.*;
import mu.nu.nullpo.gui.slick.*;
import mu.nu.nullpo.gui.swing.*;
import org.newdawn.slick.Graphics;
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
	private static Logger log = Logger.getLogger(ResourceHolderCustomAssetExtension.class);
	
	/** Internal used ResourceHolder class. */
	private static final int HOLDER_SLICK = 0,
							HOLDER_SWING = 1,
							HOLDER_SDL = 2;

	private static int bgmPrevious = -1;
	
	private HashMap<String, org.newdawn.slick.Image> slickImages;  // Slick Images
	private HashMap<String, java.awt.Image> swingImages;           // Swing Images
	private HashMap<String, sdljava.video.SDLSurface> sdlImages;   // SDL Images

	private HashMap<String, org.newdawn.slick.Music> slickMusic;  // Slick Music
	private HashMap<String, sdljava.mixer.MixMusic> sdlMusic;     // SDL Music

	private Graphics2D localSwingGraphics;
	private sdljava.video.SDLSurface localSDLGraphics;
	
	private int holderType;
	
	public ResourceHolderCustomAssetExtension() {
		this(8);
	}

	public ResourceHolderCustomAssetExtension(int initialCapacity) {
		holderType = -1;
		if (ResourceHolder.imgNormalBlockList != null) holderType = HOLDER_SLICK;
		else if (ResourceHolderSwing.imgNormalBlockList != null) holderType = HOLDER_SWING;
		else if (ResourceHolderSDL.imgNormalBlockList != null) holderType = HOLDER_SDL;

		switch (holderType) {
			case HOLDER_SLICK:
				slickImages = new HashMap<>(initialCapacity);
				slickMusic = new HashMap<>(initialCapacity);
				break;
			case HOLDER_SWING:
				swingImages = new HashMap<>(initialCapacity);
				break;
			case HOLDER_SDL:
				sdlImages = new HashMap<>(initialCapacity);
				sdlMusic = new HashMap<>(initialCapacity);
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
	 * Copies an image from the HashMap key to another key.
	 * @param source Source key
	 * @param dest Destination key
	 */
	public void copyImage(String source, String dest) {
		switch (holderType) {
			case HOLDER_SLICK:
				slickImages.replace(dest, slickImages.get(source));
				break;
			case HOLDER_SWING:
				swingImages.replace(dest, swingImages.get(source));
				break;
			case HOLDER_SDL:
				sdlImages.replace(dest, sdlImages.get(source));
				break;
		}
	}

	/**
	 * Gets the pixel dimensions of the named image.
	 * @param name Image name in holder dictionary.
	 * @return int[] { width, height } (both in pixels).
	 */
	public int[] getImageDimensions(String name) {
		try {
			int[] dim = new int[2];
			switch (holderType) {
				case HOLDER_SLICK:
					dim[0] = slickImages.get(name).getWidth();
					dim[1] = slickImages.get(name).getHeight();
					return dim;
				case HOLDER_SWING:
					dim[0] = swingImages.get(name).getWidth(null);
					dim[1] = swingImages.get(name).getHeight(null);
					return dim;
				case HOLDER_SDL:
					dim[0] = sdlImages.get(name).getWidth();
					dim[1] = sdlImages.get(name).getHeight();
					return dim;
				default:
					return new int[] { 0, 0 };
			}
		} catch (Exception e) {
			return new int[] { 0, 0 };
		}
	}

	/**
	 * Puts image in the holder at name.
	 * @param name Image name
	 */
	public void putImageAt(Object image, String name) {
		if (image == null) return;
		try {
			switch (holderType) {
				case HOLDER_SLICK:
					slickImages.put(name, (org.newdawn.slick.Image)image);
					break;
				case HOLDER_SWING:
					swingImages.put(name, (java.awt.Image)image);
					break;
				case HOLDER_SDL:
					sdlImages.put(name, (SDLSurface) image);
					break;
				default:
					break;
			}
		} catch (Exception e) {
			log.error("Unable to insert image " + image.toString() + " at " + name);
		}
	}

	/**
	 * Gets object that is an image instance.
	 * <code>A CAST IS STRICTLY NECESARRY!</code>
	 * @param name Image name
	 * @return Image at name
	 */
	public Object getImageAt(String name) {
		try {
			switch (holderType) {
				case HOLDER_SLICK:
					return slickImages.get(name);
				case HOLDER_SWING:
					return swingImages.get(name);
				case HOLDER_SDL:
					return sdlImages.get(name);
				default:
					return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Replaces an image.
	 * @param name Key of image to replace
	 * @param image Image object to replace with.
	 */
	public void setImageAt(String name, Object image) {
		try {
			switch (holderType) {
				case HOLDER_SLICK:
					slickImages.replace(name, (org.newdawn.slick.Image)image);
					log.error("Image " + name + " replaced.");
					break;
				case HOLDER_SWING:
					swingImages.replace(name, (java.awt.Image)image);
					log.error("Image " + name + " replaced.");
					break;
				case HOLDER_SDL:
					sdlImages.replace(name, (sdljava.video.SDLSurface)image);
					log.error("Image " + name + " replaced.");
					break;
				default:
					break;
			}
		} catch (Exception e) {
			log.error("Image does not exist or invalid cast attempted.");
		}
	}

	/**
	 * Sets rotation centre for an image when using Slick renderer.
	 * @param name Image name
	 * @param x X-coordinate relative to image's top-left corner
	 * @param y Y-coordinate relative to image's top-left corner
	 */
	public void setRotationCentre(String name, float x, float y) {
		if (holderType == HOLDER_SLICK) {
			slickImages.get(name).setCenterOfRotation(x, y);
		}
	}

	/**
	 * Sets rotation for an image when using Slick renderer.
	 * @param name Image name
	 * @param a Angle, degrees.
	 */
	public void setRotation(String name, float a) {
		if (holderType == HOLDER_SLICK) {
			slickImages.get(name).setRotation(a);
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
	 * Draws image to game.
	 * @param engine GameEngine to draw with.
	 * @param name Identifier of image.
	 * @param x X position of top-left
	 * @param y Y position of top-left
	 * @param x2 X position of bottom-right
	 * @param y2 Y position of bottom-right
	 * @param srcX Source X position
	 * @param srcY Source Y position
	 * @param srcSizeX Source X size
	 * @param srcSizeY Source Y size
	 * @param red Red component
	 * @param green Green component
	 * @param blue Blue component
	 * @param alpha Alpha component
	 */
	public void drawImage(GameEngine engine, String name, int x, int y, int x2, int y2, int srcX, int srcY, int srcSizeX, int srcSizeY, int red, int green, int blue, int alpha) {
		if (x < x2 || y < y2) return;

		switch (holderType) {
			case HOLDER_SLICK:
				org.newdawn.slick.Image toDraw = slickImages.get(name);

				org.newdawn.slick.Color filter = new org.newdawn.slick.Color(red, green, blue, alpha);

				toDraw.draw(x, y, x2, y2, srcX, srcY, srcX + srcSizeX, srcY + srcSizeY, filter);

				break;
			case HOLDER_SWING:
				localSwingGraphics = getGraphicsSwing((RendererSwing)engine.owner.receiver);
				java.awt.Image toDrawSwing = swingImages.get(name);

				localSwingGraphics.setColor(new java.awt.Color(red, green, blue, alpha));
				localSwingGraphics.drawImage(toDrawSwing, x, y, x2, y2, srcX, srcY, srcX + srcSizeX, srcY + srcSizeY, null);
				localSwingGraphics.setColor(new java.awt.Color(255, 255, 255, 255));
				break;
			case HOLDER_SDL:
				localSDLGraphics = getGraphicsSDL((RendererSDL)engine.owner.receiver);
				sdljava.video.SDLSurface toDrawSDL = sdlImages.get(name);
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
	 * @param engine GameEngine to draw with.
	 * @param name Identifier of image.
	 * @param x X position
	 * @param y Y position
	 * @param sx X size
	 * @param sy Y size
	 * @param srcX Source X position
	 * @param srcY Source Y position
	 * @param srcSizeX Source X size
	 * @param srcSizeY Source Y size
	 * @param red Red component
	 * @param green Green component
	 * @param blue Blue component
	 * @param alpha Alpha component
	 * @param flag (silences the compiler)
	 */
	public void drawImage(GameEngine engine, String name, int x, int y, int sx, int sy, int srcX, int srcY, int srcSizeX, int srcSizeY, int red, int green, int blue, int alpha, Integer flag) {
		if (flag == null) flag = 0;
		if (sx <= 0 || sy <= 0) return;

		switch (holderType) {
			case HOLDER_SLICK:
				org.newdawn.slick.Image toDraw = slickImages.get(name);

				int fx = x + sx;
				int fy = y + sy;

				org.newdawn.slick.Color filter = new org.newdawn.slick.Color(red, green, blue, alpha);

				toDraw.draw(x, y, fx, fy, srcX, srcY, srcX + srcSizeX, srcY + srcSizeY, filter);

				break;
			case HOLDER_SWING:
				localSwingGraphics = getGraphicsSwing((RendererSwing)engine.owner.receiver);
				java.awt.Image toDrawSwing = swingImages.get(name);

				int fxSw = x + sx;
				int fySw = y + sy;

				localSwingGraphics.setColor(new java.awt.Color(red, green, blue, alpha));
				localSwingGraphics.drawImage(toDrawSwing, x, y, fxSw, fySw, srcX, srcY, srcX + srcSizeX, srcY + srcSizeY, null);
				localSwingGraphics.setColor(new java.awt.Color(255, 255, 255, 255));
				break;
			case HOLDER_SDL:
				localSDLGraphics = getGraphicsSDL((RendererSDL)engine.owner.receiver);
				sdljava.video.SDLSurface toDrawSDL = sdlImages.get(name);
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
	 * Starts the play of an internal BGM. Use the relative index of the added music. (0 = first added track.)
	 * There is a limitation that the song can't be paused.
	 * @param name Track name.
	 * @param noLoop Set true if it shouldn't loop.
	 */
	public void playCustomBGM(String name, boolean noLoop) {
		switch (holderType) {
		case HOLDER_SLICK:
			if(!NullpoMinoSlick.propConfig.getProperty("option.bgm", false)) return;

			stopCustomBGM();

			int bgmvolume = NullpoMinoSlick.propConfig.getProperty("option.bgmvolume", 128);
			NullpoMinoSlick.appGameContainer.setMusicVolume(bgmvolume / (float)128);

				try {
					if(noLoop)
						slickMusic.get(name).play();
					else
						slickMusic.get(name).play();
				} catch(Throwable e) {
					log.error("Failed to play music " + name, e);
				}

			return;
		case HOLDER_SWING:
			log.warn("BGM is not supported on Swing.");
			return;
		case HOLDER_SDL:
			if(!NullpoMinoSDL.propConfig.getProperty("option.bgm", false)) return;

			stopCustomBGM();

			try {
				if(noLoop)
					SDLMixer.playMusic(sdlMusic.get(name), 1);
				else
					SDLMixer.playMusic(sdlMusic.get(name), -1);

				SDLMixer.volumeMusic(NullpoMinoSDL.propConfig.getProperty("option.bgmvolume", 128));
			} catch (Exception e) {
				log.warn("BGM " + name + " start failed", e);
			}
		}
	}

	/**
	 * Remove internal custom BGM by name
	 * @param name BGM name
	 */
	public void removeCustomInternalBGM(String name) {
		switch (holderType) {
			case HOLDER_SLICK:
				slickMusic.remove(name);
				break;
			case HOLDER_SDL:
				sdlMusic.remove(name);
				break;
			default:
				break;
		}
	}
	
	/**
	 * Gets number of loaded, appended BGM files.
	 * @return Number of loaded, appended BGM files.
	 */
	public int getAmountLoadedBGM() {
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
	 * @return Number of loaded BGM files in holder.
	 */
	public int getAmountDiscreteLoadedBGM() {
		switch (holderType) {
			case HOLDER_SLICK:
				return slickMusic.size();
			case HOLDER_SDL:
				return sdlImages.size();
			default:
				return 0;
		}
	}

	/**
	 * Stops all custom BGM.
	 */
	public void stopCustomBGM() {
		switch (holderType) {
			case HOLDER_SLICK:
				if(!NullpoMinoSlick.propConfig.getProperty("option.bgm", false)) return;

				for (String s : slickMusic.keySet()) {
					slickMusic.get(s).pause();
					slickMusic.get(s).stop();
				}

				return;
			case HOLDER_SWING:
				log.warn("BGM is not supported on Swing.");
				return;
			case HOLDER_SDL:
				if(!NullpoMinoSDL.propConfig.getProperty("option.bgm", false)) return;

				try {
					SDLMixer.haltMusic();
				} catch (Exception e) {
					log.debug("BGM stop failed", e);
				}
				break;
		}
	}

	/**
	 * Stops all BGM.
	 * @param owner Current GameManager
	 */
	public void stopDefaultBGM(GameManager owner) {
		if (owner.bgmStatus.bgm != -1) bgmPrevious = owner.bgmStatus.bgm;
		owner.bgmStatus.bgm = -1;
	}

	/**
	 * Restarts previously playing default BGM.
	 * @param owner Current GameManager
	 */
	public void restartDefaultBGM(GameManager owner) {
		if (owner.bgmStatus.bgm == -1) {
			owner.bgmStatus.bgm = bgmPrevious;
			owner.bgmStatus.fadesw = false;
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
			return (Graphics2D)localField.get(renderer);
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
			return (Graphics)localField.get(renderer);
		} catch (Exception e) {
			log.error("Failed to extract graphics from Slick renderer.");
			return null;
		}
	}

	public static void setGraphicsSDL(RendererSDL renderer, SDLSurface grp) {
		Class<RendererSDL> local = RendererSDL.class;
		Field localField;
		try {
			localField = local.getDeclaredField("graphics");
			localField.setAccessible(true);
			localField.set(renderer, grp);
		} catch (Exception e) {
			log.error("Failed to extract graphics from SDL renderer.");
		}
	}

	public static void setGraphicsSwing(RendererSwing renderer, Graphics2D grp) {
		Class<RendererSwing> local = RendererSwing.class;
		Field localField;
		try {
			localField = local.getDeclaredField("graphics");
			localField.setAccessible(true);
			localField.set(renderer, grp);
		} catch (Exception e) {
			log.error("Failed to extract graphics from Swing renderer.");
		}
	}

	public static void setGraphicsSlick(RendererSlick renderer, Graphics grp) {
		Class<RendererSlick> local = RendererSlick.class;
		Field localField;
		try {
			localField = local.getDeclaredField("graphics");
			localField.setAccessible(true);
			localField.set(renderer, grp);
		} catch (Exception e) {
			log.error("Failed to extract graphics from Slick renderer.");
		}
	}
}
