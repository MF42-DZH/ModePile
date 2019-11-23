package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.gui.sdl.NullpoMinoSDL;
import mu.nu.nullpo.gui.sdl.ResourceHolderSDL;
import mu.nu.nullpo.gui.sdl.SoundManagerSDL;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.gui.slick.ResourceHolder;
import mu.nu.nullpo.gui.slick.SoundManager;
import mu.nu.nullpo.gui.swing.NullpoMinoSwing;
import mu.nu.nullpo.gui.swing.ResourceHolderSwing;
import mu.nu.nullpo.gui.swing.WaveEngine;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;

import org.apache.log4j.Logger;
import org.newdawn.slick.Sound;

import sdljava.mixer.MixChunk;

import zeroxfc.nullpo.custom.libs.*;

import javax.sound.sampled.Clip;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.lang.reflect.*;
import java.lang.StringBuilder;

public class MarathonTwo extends MarathonModeBase {
	// region Imported Fields
	/** Current version */
	private static final int CURRENT_VERSION = 2;

	/** Fall velocity table (numerators) */
	private static final int[] tableGravity = { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 465, 731, 1280, 1707,  -1,  -1,  -1};

	/** Fall velocity table (denominators) */
	private static final int[] tableDenominator = {63, 50, 39, 30, 22, 16, 12,  8,  6,  4,  3,  2,  1, 256, 256,  256,  256, 256, 256, 256};

	/** Line counts when BGM changes occur */
	private static final int[] tableBGMChange = {50, 100, 150, 200, -1};

	/** Line counts when game ending occurs */
	private static final int[] tableGameClearLines = {150, 200, -1};

	/** Number of entries in rankings */
	private static final int RANKING_MAX = 10;

	/** Number of ranking types */
	private static final int RANKING_TYPE = 3;

	/** Number of game types */
	private static final int GAMETYPE_MAX = 3;

	/** Most recent scoring event type constants */
	private static final int EVENT_NONE = 0,
			EVENT_SINGLE = 1,
			EVENT_DOUBLE = 2,
			EVENT_TRIPLE = 3,
			EVENT_FOUR = 4,
			EVENT_TSPIN_ZERO_MINI = 5,
			EVENT_TSPIN_ZERO = 6,
			EVENT_TSPIN_SINGLE_MINI = 7,
			EVENT_TSPIN_SINGLE = 8,
			EVENT_TSPIN_DOUBLE_MINI = 9,
			EVENT_TSPIN_DOUBLE = 10,
			EVENT_TSPIN_TRIPLE = 11,
			EVENT_TSPIN_EZ = 12;

	/** Most recent increase in score */
	private int lastscore;

	/** Time to display the most recent increase in score */
	private int scgettime;

	/** Most recent scoring event type */
	private int lastevent;

	/** True if most recent scoring event is a B2B */
	private boolean lastb2b;

	/** Combo count for most recent scoring event */
	private int lastcombo;

	/** Piece ID for most recent scoring event */
	private int lastpiece;

	/** Current BGM */
	private int bgmlv;

	/** Level at start time */
	private int startlevel;

	/** Flag for types of T-Spins allowed (0=none, 1=normal, 2=all spin) */
	private int tspinEnableType;

	/** Old flag for allowing T-Spins */
	private boolean enableTSpin;

	/** Flag for enabling wallkick T-Spins */
	private boolean enableTSpinKick;

	/** Spin check type (4Point or Immobile) */
	private int spinCheckType;

	/** Immobile EZ spin */
	private boolean tspinEnableEZ;

	/** Flag for enabling B2B */
	private boolean enableB2B;

	/** Flag for enabling combos */
	private boolean enableCombo;

	/** Game type */
	private int goaltype;

	/** Big */
	private boolean big;

	/** Version */
	private int version;

	/** Current round's ranking rank */
	private int rankingRank;

	/** Rankings' scores */
	private int[][] rankingScore;

	/** Rankings' line counts */
	private int[][] rankingLines;

	/** Rankings' times */
	private int[][] rankingTime;
	// endregion Imported Fields

	// region Static Final Fields
	private static Logger log = Logger.getLogger(MarathonTwo.class);

	private static final int HOLDER_SLICK = 0,
	                         HOLDER_SWING = 1,
	                         HOLDER_SDL = 2;

	private static final double FRAME_COLOUR_FLUCTUATION_CHANCE = (1d / 160d);

	private static final String[] POSSIBLE_NAMES = {
			"USELESS",
			"MATRIX GLITCH",
			"FAILURE",
			"NOT MARATHON",
			"BEHIND YOUR BACK",
			"WATCH SURROUNDINGS",
			"NIGHTMARE",
			"IMAGINATION",
			"DESPERATION",
			"DESTRUCTION",
			"SOCIETAL SANITY",
			"ACCOMPLISHMENT-LESS",
			"S3 PROGRAM",
			"THE EDGE",
			"FUTURE LOOP FOUNDATION",
			"SHADOWS",
			"MIND CONTROL"
	};

	private static final String[] POSSIBLE_SUBTEXT = {
			"DANGEROUS",
			"NEVERENDING",
			"ABYSSAL",
			"HOPELESS",
			"EMPTY",
			"BORING",
			"TERRIBLE",
			"IDIOTIC"
	};

	private static final double CHAR_SCRAMBLE_CHANCE = (1d / 5d);
	private static final String characters = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopq";

	private static final double BASE_EFFECT_PROC_CHANCE = (1d / 2400d);
	private static final int SPOOKY_MAX = 200;

	private static final int[] EFFECT_WEIGHTS = {
			12, 8, 1, 12, 20, 8, 20, 12, 2, 6, 10
	};

	private static final int EFFECT_NONE = 0,
			                 EFFECT_FAKE_HOVER_BLOCK = 1,
			                 EFFECT_HARDEN_ALL = 2,
			                 EFFECT_BLACKOUT = 3,
			                 EFFECT_BONE_NEXT = 4,
			                 EFFECT_SCRAMBLE_COLOUR = 5,
			                 EFFECT_RANDOM_SOUND_EFFECT = 6,
			                 EFFECT_GLITCH_FIELD = 7,
			                 EFFECT_SHOTGUN = 8,
			                 EFFECT_REAL_HOVER_BLOCK = 9,
			                 EFFECT_SKIN_CHANGE = 10;

	// endregion Static Final Fields

	// region Private Fields
	private GameManager owner;
	private EventReceiver receiver;
	private Random effectRandomiser, renderRandomiser;
	private int spookyValue, lastEffect, holderType;
	private ArrayList<String> soundList;
	private int glitchTimer, blackoutTimer;
	private double titleCoefficient, subtextCoefficient, frameCoefficient;
	private String currentTitle, currentSubtext;
	private WeightedRandomiser effectTypeRandomiser;
	private boolean initialBGState;
	private ArrayList<int[]> fakeHoverBlocks, gTextCoords;
	private ArrayList<String> gTextChars;
	private int bTextX, bTextY;
	private int prevScore;
	private int rainbowPhase;

	// PROFILE
	private ProfileProperties playerProperties;
	private boolean showPlayerStats;
	private static final int headerColour = EventReceiver.COLOR_DARKBLUE;
	private int rankingRankPlayer;
	private int[][] rankingScorePlayer;
	private int[][] rankingTimePlayer;
	private int[][] rankingLinesPlayer;
	private String PLAYER_NAME;

	/** Field scattering effect */
	private FieldScatter fs = null;

	/** The good hard drop effect */
	private ArrayList<int[]> pCoordList;
	private Piece cPiece;

	// endregion Private Fields

	/** Mode Name */
	@Override
	public String getName() {
		return "MARATHON II";
	}

	/*
	 * Initialization
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		receiver = engine.owner.receiver;
		lastscore = 0;
		scgettime = 0;
		lastevent = EVENT_NONE;
		lastb2b = false;
		lastcombo = 0;
		lastpiece = 0;
		bgmlv = 0;
		prevScore = 0;

		rankingRank = -1;
		rankingScore = new int[RANKING_TYPE][RANKING_MAX];
		rankingLines = new int[RANKING_TYPE][RANKING_MAX];
		rankingTime = new int[RANKING_TYPE][RANKING_MAX];

		if (playerProperties == null) {
			playerProperties = new ProfileProperties(headerColour);

			showPlayerStats = false;
		}

		rankingRankPlayer = -1;
		rankingScorePlayer = new int[RANKING_TYPE][RANKING_MAX];
		rankingLinesPlayer = new int[RANKING_TYPE][RANKING_MAX];
		rankingTimePlayer = new int[RANKING_TYPE][RANKING_MAX];

		pCoordList = new ArrayList<>();
		cPiece = null;

		spookyValue = 0;
		lastEffect = EFFECT_NONE;
		blackoutTimer = 0;
		glitchTimer = 0;
		frameCoefficient = 1;
		titleCoefficient = 1;
		subtextCoefficient = 1;
		fakeHoverBlocks = new ArrayList<>();
		gTextCoords = new ArrayList<>();
		gTextChars = new ArrayList<>();
		rainbowPhase = 0;

		currentTitle = "";
		currentSubtext = "";

		SoundLoader.loadSoundset(SoundLoader.LOADTYPE_FIREWORKS);
		SoundLoader.loadSoundset(SoundLoader.LOADTYPE_SCANNER);
		SoundLoader.loadSoundset(SoundLoader.LOADTYPE_MINESWEEPER);
		SoundLoader.loadSoundset(SoundLoader.LOADTYPE_COLLAPSE);
		SoundLoader.importSound("res/se/zeroxfc/timereduce.wav", "timereduce");

		// Reflection is an unholy magic. It's powerful alright, but very unsafe.
		// region SOUND NAME EXTRACTION
		holderType = -1;
		String mainClass = ResourceHolderCustomAssetExtension.getMainClassName();

		if (mainClass.contains("Slick")) holderType = HOLDER_SLICK;
		else if (mainClass.contains("Swing")) holderType = HOLDER_SWING;
		else if (mainClass.contains("SDL")) holderType = HOLDER_SDL;

		soundList = new ArrayList<>();

		/*
		 * XXX: welp, i guess it's time to use reflection again... apologies in advance for this atrocity.
		 */
		Field localField;
		switch (holderType) {
			case HOLDER_SLICK:
				Class<SoundManager> slickSound = SoundManager.class;
				try {
					localField = slickSound.getDeclaredField("clipMap");
					localField.setAccessible(true);

					/*
					 * This should not return anything other than HashMap<String, Sound>,
					 * as verified in the source code (see SoundManager.java).
					 *
					 * Use @SuppressWarnings("unchecked").
					 */
					HashMap<String, Sound> slickSE = (HashMap<String, Sound>)localField.get(ResourceHolder.soundManager);

					soundList.addAll(slickSE.keySet());
					log.info("Sound name loading from Slick soundManager successful.");
				} catch (Exception e) {
					log.error("Sound name loading from Slick soundManager failed.");
				}

				break;
			case HOLDER_SWING:
				Class<WaveEngine> swingSound = WaveEngine.class;
				try {
					localField = swingSound.getDeclaredField("clipMap");
					localField.setAccessible(true);

					/*
					 * This should not return anything other than HashMap<String, Clip>,
					 * as verified in the source code (see WaveEngine.java).
					 *
					 * Use @SuppressWarnings("unchecked").
					 */
					HashMap<String, Clip> swingSE = (HashMap<String, Clip>)localField.get(ResourceHolderSwing.soundManager);

					soundList.addAll(swingSE.keySet());
					log.info("Sound name loading from Swing soundManager successful.");
				} catch (Exception e) {
					log.error("Sound name loading from Swing soundManager failed.");
				}

				break;
			case HOLDER_SDL:
				Class<SoundManagerSDL> sdlSound = SoundManagerSDL.class;
				try {
					localField = sdlSound.getDeclaredField("clipMap");
					localField.setAccessible(true);

					/*
					 * This should not return anything other than HashMap<String, MixChunk>,
					 * as verified in the source code (see SoundManagerSDL.java).
					 *
					 * Use @SuppressWarnings("unchecked").
					 */
					HashMap<String, MixChunk> sdlSE = (HashMap<String, MixChunk>)localField.get(ResourceHolderSDL.soundManager);

					soundList.addAll(sdlSE.keySet());
					log.info("Sound name loading from SDL soundManager successful.");
				} catch (Exception e) {
					log.error("Sound name loading from SDL soundManager failed.");
				}

				break;
		}
		// endregion SOUND NAME EXTRACTION

		netPlayerInit(engine, playerID);

		if(!owner.replayMode) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

			if (playerProperties.isLoggedIn()) {
				loadSettingPlayer(playerProperties);
				loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
			}

			getInitialBGState();
			version = CURRENT_VERSION;

			PLAYER_NAME = "";
		} else {
			loadSetting(owner.replayProp);
			if((version == 0) && (owner.replayProp.getProperty("marathon2.endless", false))) goaltype = 2;

			PLAYER_NAME = owner.replayProp.getProperty("marathon2.playerName", "");

			// NET: Load name
			netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}

		engine.owner.backgroundStatus.bg = startlevel;
		engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
	}

	private void bgmPause() {
		switch (holderType) {
			case HOLDER_SLICK:
				if (ResourceHolder.bgmIsPlaying()) ResourceHolder.bgmPause();
				break;
			case HOLDER_SDL:
				if (ResourceHolderSDL.bgmIsPlaying()) ResourceHolderSDL.bgmPause();
				break;
			default:
				break;
		}
	}

	private void bgmResume() {
		switch (holderType) {
			case HOLDER_SLICK:
				if (!ResourceHolder.bgmIsPlaying()) ResourceHolder.bgmResume();
				break;
			case HOLDER_SDL:
				if (!ResourceHolderSDL.bgmIsPlaying())ResourceHolderSDL.bgmResume();
				break;
			default:
				break;
		}
	}

	private void getInitialBGState() {
		switch (holderType) {
			case HOLDER_SLICK:
				initialBGState = NullpoMinoSlick.propConfig.getProperty("option.showbg", true);
				break;
			case HOLDER_SWING:
				initialBGState = NullpoMinoSwing.propConfig.getProperty("option.showbg", true);
				break;
			case HOLDER_SDL:
				initialBGState = NullpoMinoSDL.propConfig.getProperty("option.showbg", true);
				break;
			default:
				break;
		}
	}

	private void setBGState(boolean bool) {
		if (initialBGState) {
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
	}

	/**
	 * Gets a random sound name from the current <code>soundManager</code>.
	 * @return A random sound name or an empty string if <code>reflection</code> fails to get the name list.
	 */
	private String randomSound() {
		if (soundList.size() > 0 && effectRandomiser != null) return soundList.get(effectRandomiser.nextInt(soundList.size()));
		else return "";
	}

	/**
	 * Generates a scrambled string from a base string.
	 * @param baseStringArray Array to select a base string from.
	 * @return Scrambled string.
	 */
	private String randomString(String[] baseStringArray) {
		StringBuilder sb = new StringBuilder(baseStringArray[effectRandomiser.nextInt(baseStringArray.length)]);

		if (spookyValue >= (SPOOKY_MAX / 2)) {
			for (int i = 0; i < sb.length(); i++) {
				double coefficient = effectRandomiser.nextDouble();
				if (coefficient < (CHAR_SCRAMBLE_CHANCE * (spookyValue / 50d))) {
					sb.setCharAt(i, characters.charAt(effectRandomiser.nextInt(characters.length())));
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Set the gravity rate
	 * @param engine GameEngine
	 */
	public void setSpeed(GameEngine engine) {
		int lv = engine.statistics.level;

		if(lv < 0) lv = 0;
		if(lv >= tableGravity.length) lv = tableGravity.length - 1;

		engine.speed.gravity = tableGravity[lv];
		engine.speed.denominator = tableDenominator[lv];
	}

	/*
	 * Called at settings screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		setBGState(initialBGState);
		effectRandomiser = null;
		if (renderRandomiser == null) renderRandomiser = new Random(0);
		engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
		frameCoefficient = 1;
		titleCoefficient = 1;
		subtextCoefficient = 1;

		// NET: Net Ranking
		if(netIsNetRankingDisplayMode) {
			netOnUpdateNetPlayRanking(engine, goaltype);
		}
		// Menu
		else if(!engine.owner.replayMode) {
			// Configuration changes
			int change = updateCursor(engine, 8, playerID);

			if(change != 0) {
				engine.playSE("change");

				switch(engine.statc[2]) {
					case 0:
						startlevel += change;
						if(tableGameClearLines[goaltype] >= 0) {
							if(startlevel < 0) startlevel = (tableGameClearLines[goaltype] - 1) / 10;
							if(startlevel > (tableGameClearLines[goaltype] - 1) / 10) startlevel = 0;
						} else {
							if(startlevel < 0) startlevel = 19;
							if(startlevel > 19) startlevel = 0;
						}
						engine.owner.backgroundStatus.bg = startlevel;
						break;
					case 1:
						//enableTSpin = !enableTSpin;
						tspinEnableType += change;
						if(tspinEnableType < 0) tspinEnableType = 2;
						if(tspinEnableType > 2) tspinEnableType = 0;
						break;
					case 2:
						enableTSpinKick = !enableTSpinKick;
						break;
					case 3:
						spinCheckType += change;
						if(spinCheckType < 0) spinCheckType = 1;
						if(spinCheckType > 1) spinCheckType = 0;
						break;
					case 4:
						tspinEnableEZ = !tspinEnableEZ;
						break;
					case 5:
						enableB2B = !enableB2B;
						break;
					case 6:
						enableCombo = !enableCombo;
						break;
					case 7:
						goaltype += change;
						if(goaltype < 0) goaltype = GAMETYPE_MAX - 1;
						if(goaltype > GAMETYPE_MAX - 1) goaltype = 0;

						if((startlevel > (tableGameClearLines[goaltype] - 1) / 10) && (tableGameClearLines[goaltype] >= 0)) {
							startlevel = (tableGameClearLines[goaltype] - 1) / 10;
							engine.owner.backgroundStatus.bg = startlevel;
						}
						break;
					case 8:
						big = !big;
						break;
				}

				// NET: Signal options change
				if(netIsNetPlay && (netNumSpectators > 0)) {
					netSendOptions(engine);
				}
			}

			engine.owner.backgroundStatus.bg = startlevel;

			// Confirm
			if(engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
				engine.playSE("decide");
				if (playerProperties.isLoggedIn()) {
					saveSettingPlayer(playerProperties);
					playerProperties.saveProfileConfig();
				} else {
					saveSetting(owner.modeConfig);
					receiver.saveModeConfig(owner.modeConfig);
				}

				// NET: Signal start of the game
				if(netIsNetPlay) netLobby.netPlayerClient.send("start1p\n");

				return false;
			}

			// Cancel
			if(engine.ctrl.isPush(Controller.BUTTON_B) && !netIsNetPlay) {
				engine.quitflag = true;
				playerProperties = new ProfileProperties(headerColour);
			}

			// New acc
			if(engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null && !netIsNetPlay) {
				playerProperties = new ProfileProperties(headerColour);
				engine.playSE("decide");

				engine.stat = GameEngine.STAT_CUSTOM;
				engine.resetStatc();
				return true;
			}

			// NET: Netplay Ranking
			if(engine.ctrl.isPush(Controller.BUTTON_D) && netIsNetPlay && startlevel == 0 && !big &&
					engine.ai == null) {
				netEnterNetPlayRankingScreen(engine, playerID, goaltype);
			}

			engine.statc[3]++;
		}
		// Replay
		else {
			engine.statc[3]++;
			engine.statc[2] = -1;

			return engine.statc[3] < 60;
		}

		return true;
	}

	/*
	 * Render the settings screen
	 */
	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		if(netIsNetRankingDisplayMode) {
			// NET: Netplay Ranking
			netOnRenderNetPlayRanking(engine, playerID, receiver);
		} else {
			String strTSpinEnable = "";
			if(version >= 2) {
				if(tspinEnableType == 0) strTSpinEnable = "OFF";
				if(tspinEnableType == 1) strTSpinEnable = "T-ONLY";
				if(tspinEnableType == 2) strTSpinEnable = "ALL";
			} else {
				strTSpinEnable = GeneralUtil.getONorOFF(enableTSpin);
			}
			drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
					"LEVEL", String.valueOf(startlevel + 1),
					"SPIN BONUS", strTSpinEnable,
					"EZ SPIN", GeneralUtil.getONorOFF(enableTSpinKick),
					"SPIN TYPE", (spinCheckType == 0) ? "4POINT" : "IMMOBILE",
					"EZIMMOBILE", GeneralUtil.getONorOFF(tspinEnableEZ),
					"B2B", GeneralUtil.getONorOFF(enableB2B),
					"COMBO",  GeneralUtil.getONorOFF(enableCombo),
					"GOAL",  (goaltype == 2) ? "ENDLESS" : tableGameClearLines[goaltype] + " LINES",
					"BIG", GeneralUtil.getONorOFF(big));
		}
	}

	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		if (engine.statc[0] == 0) {
			effectRandomiser = new Random(engine.randSeed);
			effectTypeRandomiser = new WeightedRandomiser(EFFECT_WEIGHTS, engine.randSeed + 1);
			renderRandomiser = new Random(engine.randSeed + 2);

			fakeHoverBlocks.clear();
			gTextCoords.clear();
			gTextChars.clear();
			prevScore = 0;

			spookyValue = 0;
			lastEffect = EFFECT_NONE;
			blackoutTimer = 0;
			glitchTimer = 0;
			frameCoefficient = 1;
			titleCoefficient = 1;
			subtextCoefficient = 1;
			rainbowPhase = 0;
		}
		return false;
	}

	/*
	 * Called for initialization during "Ready" screen
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.statistics.level = startlevel;
		engine.statistics.levelDispAdd = 1;
		engine.b2bEnable = enableB2B;
		if(enableCombo) {
			engine.comboType = GameEngine.COMBO_TYPE_NORMAL;
		} else {
			engine.comboType = GameEngine.COMBO_TYPE_DISABLE;
		}
		engine.big = big;

		if(version >= 2) {
			engine.tspinAllowKick = enableTSpinKick;
			if(tspinEnableType == 0) {
				engine.tspinEnable = false;
			} else if(tspinEnableType == 1) {
				engine.tspinEnable = true;
			} else {
				engine.tspinEnable = true;
				engine.useAllSpinBonus = true;
			}
		} else {
			engine.tspinEnable = enableTSpin;
		}

		engine.spinCheckType = spinCheckType;
		engine.tspinEnableEZ = tspinEnableEZ;

		setSpeed(engine);

		if(netIsWatch) {
			owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
		}
	}

	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		if (engine.statc[0] == 0) {
			if (engine.statistics.lines < 100) {
				for (int y = engine.field.getHiddenHeight() * -1; y < engine.field.getHeight(); y++) {
					for (int x = 0; x < engine.field.getWidth(); x++) {
						Block blk = engine.field.getBlock(x, y);
						if (blk != null) {
							if (blk.color > Block.BLOCK_COLOR_NONE) {
								blk.secondaryColor = 0;
								blk.hard = 0;
							}
						}
					}
				}

				fs = new FieldScatter(receiver, engine, playerID);
			}
		}

		return false;
	}

	@Override
	public void renderGameOver(GameEngine engine, int playerID) {
		if (engine.statistics.lines >= 100 && engine.statistics.lines < tableGameClearLines[goaltype]) {
			for (int y = 0; y < 480 / 32; y++) {
				for (int x = 0; x < 640 / 32; x++) {
					receiver.drawSingleBlock(engine, playerID,
							(32 * x), (32 * y),Block.BLOCK_COLOR_GRAY,
							0,false, 1f,1f, 2f);
				}
			}
			for (int i = 0; i < 480 / 16; i++) {
				StringBuilder sb = new StringBuilder();
				long seed = (System.nanoTime() + i) * ((long)engine.statistics.time - i);
				sb.append(GameTextUtilities.randomString(640 / 16, new Random(seed))).append("\n");
				GameTextUtilities.drawRandomRainbowDirectString(receiver, engine, playerID, 0, i * 16, sb.toString(), new Random(seed + (i * i)), 1f);
			}
		}
	}

	@Override
	public void renderFirst(GameEngine engine, int playerID) {
		if (blackoutTimer > 0) {
			for (int y = 0; y < 480 / 32; y++) {
				for (int x = 0; x < 640 / 32; x++) {
					receiver.drawSingleBlock(engine, playerID,
							(32 * x), (32 * y),Block.BLOCK_COLOR_GRAY,
							0,false, 1f,1f, 2f);
				}
			}
		}
	}

	@Override
	public void onFirst(GameEngine engine, int playerID) {
		pCoordList.clear();
		cPiece = null;
	}

	/*
	 * Hard drop
	 */
	@Override
	public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
		engine.statistics.scoreFromHardDrop += fall * 2;
		engine.statistics.score += fall * 2;

		if (blackoutTimer == 0) {
			int baseX = (16 * engine.nowPieceX) + 4 + receiver.getFieldDisplayPositionX(engine, playerID);
			int baseY = (16 * engine.nowPieceY) + 52 + receiver.getFieldDisplayPositionY(engine, playerID);
			cPiece = new Piece(engine.nowPieceObject);
			for (int i = 1; i <= fall; i++) {
				pCoordList.add(
						new int[] { engine.nowPieceX, engine.nowPieceY - i }
				);
			}
			for (int i = 0; i < cPiece.getMaxBlock(); i++) {
				if (!cPiece.big) {
					int x2 = baseX + (cPiece.dataX[cPiece.direction][i] * 16);
					int y2 = baseY + (cPiece.dataY[cPiece.direction][i] * 16);

					RendererExtension.addBlockBreakEffect(receiver, x2, y2, cPiece.block[i]);
				} else {
					int x2 = baseX + (cPiece.dataX[cPiece.direction][i] * 32);
					int y2 = baseY + (cPiece.dataY[cPiece.direction][i] * 32);

					RendererExtension.addBlockBreakEffect(receiver, x2, y2, cPiece.block[i]);
					RendererExtension.addBlockBreakEffect(receiver, x2+16, y2, cPiece.block[i]);
					RendererExtension.addBlockBreakEffect(receiver, x2, y2+16, cPiece.block[i]);
					RendererExtension.addBlockBreakEffect(receiver, x2+16, y2+16, cPiece.block[i]);
				}
			}
		}
	}

	/*
	 * Render score
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;

		if (engine.stat != GameEngine.STAT_SETTING && engine.stat != GameEngine.STAT_RESULT && spookyValue >= 50) {
			if (titleCoefficient < FRAME_COLOUR_FLUCTUATION_CHANCE * Math.pow(spookyValue / 50d, 3.5)) receiver.drawScoreFont(engine, playerID, 0, 0, currentTitle, EventReceiver.COLOR_RED);
			else receiver.drawScoreFont(engine, playerID, 0, 0, glitchTimer > 0 ? currentTitle : getName(), EventReceiver.COLOR_GREEN);

			if(tableGameClearLines[goaltype] == -1) {
				if (subtextCoefficient < FRAME_COLOUR_FLUCTUATION_CHANCE * Math.pow(spookyValue / 50d, 3.5)) receiver.drawScoreFont(engine, playerID, 0, 1, "(" + currentSubtext + " GAME)", EventReceiver.COLOR_RED);
				else receiver.drawScoreFont(engine, playerID, 0, 1, "(ENDLESS GAME)", EventReceiver.COLOR_GREEN);
			} else {
				if (subtextCoefficient < FRAME_COLOUR_FLUCTUATION_CHANCE * Math.pow(spookyValue / 50d, 3.5)) receiver.drawScoreFont(engine, playerID, 0, 1, "(" + currentSubtext + " GAME)", EventReceiver.COLOR_RED);
				else receiver.drawScoreFont(engine, playerID, 0, 1, "(" + (glitchTimer > 0 ? currentSubtext : tableGameClearLines[goaltype]) + " LINES GAME)", EventReceiver.COLOR_GREEN);
			}
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 0, (titleCoefficient < (1d / 160d) && (engine.stat != GameEngine.STAT_SETTING && engine.stat != GameEngine.STAT_RESULT))  ? "MARATHON 2" : getName(), EventReceiver.COLOR_GREEN);
			if(tableGameClearLines[goaltype] == -1) {
				receiver.drawScoreFont(engine, playerID, 0, 1, "(ENDLESS GAME)", EventReceiver.COLOR_GREEN);
			} else {
				receiver.drawScoreFont(engine, playerID, 0, 1, "(" + tableGameClearLines[goaltype] + " LINES GAME)", EventReceiver.COLOR_GREEN);
			}
		}

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) ) {
			setBGState(initialBGState);
			if((!owner.replayMode) && (!big) && (engine.ai == null)) {
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
				int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
				receiver.drawScoreFont(engine, playerID, 3, topY-1, "SCORE  LINE TIME", EventReceiver.COLOR_BLUE, scale);

				if (showPlayerStats) {
					for(int i = 0; i < RANKING_MAX; i++) {
						receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
						receiver.drawScoreFont(engine, playerID,  3, topY+i, String.valueOf(rankingScorePlayer[goaltype][i]), (i == rankingRankPlayer), scale);
						receiver.drawScoreFont(engine, playerID, 10, topY+i, String.valueOf(rankingLinesPlayer[goaltype][i]), (i == rankingRankPlayer), scale);
						receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingTimePlayer[goaltype][i]), (i == rankingRankPlayer), scale);
					}

					receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 2, playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);

					receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
				} else {
					for(int i = 0; i < RANKING_MAX; i++) {
						receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
						receiver.drawScoreFont(engine, playerID,  3, topY+i, String.valueOf(rankingScore[goaltype][i]), (i == rankingRank), scale);
						receiver.drawScoreFont(engine, playerID, 10, topY+i, String.valueOf(rankingLines[goaltype][i]), (i == rankingRank), scale);
						receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingTime[goaltype][i]), (i == rankingRank), scale);
					}

					receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "LOCAL SCORES", EventReceiver.COLOR_BLUE);
					if (!playerProperties.isLoggedIn()) receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 2, "(NOT LOGGED IN)\n(E:LOG IN)");
					if (playerProperties.isLoggedIn()) receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
				}
			}
		} else if (engine.stat == GameEngine.STAT_CUSTOM) {
			playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
		} else if ( (engine.stat == GameEngine.STAT_RESULT) ) {
			setBGState(initialBGState);
		} else {
			int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
			int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
			if (pCoordList.size() > 0 && cPiece != null) {
				for (int[] loc : pCoordList) {
					int cx = baseX + (16 * loc[0]);
					int cy = baseY + (16 * loc[1]);
					RendererExtension.drawScaledPiece(receiver, engine, playerID, cx, cy, cPiece, 1f, 0f);
				}
			}

			if (glitchTimer > 0) GameTextUtilities.drawRandomRainbowScoreString(receiver, engine, playerID, 0, 3, GameTextUtilities.randomString(5, renderRandomiser), renderRandomiser, 1f);
			else receiver.drawScoreFont(engine, playerID, 0, 3, "SCORE", EventReceiver.COLOR_BLUE);

			String strScore;
			if((lastscore == 0) || (scgettime >= 120)) {
				strScore = String.valueOf(engine.statistics.score);
			} else {
				strScore = Interpolation.lerp(prevScore, engine.statistics.score, scgettime / 120d) + "(+" + lastscore + ")";
			}
			if (glitchTimer > 0) {
				GameTextUtilities.drawRandomRainbowScoreString(receiver, engine, playerID, 0, 4, GameTextUtilities.randomString(strScore.length(), renderRandomiser), renderRandomiser, 1f);
			} else {
				if ((lastscore == 0) || (scgettime >= 120)) receiver.drawScoreFont(engine, playerID, 0, 4, strScore);
				else GameTextUtilities.drawRainbowScoreString(receiver, engine, playerID, 0, 4, strScore, GameTextUtilities.RAINBOW_ORDER[rainbowPhase / 6], 1f);
			}

			if (glitchTimer > 0) GameTextUtilities.drawRandomRainbowScoreString(receiver, engine, playerID, 0, 6, GameTextUtilities.randomString(4, renderRandomiser), renderRandomiser, 1f);
			else receiver.drawScoreFont(engine, playerID, 0, 6, "LINE", EventReceiver.COLOR_BLUE);

			if (glitchTimer == 0) {
				if((engine.statistics.level >= 19) && (tableGameClearLines[goaltype] < 0))
					receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "");
				else
					receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "/" + ((engine.statistics.level + 1) * 10));
			} else {
				String sl;
				if((engine.statistics.level >= 19) && (tableGameClearLines[goaltype] < 0)) {
					sl = engine.statistics.lines + "";
					GameTextUtilities.drawRandomRainbowScoreString(receiver, engine, playerID, 0, 7, GameTextUtilities.randomString(sl.length(), renderRandomiser), renderRandomiser, 1f);
				} else {
					sl = engine.statistics.lines + "/" + ((engine.statistics.level + 1) * 10);
					GameTextUtilities.drawRandomRainbowScoreString(receiver, engine, playerID, 0, 7, GameTextUtilities.randomString(sl.length(), renderRandomiser), renderRandomiser, 1f);
				}

			}

			if (glitchTimer > 0) {
				String slv = String.valueOf(engine.statistics.level + 1);
				String st = GeneralUtil.getTime(engine.statistics.time);

				GameTextUtilities.drawRandomRainbowScoreString(receiver, engine, playerID, 0, 9, GameTextUtilities.randomString(5, renderRandomiser), renderRandomiser, 1f);
				GameTextUtilities.drawRandomRainbowScoreString(receiver, engine, playerID, 0, 10, GameTextUtilities.randomString(slv.length(), renderRandomiser), renderRandomiser, 1f);

				GameTextUtilities.drawRandomRainbowScoreString(receiver, engine, playerID, 0, 12, GameTextUtilities.randomString(4, renderRandomiser), renderRandomiser, 1f);
				GameTextUtilities.drawRandomRainbowScoreString(receiver, engine, playerID, 0, 13, GameTextUtilities.randomString(st.length(), renderRandomiser), renderRandomiser, 1f);
			} else {
				receiver.drawScoreFont(engine, playerID, 0, 9, "LEVEL", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(engine.statistics.level + 1));

				receiver.drawScoreFont(engine, playerID, 0, 12, "TIME", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 0, 13, GeneralUtil.getTime(engine.statistics.time));
			}

			if (playerProperties.isLoggedIn() || PLAYER_NAME.length() > 0) {
				if (glitchTimer == 0) {
					receiver.drawScoreFont(engine, playerID, 0, 15, "PLAYER", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, 0, 16, owner.replayMode ? PLAYER_NAME : playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
				} else {
					GameTextUtilities.drawRandomRainbowScoreString(receiver, engine, playerID, 0, 15, GameTextUtilities.randomString(6, renderRandomiser), renderRandomiser, 1f);
					GameTextUtilities.drawRandomRainbowScoreString(receiver, engine, playerID, 0, 16, GameTextUtilities.randomString(3, renderRandomiser), renderRandomiser, 2f);
				}
			}

			if((lastevent != EVENT_NONE) && (scgettime < 120)) {
				String strPieceName = Piece.getPieceName(lastpiece);

				switch(lastevent) {
					case EVENT_SINGLE:
						receiver.drawMenuFont(engine, playerID, 2, 21, "SINGLE", EventReceiver.COLOR_DARKBLUE);
						break;
					case EVENT_DOUBLE:
						receiver.drawMenuFont(engine, playerID, 2, 21, "DOUBLE", EventReceiver.COLOR_BLUE);
						break;
					case EVENT_TRIPLE:
						receiver.drawMenuFont(engine, playerID, 2, 21, "TRIPLE", EventReceiver.COLOR_GREEN);
						break;
					case EVENT_FOUR:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 3, 21, "FOUR", EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 3, 21, "FOUR", EventReceiver.COLOR_ORANGE);
						break;
					case EVENT_TSPIN_ZERO_MINI:
						receiver.drawMenuFont(engine, playerID, 2, 21, strPieceName + "-SPIN", EventReceiver.COLOR_PURPLE);
						break;
					case EVENT_TSPIN_ZERO:
						receiver.drawMenuFont(engine, playerID, 2, 21, strPieceName + "-SPIN", EventReceiver.COLOR_PINK);
						break;
					case EVENT_TSPIN_SINGLE_MINI:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-S", EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-S", EventReceiver.COLOR_ORANGE);
						break;
					case EVENT_TSPIN_SINGLE:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-SINGLE", EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-SINGLE", EventReceiver.COLOR_ORANGE);
						break;
					case EVENT_TSPIN_DOUBLE_MINI:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-D", EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-D", EventReceiver.COLOR_ORANGE);
						break;
					case EVENT_TSPIN_DOUBLE:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-DOUBLE", EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-DOUBLE", EventReceiver.COLOR_ORANGE);
						break;
					case EVENT_TSPIN_TRIPLE:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE", EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE", EventReceiver.COLOR_ORANGE);
						break;
					case EVENT_TSPIN_EZ:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName, EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName, EventReceiver.COLOR_ORANGE);
						break;
				}

				if((lastcombo >= 2) && (lastevent != EVENT_TSPIN_ZERO_MINI) && (lastevent != EVENT_TSPIN_ZERO))
					receiver.drawMenuFont(engine, playerID, 2, 22, (lastcombo - 1) + "COMBO", EventReceiver.COLOR_CYAN);
			}

			// Fake hover blocks.
			if (fakeHoverBlocks.size() > 0) {
				for (int[] location : fakeHoverBlocks) {
					receiver.drawSingleBlock(engine, playerID,
							baseX + (16 * location[0]), baseY + (16 * location[1]),Block.BLOCK_COLOR_GRAY,
							engine.getSkin(),false, 0.2f,1f, 1f);
				}
			}

			if (engine.field != null) {
				// Hard block differentiator.
				for (int y = 0; y < engine.field.getHeight(); y++) {
					for (int x = 0; x < engine.field.getWidth(); x++) {
						Block blk = engine.field.getBlock(x, y);
						if (blk != null) {
							if (blk.hard > 0) {
								receiver.drawDirectFont(engine, playerID, baseX + 4 + (x * 16), baseY + 4 + (y * 16), "g", 0.5f);
							}
						}
					}
				}

				// Glitch Covering
				if (blackoutTimer == 0 && glitchTimer > 0) {
					int maxRow;
					if (glitchTimer > 60) {
						maxRow = (int)(((glitchTimer - 120) / - 60d) * (engine.field.getHeight() - 1));
					} else {
						maxRow = engine.field.getHeight() - 1;
					}

					for (int y = 0; y <= maxRow; y++) {
						for (int x = 0; x < engine.field.getWidth(); x++) {
							int mVal = ((x % 2) + ((glitchTimer / 6) % 2)) % 2;
							if (y % 2 == mVal) receiver.drawSingleBlock(engine, playerID,
									baseX + (16 * x), baseY + (16 * y), Block.BLOCK_COLOR_GRAY,
									engine.getSkin(),true, 0f,1f, 1f);
						}
					}
				}

				// Blackout Covering
				if (blackoutTimer > 0) {
					for (int y = 0; y < engine.field.getHeight(); y++) {
						for (int x = 0; x < engine.field.getWidth(); x++) {
							receiver.drawSingleBlock(engine, playerID,
									baseX + (16 * x), baseY + (16 * y),Block.BLOCK_COLOR_GRAY,
									engine.getSkin(),false, 1f,1f, 1f);
						}
					}

					if (spookyValue >= 100 && blackoutTimer % 2 == 0 && blackoutTimer <= 16) {
						receiver.drawDirectFont(engine, playerID, bTextX, bTextY, "TURN THE COMPUTER", EventReceiver.COLOR_RED);
						receiver.drawDirectFont(engine, playerID, bTextX, bTextY + 16, "OFF IMMEDIATELY!", EventReceiver.COLOR_RED);
					}
				}
			}

			// GLITCH TEXT
			if (gTextCoords.size() > 0 && glitchTimer > 0) {
				for (int i = 0; i < gTextCoords.size(); i++) {
					receiver.drawDirectFont(engine, playerID, gTextCoords.get(i)[0], gTextCoords.get(i)[1], gTextChars.get(i), gTextCoords.get(i)[2]);
				}
			}
		}

		if (fs != null) fs.draw(receiver, engine, playerID);

		// NET: Number of spectators
		netDrawSpectatorsCount(engine, 0, 18);
		// NET: All number of players
		if(playerID == getPlayers() - 1) {
			netDrawAllPlayersCount(engine);
			netDrawGameRate(engine);
		}
		// NET: Player name (It may also appear in offline replay)
		netDrawPlayerName(engine);
	}

	@Override
	public boolean onCustom(GameEngine engine, int playerID) {
		showPlayerStats = false;

		engine.isInGame = true;

		boolean s = playerProperties.loginScreen.updateScreen(engine, playerID);
		if (playerProperties.isLoggedIn()) {
			loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
			loadSettingPlayer(playerProperties);
		}

		if (engine.stat == GameEngine.STAT_SETTING) engine.isInGame = false;

		return s;
	}

	/*
	 * TODO:
	 *  Implement the following effects -
	 *  https://canary.discordapp.com/channels/@me/561721947176697867/626143449837731860
	 */

	private void executeEffect(GameEngine engine, int playerID) {
		double procRoll = effectRandomiser.nextDouble();
		if (procRoll < BASE_EFFECT_PROC_CHANCE * (spookyValue / 50d)) {
			int effectType;
			do {
				effectType = effectTypeRandomiser.nextInt();
			} while (effectType == lastEffect);
			lastEffect = effectType;

			switch (effectType) {
				case EFFECT_FAKE_HOVER_BLOCK:
					if (fakeHoverBlocks.size() < (engine.field.getWidth() * engine.field.getHeight()) / 4) {
						int[] l;
						do {
							l = new int[] { effectRandomiser.nextInt(engine.field.getWidth()), effectRandomiser.nextInt(engine.field.getHeight()) };
						} while (fakeHoverBlocks.contains(l));
						fakeHoverBlocks.add(l);
					}
					engine.playSE("square_s");
					break;
				case EFFECT_HARDEN_ALL:
					for (int y = engine.field.getHiddenHeight() * -1; y < engine.field.getHeight(); y++) {
						for (int x = 0; x < engine.field.getWidth(); x++) {
							Block blk = engine.field.getBlock(x, y);
							if (blk != null) {
								if (blk.color > Block.BLOCK_COLOR_NONE) {
									receiver.blockBreak(engine, playerID, x, y, blk);
									blk.hard = 1;
									blk.secondaryColor = blk.color;
									blk.color = Block.BLOCK_COLOR_GRAY;
								}
							}
						}
					}
					engine.playSE("garbage");
					break;
				case EFFECT_BLACKOUT:
					blackoutTimer = effectRandomiser.nextInt(61) + 60;
					log.debug("BLACKOUT HAS BEEN SET TO OCCUR FOR " + blackoutTimer + " FRAMES");
					engine.playSE("danger");
					break;
				case EFFECT_BONE_NEXT:
					engine.getNextObject(engine.nextPieceCount).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, true);
					engine.playSE("garbage");
					break;
				case EFFECT_SCRAMBLE_COLOUR:
					for (int y = engine.field.getHiddenHeight() * -1; y < engine.field.getHeight(); y++) {
						for (int x = 0; x < engine.field.getWidth(); x++) {
							Block blk = engine.field.getBlock(x, y);
							if (blk != null) {
								if (blk.color > Block.BLOCK_COLOR_NONE && blk.hard <= 0) {
									blk.color = effectRandomiser.nextInt(8) + 1;
								}
							}
						}
					}
					engine.playSE("hurryup");
					break;
				case EFFECT_RANDOM_SOUND_EFFECT:
					engine.playSE(randomSound());
					break;
				case EFFECT_GLITCH_FIELD:
					glitchTimer = 120;
					engine.playSE("died");
					break;
				case EFFECT_SHOTGUN:
					for (int y = engine.field.getHiddenHeight() * -1; y < engine.field.getHeight(); y++) {
						boolean allEmpty = true;
						ArrayList<Integer> spaces = new ArrayList<>();

						for (int x = 0; x < engine.field.getWidth(); x++) {
							Block blk = engine.field.getBlock(x, y);
							if (blk != null) {
								if (blk.color > Block.BLOCK_COLOR_NONE) {
									allEmpty = false;
									spaces.add(x);
								}
							}
						}

						if (!allEmpty) {
							while (true) {
								int x = spaces.get(effectRandomiser.nextInt(spaces.size()));
								Block blk = engine.field.getBlock(x, y);
								if (blk != null) {
									if (blk.color > Block.BLOCK_COLOR_NONE) {
										receiver.blockBreak(engine, playerID, x, y, blk);
										blk.color = Block.BLOCK_COLOR_NONE;
										break;
									}
								}
							}
						}
					}
					engine.playSE("regret");
					break;
				case EFFECT_REAL_HOVER_BLOCK:
					int x, y;
					Block blk;
					ArrayList<int[]> coordList = new ArrayList<>();

					for (int y2 = 0; y2 < engine.field.getHeight(); y2++) {
						for (int x2 = 0; x2 < engine.field.getWidth(); x2++) {
							Block blk2 = engine.field.getBlock(x2, y2);
							if (blk2 != null) {
								if (blk2.color <= Block.BLOCK_COLOR_NONE) {
									coordList.add(new int[] { x2, y2 });
								}
							}
						}
					}

					if (coordList.size() > 0) {
						int[] loc = coordList.get(effectRandomiser.nextInt(coordList.size()));
						x = loc[0];
						y = loc[1];
						blk = engine.field.getBlock(x, y);
						blk.color = effectRandomiser.nextInt(8) + 1;
						blk.skin = engine.getSkin();
						blk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
					}

					engine.playSE("square_g");
					break;
				case EFFECT_SKIN_CHANGE:
					int skin = 0;
					switch (holderType) {
						case HOLDER_SLICK:
							skin = effectRandomiser.nextInt(ResourceHolder.imgNormalBlockList.size());
							break;
						case HOLDER_SWING:
							skin = effectRandomiser.nextInt(ResourceHolderSwing.imgNormalBlockList.size());
							break;
						case HOLDER_SDL:
							skin = effectRandomiser.nextInt(ResourceHolderSDL.imgNormalBlockList.size());
							break;
						default:
							break;
					}

					int amount = effectRandomiser.nextInt(21) + 4;
					for (int i = 0; i < amount; i++) {
						engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay + i).setSkin(skin);
					}
					engine.playSE("medal");
					break;
				default:
					break;
			}
		}
	}

	/*
	 * Called after every frame
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		scgettime++;
		rainbowPhase = (rainbowPhase + 1) % (GameTextUtilities.RAINBOW_COLOURS * 6);

		if (engine.stat != GameEngine.STAT_SETTING && engine.stat != GameEngine.STAT_RESULT && effectRandomiser != null) {
			if (glitchTimer > 0) glitchTimer--;
			if (blackoutTimer > 0) blackoutTimer--;

			if (spookyValue >= 50) {
				boolean ct, st;
				double ctd, std;

				executeEffect(engine, playerID);

				ctd = titleCoefficient;
				std = subtextCoefficient;

				frameCoefficient = effectRandomiser.nextDouble();
				titleCoefficient = effectRandomiser.nextDouble();
				subtextCoefficient = effectRandomiser.nextDouble();

				ct = ctd < FRAME_COLOUR_FLUCTUATION_CHANCE * Math.pow(spookyValue / 50d, 3.5);
				st = std < FRAME_COLOUR_FLUCTUATION_CHANCE * Math.pow(spookyValue / 50d, 3.5);

				if (frameCoefficient < FRAME_COLOUR_FLUCTUATION_CHANCE * Math.pow(spookyValue / 50d, 3.5)) {
					engine.framecolor = GameEngine.FRAME_COLOR_RED;
				} else {
					engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
				}

				if (titleCoefficient < FRAME_COLOUR_FLUCTUATION_CHANCE * Math.pow(spookyValue / 50d, 3.5)) {
					if (!ct) currentTitle = randomString(POSSIBLE_NAMES);
				}

				if (subtextCoefficient < FRAME_COLOUR_FLUCTUATION_CHANCE * Math.pow(spookyValue / 50d, 3.5)) {
					if (!st) currentSubtext = randomString(POSSIBLE_SUBTEXT);
				}
			} else if (spookyValue >= 25) {
				titleCoefficient = effectRandomiser.nextDouble();
			} else {
				frameCoefficient = 1;
				titleCoefficient = 1;
				subtextCoefficient = 1;
			}

			if (engine.stat == GameEngine.STAT_EXCELLENT || engine.stat == GameEngine.STAT_GAMEOVER || engine.stat == GameEngine.STAT_READY) {
				engine.framecolor = GameEngine.FRAME_COLOR_GREEN;

				frameCoefficient = 1;
				titleCoefficient = 1;
				subtextCoefficient = 1;

				glitchTimer = 0;
				blackoutTimer = 0;
				spookyValue = 0;

				gTextCoords.clear();
				fakeHoverBlocks.clear();
				gTextChars.clear();
			}

			// Reset previously grey hard blocks to their normal colour.
			for (int y = engine.field.getHiddenHeight() * -1; y < engine.field.getHeight(); y++) {
				for (int x = 0; x < engine.field.getWidth(); x++) {
					Block blk = engine.field.getBlock(x, y);
					if (blk != null) {
						if (blk.hard == 0 && blk.color > Block.BLOCK_COLOR_NONE) {
							if (blk.secondaryColor > 0) blk.color = blk.secondaryColor;
						}
					}
				}
			}

			if (blackoutTimer > 0) {
				bTextX = effectRandomiser.nextInt(640);
				bTextY = effectRandomiser.nextInt(480);
				setBGState(false);  // BG is black during a blackout.
				bgmPause();
			} else {
				setBGState(true);
				bgmResume();
			}

			if (glitchTimer > 0) {
				gTextCoords.clear();
				gTextChars.clear();

				for (int i = 0; i < -2 * (glitchTimer - 120 - 1); i++) {
					int[] b;
					do {
						b = new int[] { effectRandomiser.nextInt(640 / 16) * 16, effectRandomiser.nextInt(480 / 16) * 16 };
					} while (gTextCoords.contains(b));

					gTextCoords.add(b);
					gTextChars.add(String.valueOf(characters.charAt(effectRandomiser.nextInt(characters.length()))));
				}

				for (int i = 0; i < gTextCoords.size(); i++) {
					int[] b = gTextCoords.get(i);
					b = new int[] { b[0], b[1], effectRandomiser.nextInt(10) };
					gTextCoords.set(i, b);
				}

				if (effectRandomiser.nextDouble() < (1d / (glitchTimer * 4))) {
					engine.playSE(randomSound());
				}
			}

			if (effectRandomiser.nextDouble() < (1d / 3200d)) {
				engine.playSE(randomSound());
			}
		}

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) || engine.stat == GameEngine.STAT_CUSTOM ) {
			// Show rank
			if(engine.ctrl.isPush(Controller.BUTTON_F) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
				showPlayerStats = !showPlayerStats;
				engine.playSE("change");
			}
		}

		if (fs != null) fs.update();
		if (fs != null) if (fs.shouldNull()) fs = null;

		if (engine.quitflag) fs = null;

		if (engine.quitflag) {
			playerProperties = new ProfileProperties(headerColour);
		}
	}

	/*
	 * Calculate score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Line clear bonus
		int pts = 0;

		if(engine.tspin) {
			// T-Spin 0 lines
			if((lines == 0) && (!engine.tspinez)) {
				if(engine.tspinmini) {
					pts += 100 * (engine.statistics.level + 1);
					lastevent = EVENT_TSPIN_ZERO_MINI;
				} else {
					pts += 400 * (engine.statistics.level + 1);
					lastevent = EVENT_TSPIN_ZERO;
				}
			}
			// Immobile EZ Spin
			else if(engine.tspinez && (lines > 0)) {
				if(engine.b2b) {
					pts += 180 * (engine.statistics.level + 1);
				} else {
					pts += 120 * (engine.statistics.level + 1);
				}
				lastevent = EVENT_TSPIN_EZ;
			}
			// T-Spin 1 line
			else if(lines == 1) {
				if(engine.tspinmini) {
					if(engine.b2b) {
						pts += 300 * (engine.statistics.level + 1);
					} else {
						pts += 200 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_SINGLE_MINI;
				} else {
					if(engine.b2b) {
						pts += 1200 * (engine.statistics.level + 1);
					} else {
						pts += 800 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_SINGLE;
				}
			}
			// T-Spin 2 lines
			else if(lines == 2) {
				if(engine.tspinmini && engine.useAllSpinBonus) {
					if(engine.b2b) {
						pts += 600 * (engine.statistics.level + 1);
					} else {
						pts += 400 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_DOUBLE_MINI;
				} else {
					if(engine.b2b) {
						pts += 1800 * (engine.statistics.level + 1);
					} else {
						pts += 1200 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_DOUBLE;
				}
			}
			// T-Spin 3 lines
			else if(lines >= 3) {
				if(engine.b2b) {
					pts += 2400 * (engine.statistics.level + 1);
				} else {
					pts += 1600 * (engine.statistics.level + 1);
				}
				lastevent = EVENT_TSPIN_TRIPLE;
			}
		} else {
			if(lines == 1) {
				pts += 100 * (engine.statistics.level + 1); // 1列
				lastevent = EVENT_SINGLE;
			} else if(lines == 2) {
				pts += 300 * (engine.statistics.level + 1); // 2列
				lastevent = EVENT_DOUBLE;
			} else if(lines == 3) {
				pts += 500 * (engine.statistics.level + 1); // 3列
				lastevent = EVENT_TRIPLE;
			} else if(lines >= 4) {
				// 4 lines
				if(engine.b2b) {
					pts += 1200 * (engine.statistics.level + 1);
				} else {
					pts += 800 * (engine.statistics.level + 1);
				}
				lastevent = EVENT_FOUR;
			}
		}

		lastb2b = engine.b2b;

		// Combo
		if((enableCombo) && (engine.combo >= 1) && (lines >= 1)) {
			pts += ((engine.combo - 1) * 50) * (engine.statistics.level + 1);
			lastcombo = engine.combo;
		}

		// All clear
		if((lines >= 1) && (engine.field.isEmpty())) {
			engine.playSE("bravo");
			pts += 1800 * (engine.statistics.level + 1);
		}

		// Add to score
		if(pts > 0) {
			if (blackoutTimer > 0) pts *= 2;
			if (glitchTimer > 0) pts *= 1.5;

			lastscore = pts;
			lastpiece = engine.nowPieceObject.id;
			scgettime = 0;
			if(lines >= 1) engine.statistics.scoreFromLineClear += pts;
			else engine.statistics.scoreFromOtherBonus += pts;
			prevScore = engine.statistics.score;
			engine.statistics.score += pts;
		}

		spookyValue = engine.statistics.lines;
		if (engine.statistics.lines > SPOOKY_MAX) spookyValue = SPOOKY_MAX;

		// BGM fade-out effects and BGM changes
		if(tableBGMChange[bgmlv] != -1) {
			if(engine.statistics.lines >= tableBGMChange[bgmlv] - 5) owner.bgmStatus.fadesw = true;

			if( (engine.statistics.lines >= tableBGMChange[bgmlv]) &&
					((engine.statistics.lines < tableGameClearLines[goaltype]) || (tableGameClearLines[goaltype] < 0)) )
			{
				bgmlv++;
				owner.bgmStatus.bgm = bgmlv;
				owner.bgmStatus.fadesw = false;
			}
		}

		// Meter
		engine.meterValue = ((engine.statistics.lines % 10) * receiver.getMeterMax(engine)) / 9;
		engine.meterColor = GameEngine.METER_COLOR_GREEN;
		if(engine.statistics.lines % 10 >= 4) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		if(engine.statistics.lines % 10 >= 6) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		if(engine.statistics.lines % 10 >= 8) engine.meterColor = GameEngine.METER_COLOR_RED;

		if((engine.statistics.lines >= tableGameClearLines[goaltype]) && (tableGameClearLines[goaltype] >= 0)) {
			// Ending
			engine.ending = 1;
			engine.gameEnded();
		} else if((engine.statistics.lines >= (engine.statistics.level + 1) * 10) && (engine.statistics.level < 19)) {
			// Level up
			engine.statistics.level++;

			owner.backgroundStatus.fadesw = true;
			owner.backgroundStatus.fadecount = 0;
			owner.backgroundStatus.fadebg = engine.statistics.level;

			setSpeed(engine);
			engine.playSE("levelup");
		}
	}

	/*
	 * Render results screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		drawResultStats(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE,
				STAT_SCORE, STAT_LINES, STAT_LEVEL, STAT_TIME, STAT_SPL, STAT_LPM);
		drawResultRank(engine, playerID, receiver, 12, EventReceiver.COLOR_BLUE, rankingRank);
		drawResultNetRank(engine, playerID, receiver, 14, EventReceiver.COLOR_BLUE, netRankingRank[0]);
		drawResultNetRankDaily(engine, playerID, receiver, 16, EventReceiver.COLOR_BLUE, netRankingRank[1]);

		if(netIsPB) {
			receiver.drawMenuFont(engine, playerID, 2, 21, "NEW PB", EventReceiver.COLOR_ORANGE);
		}

		if(netIsNetPlay && (netReplaySendStatus == 1)) {
			receiver.drawMenuFont(engine, playerID, 0, 22, "SENDING...", EventReceiver.COLOR_PINK);
		} else if(netIsNetPlay && !netIsWatch && (netReplaySendStatus == 2)) {
			receiver.drawMenuFont(engine, playerID, 1, 22, "A: RETRY", EventReceiver.COLOR_RED);
		}
	}

	/*
	 * Called when saving replay
	 */
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		saveSetting(prop);

		// NET: Save name
		if((netPlayerName != null) && (netPlayerName.length() > 0)) {
			prop.setProperty(playerID + ".net.netPlayerName", netPlayerName);
		}

		// Update rankings
		if((!owner.replayMode) && (!big) && (engine.ai == null)) {
			updateRanking(engine.statistics.score, engine.statistics.lines, engine.statistics.time, goaltype);

			if (playerProperties.isLoggedIn()) {
				prop.setProperty("marathon2.playerName", playerProperties.getNameDisplay());
			}

			if(rankingRank != -1) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				receiver.saveModeConfig(owner.modeConfig);
			}

			if(rankingRankPlayer != -1 && playerProperties.isLoggedIn()) {
				saveRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
				playerProperties.saveProfileConfig();
			}
		}
	}

	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSetting(CustomProperties prop) {
		startlevel = prop.getProperty("marathon2.startlevel", 0);
		tspinEnableType = prop.getProperty("marathon2.tspinEnableType", 1);
		enableTSpin = prop.getProperty("marathon2.enableTSpin", true);
		enableTSpinKick = prop.getProperty("marathon2.enableTSpinKick", true);
		spinCheckType = prop.getProperty("marathon2.spinCheckType", 0);
		tspinEnableEZ = prop.getProperty("marathon2.tspinEnableEZ", false);
		enableB2B = prop.getProperty("marathon2.enableB2B", true);
		enableCombo = prop.getProperty("marathon2.enableCombo", true);
		goaltype = prop.getProperty("marathon2.gametype", 0);
		big = prop.getProperty("marathon2.big", false);
		version = prop.getProperty("marathon2.version", 0);
		initialBGState = prop.getProperty("marathon2.initBG", true);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("marathon2.startlevel", startlevel);
		prop.setProperty("marathon2.tspinEnableType", tspinEnableType);
		prop.setProperty("marathon2.enableTSpin", enableTSpin);
		prop.setProperty("marathon2.enableTSpinKick", enableTSpinKick);
		prop.setProperty("marathon2.spinCheckType", spinCheckType);
		prop.setProperty("marathon2.tspinEnableEZ", tspinEnableEZ);
		prop.setProperty("marathon2.enableB2B", enableB2B);
		prop.setProperty("marathon2.enableCombo", enableCombo);
		prop.setProperty("marathon2.gametype", goaltype);
		prop.setProperty("marathon2.big", big);
		prop.setProperty("marathon2.version", version);
		prop.setProperty("marathon2.initBG", initialBGState);
	}

	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSettingPlayer(ProfileProperties prop) {
		if (!prop.isLoggedIn()) return;
		startlevel = prop.getProperty("marathon2.startlevel", 0);
		tspinEnableType = prop.getProperty("marathon2.tspinEnableType", 1);
		enableTSpin = prop.getProperty("marathon2.enableTSpin", true);
		enableTSpinKick = prop.getProperty("marathon2.enableTSpinKick", true);
		spinCheckType = prop.getProperty("marathon2.spinCheckType", 0);
		tspinEnableEZ = prop.getProperty("marathon2.tspinEnableEZ", false);
		enableB2B = prop.getProperty("marathon2.enableB2B", true);
		enableCombo = prop.getProperty("marathon2.enableCombo", true);
		goaltype = prop.getProperty("marathon2.gametype", 0);
		big = prop.getProperty("marathon2.big", false);
		initialBGState = prop.getProperty("marathon2.initBG", true);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSettingPlayer(ProfileProperties prop) {
		if (!prop.isLoggedIn()) return;
		prop.setProperty("marathon2.startlevel", startlevel);
		prop.setProperty("marathon2.tspinEnableType", tspinEnableType);
		prop.setProperty("marathon2.enableTSpin", enableTSpin);
		prop.setProperty("marathon2.enableTSpinKick", enableTSpinKick);
		prop.setProperty("marathon2.spinCheckType", spinCheckType);
		prop.setProperty("marathon2.tspinEnableEZ", tspinEnableEZ);
		prop.setProperty("marathon2.enableB2B", enableB2B);
		prop.setProperty("marathon2.enableCombo", enableCombo);
		prop.setProperty("marathon2.gametype", goaltype);
		prop.setProperty("marathon2.big", big);
		prop.setProperty("marathon2.initBG", initialBGState);
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	@Override
	protected void loadRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			for(int j = 0; j < GAMETYPE_MAX; j++) {
				rankingScore[j][i] = prop.getProperty("marathon2.ranking." + ruleName + "." + j + ".score." + i, 0);
				rankingLines[j][i] = prop.getProperty("marathon2.ranking." + ruleName + "." + j + ".lines." + i, 0);
				rankingTime[j][i] = prop.getProperty("marathon2.ranking." + ruleName + "." + j + ".time." + i, 0);
			}
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			for(int j = 0; j < GAMETYPE_MAX; j++) {
				prop.setProperty("marathon2.ranking." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
				prop.setProperty("marathon2.ranking." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
				prop.setProperty("marathon2.ranking." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
			}
		}
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void loadRankingPlayer(ProfileProperties prop, String ruleName) {
		if (!prop.isLoggedIn()) return;
		for(int i = 0; i < RANKING_MAX; i++) {
			for(int j = 0; j < GAMETYPE_MAX; j++) {
				rankingScorePlayer[j][i] = prop.getProperty("marathon2.ranking." + ruleName + "." + j + ".score." + i, 0);
				rankingLinesPlayer[j][i] = prop.getProperty("marathon2.ranking." + ruleName + "." + j + ".lines." + i, 0);
				rankingTimePlayer[j][i] = prop.getProperty("marathon2.ranking." + ruleName + "." + j + ".time." + i, 0);
			}
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void saveRankingPlayer(ProfileProperties prop, String ruleName) {
		if (!prop.isLoggedIn()) return;
		for(int i = 0; i < RANKING_MAX; i++) {
			for(int j = 0; j < GAMETYPE_MAX; j++) {
				prop.setProperty("marathon2.ranking." + ruleName + "." + j + ".score." + i, rankingScorePlayer[j][i]);
				prop.setProperty("marathon2.ranking." + ruleName + "." + j + ".lines." + i, rankingLinesPlayer[j][i]);
				prop.setProperty("marathon2.ranking." + ruleName + "." + j + ".time." + i, rankingTimePlayer[j][i]);
			}
		}
	}

	/**
	 * Update rankings
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 */
	private void updateRanking(int sc, int li, int time, int type) {
		rankingRank = checkRanking(sc, li, time, type);

		if(rankingRank != -1) {
			// Shift down ranking entries
			for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingScore[type][i] = rankingScore[type][i - 1];
				rankingLines[type][i] = rankingLines[type][i - 1];
				rankingTime[type][i] = rankingTime[type][i - 1];
			}

			// Add new data
			rankingScore[type][rankingRank] = sc;
			rankingLines[type][rankingRank] = li;
			rankingTime[type][rankingRank] = time;
		}

		if (playerProperties.isLoggedIn()) {
			rankingRankPlayer = checkRankingPlayer(sc, li, time, type);

			if(rankingRankPlayer != -1) {
				// Shift down ranking entries
				for(int i = RANKING_MAX - 1; i > rankingRankPlayer; i--) {
					rankingScorePlayer[type][i] = rankingScorePlayer[type][i - 1];
					rankingLinesPlayer[type][i] = rankingLinesPlayer[type][i - 1];
					rankingTimePlayer[type][i] = rankingTimePlayer[type][i - 1];
				}

				// Add new data
				rankingScorePlayer[type][rankingRankPlayer] = sc;
				rankingLinesPlayer[type][rankingRankPlayer] = li;
				rankingTimePlayer[type][rankingRankPlayer] = time;
			}
		}
	}

	/**
	 * Calculate ranking position
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 * @return Position (-1 if unranked)
	 */
	private int checkRanking(int sc, int li, int time, int type) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if(sc > rankingScore[type][i]) {
				return i;
			} else if((sc == rankingScore[type][i]) && (li > rankingLines[type][i])) {
				return i;
			} else if((sc == rankingScore[type][i]) && (li == rankingLines[type][i]) && (time < rankingTime[type][i])) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Calculate ranking position
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 * @return Position (-1 if unranked)
	 */
	private int checkRankingPlayer(int sc, int li, int time, int type) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if(sc > rankingScorePlayer[type][i]) {
				return i;
			} else if((sc == rankingScorePlayer[type][i]) && (li > rankingLinesPlayer[type][i])) {
				return i;
			} else if((sc == rankingScorePlayer[type][i]) && (li == rankingLinesPlayer[type][i]) && (time < rankingTimePlayer[type][i])) {
				return i;
			}
		}

		return -1;
	}
}
