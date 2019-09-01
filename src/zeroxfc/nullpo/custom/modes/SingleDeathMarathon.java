package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.component.*;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;

import java.util.ArrayList;
import java.util.Random;

public class SingleDeathMarathon extends MarathonModeBase {
	private static final String[] PIECE_NAMES = {
			"I-PIECE",
			"L-PIECE",
			"O-PIECE",
			"Z-PIECE",
			"T-PIECE",
			"J-PIECE",
			"S-PIECE"
	};

	private static final int[] PIECE_COLOURS = {
			EventReceiver.COLOR_CYAN,
			EventReceiver.COLOR_ORANGE,
			EventReceiver.COLOR_YELLOW,
			EventReceiver.COLOR_RED,
			EventReceiver.COLOR_PURPLE,
			EventReceiver.COLOR_DARKBLUE,
			EventReceiver.COLOR_GREEN
	};

	private ArrayList<Integer> pieceIDQueue;
	private ArrayList<Integer> pieceIDHistory;
	private Random localIDRandomiser;
	private int previousScore;

	private ProfileProperties playerProperties;
	private static final int headerColour = EventReceiver.COLOR_RED;
	private boolean showPlayerStats;
	private String PLAYER_NAME;
	private int rankingRankPlayer;
	private int[][] rankingScorePlayer;
	private int[][] rankingLinesPlayer;
	private int[][] rankingTimePlayer;

	/** The good hard drop effect */
	private ArrayList<int[]> pCoordList;
	private Piece cPiece;

	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "SINGLE DEATH MARATHON";
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
		previousScore = 0;

		rankingRank = -1;
		rankingScore = new int[RANKING_TYPE][RANKING_MAX];
		rankingLines = new int[RANKING_TYPE][RANKING_MAX];
		rankingTime = new int[RANKING_TYPE][RANKING_MAX];

		pieceIDQueue = new ArrayList<>();
		pieceIDHistory = new ArrayList<>();
		localIDRandomiser = null;

		pCoordList = new ArrayList<>();
		cPiece = null;

		if (playerProperties == null) {
			playerProperties = new ProfileProperties(headerColour);

			showPlayerStats = false;
		}

		rankingRankPlayer = -1;
		rankingScorePlayer = new int[RANKING_TYPE][RANKING_MAX];
		rankingLinesPlayer = new int[RANKING_TYPE][RANKING_MAX];
		rankingTimePlayer = new int[RANKING_TYPE][RANKING_MAX];

		netPlayerInit(engine, playerID);

		if(owner.replayMode == false) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

			if (playerProperties.isLoggedIn()) {
				loadSettingPlayer(playerProperties);
				loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
			}

			PLAYER_NAME = "";
			version = CURRENT_VERSION;
		} else {
			loadSetting(owner.replayProp);
			if((version == 0) && (owner.replayProp.getProperty("singledeath.endless", false) == true)) goaltype = 2;

			PLAYER_NAME = owner.replayProp.getProperty("singledeath.playerName", "");

			// NET: Load name
			netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}

		engine.owner.backgroundStatus.bg = startlevel;
		engine.bighalf = false;
		engine.bigmove = false;
		engine.framecolor = GameEngine.FRAME_COLOR_RED;
	}

	/*
	 * Called at settings screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// NET: Net Ranking
		if(netIsNetRankingDisplayMode) {
			netOnUpdateNetPlayRanking(engine, goaltype);
		}
		// Menu
		else if(engine.owner.replayMode == false) {
			// Configuration changes
			int change = updateCursor(engine, 7, playerID);

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

			if(engine.statc[3] >= 60) {
				return false;
			}
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
					"GOAL",  (goaltype == 2) ? "ENDLESS" : tableGameClearLines[goaltype] + " LINES");
		}
	}

	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		if (engine.statc[0] == 0) {
			localIDRandomiser = new Random(engine.randSeed + 1);
			previousScore = 0;

			resetQueues();

			for (int i = 0; i < 4; i++) {
				appendQueue(false);
			}
		}

		if (engine.statc[0] == 1) {
			if (setBigOrNot(engine, engine.getNextObject(engine.nextPieceCount), pieceIDQueue.get(0))) {
				appendQueue(true);
			}
		}

		return false;
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
	 * Called for initialization during "Ready" screen
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.statistics.level = startlevel;
		engine.statistics.levelDispAdd = 1;
		engine.b2bEnable = enableB2B;
		if(enableCombo == true) {
			engine.comboType = GameEngine.COMBO_TYPE_NORMAL;
		} else {
			engine.comboType = GameEngine.COMBO_TYPE_DISABLE;
		}

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

	private void resetQueues() {
		pieceIDQueue.clear();
		pieceIDHistory.clear();

		appendHistory(-1, false);
		appendHistory(-1, false);
	}

	private void appendQueue(boolean remove) {
		if (remove) pieceIDQueue.remove(0);
		int v, rolls = 0;
		do {
			v = localIDRandomiser.nextInt(7);
			rolls++;
		} while (pieceIDHistory.contains(v) && rolls < 2);

		pieceIDQueue.add(v);
		appendHistory(v, true);
	}

	private void appendHistory(int e, boolean remove) {
		if (remove) pieceIDHistory.remove(0);
		pieceIDHistory.add(e);
	}

	private boolean setBigOrNot(GameEngine engine, Piece piece, int selectedID) {
		boolean res = piece.id == selectedID;

		engine.big = res;
		piece.big = res;

		return res;
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

	/*
	 * Called after every frame
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		scgettime++;

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) || engine.stat == GameEngine.STAT_CUSTOM ) {
			// Show rank
			if(engine.ctrl.isPush(Controller.BUTTON_F) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
				showPlayerStats = !showPlayerStats;
				engine.playSE("change");
			}
		}
	}

	/*
	 * Render score
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;

		receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_RED);

		if(tableGameClearLines[goaltype] == -1) {
			receiver.drawScoreFont(engine, playerID, 0, 1, "(ENDLESS GAME)", EventReceiver.COLOR_RED);
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 1, "(" + tableGameClearLines[goaltype] + " LINES GAME)", EventReceiver.COLOR_RED);
		}

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false)) ) {
			if((owner.replayMode == false) && (big == false) && (engine.ai == null)) {
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
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 3, "SCORE", EventReceiver.COLOR_BLUE);
			String strScore;
			if((lastscore == 0) || (scgettime >= 120)) {
				strScore = String.valueOf(engine.statistics.score);
			} else {
				int cScore = Interpolation.lerp(previousScore, engine.statistics.score, (double)scgettime / 120.0);
				strScore = String.valueOf(cScore) + "(+" + String.valueOf(lastscore) + ")";
			}
			receiver.drawScoreFont(engine, playerID, 0, 4, strScore);

			if (playerProperties.isLoggedIn() || PLAYER_NAME.length() > 0) {
				receiver.drawScoreFont(engine, playerID, 10, 6, "PLAYER", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 10, 7, owner.replayMode ? PLAYER_NAME : playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
			}

			receiver.drawScoreFont(engine, playerID, 0, 6, "LINE", EventReceiver.COLOR_BLUE);
			if((engine.statistics.level >= 19) && (tableGameClearLines[goaltype] < 0))
				receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "");
			else
				receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "/" + ((engine.statistics.level + 1) * 10));

			receiver.drawScoreFont(engine, playerID, 0, 9, "LEVEL", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(engine.statistics.level + 1));

			receiver.drawScoreFont(engine, playerID, 0, 12, "TIME", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 13, GeneralUtil.getTime(engine.statistics.time));

			receiver.drawScoreFont(engine, playerID, 0, 15, "BIG QUEUE", EventReceiver.COLOR_BLUE);
			int j = 0;
			for (Integer i : pieceIDQueue) {
				receiver.drawScoreFont(engine, playerID, 0, 16 + j, (j == 0 ? "> " : "") + PIECE_NAMES[i], PIECE_COLOURS[i]);
				j++;
			}

			int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
			int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
			if (pCoordList.size() > 0 && cPiece != null) {
				for (int[] loc : pCoordList) {
					int cx = baseX + (16 * loc[0]);
					int cy = baseY + (16 * loc[1]);
					RendererExtension.drawScaledPiece(receiver, engine, playerID, cx, cy, cPiece, 1f, 0f);
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
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 3, 21, "FOUR+", EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 3, 21, "FOUR+", EventReceiver.COLOR_ORANGE);
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
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE+", EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE+", EventReceiver.COLOR_ORANGE);
						break;
					case EVENT_TSPIN_EZ:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName, EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName, EventReceiver.COLOR_ORANGE);
						break;
				}

				if((lastcombo >= 2) && (lastevent != EVENT_TSPIN_ZERO_MINI) && (lastevent != EVENT_TSPIN_ZERO))
					receiver.drawMenuFont(engine, playerID, 2, 22, (lastcombo - 1) + "COMBO", EventReceiver.COLOR_CYAN);
			}
		}

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

	// region MOVE OVERRIDE
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		owner.receiver.onMove(engine, playerID);

		// 横溜めInitialization
		int moveDirection = engine.getMoveDirection();

		if((engine.statc[0] > 0) || (engine.ruleopt.dasInMoveFirstFrame)) {
			if(engine.dasDirection != moveDirection) {
				engine.dasDirection = moveDirection;
				if(!(engine.dasDirection == 0 && engine.ruleopt.dasStoreChargeOnNeutral)){
					engine.dasCount = 0;
				}
			}
		}

		// 出現時の処理
		if(engine.statc[0] == 0) {
			if((engine.statc[1] == 0) && (engine.initialHoldFlag == false)) {
				// 通常出現
				engine.nowPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
				engine.nextPieceCount++;
				if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;
				engine.holdDisable = false;
			} else {
				// ホールド出現
				if(engine.initialHoldFlag) {
					// 先行ホールド
					if(engine.holdPieceObject == null) {
						// 1回目
						engine.holdPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
						engine.holdPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.holdPieceObject.id], engine.ruleopt.pieceOffsetY[engine.holdPieceObject.id]);
						engine.nextPieceCount++;
						if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;

						if(engine.bone == true) engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, true);

						engine.nowPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);

						if (setBigOrNot(engine, engine.nowPieceObject, pieceIDQueue.get(0))) {
							appendQueue(true);
						}

						engine.nextPieceCount++;
						if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;
					} else {
						// 2回目以降
						Piece pieceTemp = engine.holdPieceObject;

						if (setBigOrNot(engine, pieceTemp, pieceIDQueue.get(0))) {
							appendQueue(true);
						}

						engine.holdPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
						engine.holdPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.holdPieceObject.id], engine.ruleopt.pieceOffsetY[engine.holdPieceObject.id]);
						engine.nowPieceObject = pieceTemp;
						engine.nextPieceCount++;
						if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;
					}
				} else {
					// 通常ホールド
					if(engine.holdPieceObject == null) {
						// 1回目

						engine.holdPieceObject = engine.nowPieceObject;
						engine.nowPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);

						if (setBigOrNot(engine, engine.nowPieceObject, pieceIDQueue.get(0))) {
							appendQueue(true);
						}

						engine.nextPieceCount++;
						if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;
					} else {
						// 2回目以降
						Piece pieceTemp = engine.holdPieceObject;
						engine.holdPieceObject = engine.nowPieceObject;
						engine.nowPieceObject = pieceTemp;

						if (pieceTemp.big) {
							engine.nowPieceObject.big = true;
							engine.big = true;
						} else {
							if (setBigOrNot(engine, engine.nowPieceObject, pieceIDQueue.get(0))) {
								appendQueue(true);
							}
						}
					}
				}

				// Directionを戻す
				if((engine.ruleopt.holdResetDirection) && (engine.ruleopt.pieceDefaultDirection[engine.holdPieceObject.id] < Piece.DIRECTION_COUNT)) {
					engine.holdPieceObject.direction = engine.ruleopt.pieceDefaultDirection[engine.holdPieceObject.id];
					engine.holdPieceObject.updateConnectData();
				}

				// 使用した count+1
				engine.holdUsedCount++;
				engine.statistics.totalHoldUsed++;

				// ホールド無効化
				engine.initialHoldFlag = false;
				engine.holdDisable = true;
			}
			engine.playSE("piece" + engine.getNextObject(engine.nextPieceCount).id);

			if(engine.nowPieceObject.offsetApplied == false)
				engine.nowPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.nowPieceObject.id], engine.ruleopt.pieceOffsetY[engine.nowPieceObject.id]);

			engine.nowPieceObject.big = engine.big;

			// 出現位置 (横）
			engine.nowPieceX = engine.getSpawnPosX(engine.field, engine.nowPieceObject);

			// 出現位置 (縦）
			engine.nowPieceY = engine.getSpawnPosY(engine.nowPieceObject);

			engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);
			engine.nowPieceColorOverride = -1;

			if(engine.itemRollRollEnable) engine.nowPieceColorOverride = Block.BLOCK_COLOR_GRAY;

			// 先行rotation
			if(engine.versionMajor < 7.5f) engine.initialRotate(); //XXX: Weird active time IRS
			//if( (getARE() != 0) && ((getARELine() != 0) || (version < 6.3f)) ) initialRotate();

			if((engine.speed.gravity > engine.speed.denominator) && (engine.speed.denominator > 0))
				engine.gcount = engine.speed.gravity % engine.speed.denominator;
			else
				engine.gcount = 0;

			engine.lockDelayNow = 0;
			engine.dasSpeedCount = engine.getDASDelay();
			engine.dasRepeat = false;
			engine.dasInstant = false;
			engine.extendedMoveCount = 0;
			engine.extendedRotateCount = 0;
			engine.softdropFall = 0;
			engine.harddropFall = 0;
			engine.manualLock = false;
			engine.nowPieceMoveCount = 0;
			engine.nowPieceRotateCount = 0;
			engine.nowPieceRotateFailCount = 0;
			engine.nowWallkickCount = 0;
			engine.nowUpwardWallkickCount = 0;
			engine.lineClearing = 0;
			engine.lastmove = GameEngine.LASTMOVE_NONE;
			engine.kickused = false;
			engine.tspin = false;
			engine.tspinmini = false;
			engine.tspinez = false;

			engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, engine.bone);

			if(engine.ending == 0) engine.timerActive = true;

			if((engine.ai != null) && (!owner.replayMode || owner.replayRerecord)) engine.ai.newPiece(engine, playerID);
		}

		engine.checkDropContinuousUse();

		boolean softdropUsed = false; // この frame にSoft dropを使ったらtrue
		int softdropFallNow = 0; // この frame のSoft dropで落下した段count

		boolean updown = false; // Up下同時押し flag
		if(engine.ctrl.isPress(engine.getUp()) && engine.ctrl.isPress(engine.getDown())) updown = true;

		if(!engine.dasInstant) {

			// ホールド
			if(engine.ctrl.isPush(Controller.BUTTON_D) || engine.initialHoldFlag) {
				if(engine.isHoldOK()) {
					engine.statc[0] = 0;
					engine.statc[1] = 1;
					if(!engine.initialHoldFlag) engine.playSE("hold");
					engine.initialHoldContinuousUse = true;
					engine.initialHoldFlag = false;
					engine.holdDisable = true;
					engine.initialRotate(); //Hold swap triggered IRS
					engine.statMove();
					return true;
				} else if((engine.statc[0] > 0) && (!engine.initialHoldFlag)) {
					engine.playSE("holdfail");
				}
			}

			// rotation
			boolean onGroundBeforeRotate = engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field);
			int move = 0;
			boolean rotated = false;

			if(engine.initialRotateDirection != 0) {
				move = engine.initialRotateDirection;
				engine.initialRotateLastDirection = engine.initialRotateDirection;
				engine.initialRotateContinuousUse = true;
				engine.playSE("initialrotate");
			} else if((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame == true)) {
				if((engine.itemRollRollEnable) && (engine.replayTimer % engine.itemRollRollInterval == 0)) move = 1;	// Roll Roll

				//  button input
				if(engine.ctrl.isPush(Controller.BUTTON_A) || engine.ctrl.isPush(Controller.BUTTON_C)) move = -1;
				else if(engine.ctrl.isPush(Controller.BUTTON_B)) move = 1;
				else if(engine.ctrl.isPush(Controller.BUTTON_E)) move = 2;

				if(move != 0) {
					engine.initialRotateLastDirection = move;
					engine.initialRotateContinuousUse = true;
				}
			}

			if((engine.ruleopt.rotateButtonAllowDouble == false) && (move == 2)) move = -1;
			if((engine.ruleopt.rotateButtonAllowReverse == false) && (move == 1)) move = -1;
			if(engine.isRotateButtonDefaultRight() && (move != 2)) move = move * -1;

			if(move != 0) {
				// Direction after rotationを決める
				int rt = engine.getRotateDirection(move);

				// rotationできるか判定
				if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, rt, engine.field) == false)
				{
					// Wallkickなしでrotationできるとき
					rotated = true;
					engine.kickused = false;
					engine.nowPieceObject.direction = rt;
					engine.nowPieceObject.updateConnectData();
				} else if( (engine.ruleopt.rotateWallkick == true) &&
						(engine.wallkick != null) &&
						((engine.initialRotateDirection == 0) || (engine.ruleopt.rotateInitialWallkick == true)) &&
						((engine.ruleopt.lockresetLimitOver != RuleOptions.LOCKRESET_LIMIT_OVER_NOWALLKICK) || (engine.isRotateCountExceed() == false)) )
				{
					// Wallkickを試みる
					boolean allowUpward = (engine.ruleopt.rotateMaxUpwardWallkick < 0) || (engine.nowUpwardWallkickCount < engine.ruleopt.rotateMaxUpwardWallkick);
					WallkickResult kick = engine.wallkick.executeWallkick(engine.nowPieceX, engine.nowPieceY, move, engine.nowPieceObject.direction, rt,
							allowUpward, engine.nowPieceObject, engine.field, engine.ctrl);

					if(kick != null) {
						rotated = true;
						engine.kickused = true;
						engine.nowWallkickCount++;
						if(kick.isUpward()) engine.nowUpwardWallkickCount++;
						engine.nowPieceObject.direction = kick.direction;
						engine.nowPieceObject.updateConnectData();
						engine.nowPieceX += kick.offsetX;
						engine.nowPieceY += kick.offsetY;

						if(engine.ruleopt.lockresetWallkick && !engine.isRotateCountExceed()) {
							engine.lockDelayNow = 0;
							engine.nowPieceObject.setDarkness(0f);
						}
					}
				}

				// Domino Quick Turn
				if(!rotated && engine.dominoQuickTurn && (engine.nowPieceObject.id == Piece.PIECE_I2) && (engine.nowPieceRotateFailCount >= 1)) {
					rt = engine.getRotateDirection(2);
					rotated = true;
					engine.nowPieceObject.direction = rt;
					engine.nowPieceObject.updateConnectData();
					engine.nowPieceRotateFailCount = 0;

					if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, rt, engine.field) == true) {
						engine.nowPieceY--;
					} else if(onGroundBeforeRotate) {
						engine.nowPieceY++;
					}
				}

				if(rotated == true) {
					// rotation成功
					engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);

					if((engine.ruleopt.lockresetRotate == true) && (engine.isRotateCountExceed() == false)) {
						engine.lockDelayNow = 0;
						engine.nowPieceObject.setDarkness(0f);
					}

					if(onGroundBeforeRotate) {
						engine.extendedRotateCount++;
						engine.lastmove = GameEngine.LASTMOVE_ROTATE_GROUND;
					} else {
						engine.lastmove = GameEngine.LASTMOVE_ROTATE_AIR;
					}

					if(engine.initialRotateDirection == 0) {
						engine.playSE("rotate");
					}

					engine.nowPieceRotateCount++;
					if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceRotate++;
				} else {
					// rotation失敗
					engine.playSE("rotfail");
					engine.nowPieceRotateFailCount++;
				}
			}
			engine.initialRotateDirection = 0;

			// game over check
			if((engine.statc[0] == 0) && (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field) == true)) {
				// Blockの出現位置を上にずらすことができる場合はそうする
				for(int i = 0; i < engine.ruleopt.pieceEnterMaxDistanceY; i++) {
					if(engine.nowPieceObject.big) engine.nowPieceY -= 2;
					else engine.nowPieceY--;

					if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field) == false) {
						engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);
						break;
					}
				}

				// 死亡
				if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field) == true) {
					engine.nowPieceObject.placeToField(engine.nowPieceX, engine.nowPieceY, engine.field);
					engine.nowPieceObject = null;
					engine.stat = engine.STAT_GAMEOVER;
					if((engine.ending == 2) && (engine.staffrollNoDeath)) engine.stat = GameEngine.STAT_NOTHING;
					engine.resetStatc();
					return true;
				}
			}

		}

		int move = 0;
		boolean sidemoveflag = false;	// この frame に横移動したらtrue

		if((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame == true)) {
			// 横移動
			boolean onGroundBeforeMove = engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field);

			move = moveDirection;

			if (engine.statc[0] == 0 && engine.delayCancel) {
				if (engine.delayCancelMoveLeft) move = -1;
				if (engine.delayCancelMoveRight) move = 1;
				engine.dasCount = 0;
				// delayCancel = false;
				engine.delayCancelMoveLeft = false;
				engine.delayCancelMoveRight = false;
			} else if (engine.statc[0] == 1 && engine.delayCancel && (engine.dasCount < engine.getDAS())) {
				move = 0;
				engine.delayCancel = false;
			}

			if(move != 0) sidemoveflag = true;

			if(engine.big && engine.bigmove) move *= 2;

			if((move != 0) && (engine.dasCount == 0)) engine.shiftLock = 0;

			if( (move != 0) && ((engine.dasCount == 0) || (engine.dasCount >= engine.getDAS())) ) {
				engine.shiftLock &= engine.ctrl.getButtonBit();

				if(engine.shiftLock == 0) {
					if( (engine.dasSpeedCount >= engine.getDASDelay()) || (engine.dasCount == 0) ) {
						if(engine.dasCount > 0) engine.dasSpeedCount = 1;

						if(engine.nowPieceObject.checkCollision(engine.nowPieceX + move, engine.nowPieceY, engine.field) == false) {
							engine.nowPieceX += move;

							if((engine.getDASDelay() == 0) && (engine.dasCount > 0) && (engine.nowPieceObject.checkCollision(engine.nowPieceX + move, engine.nowPieceY, engine.field) == false)) {
								if(!engine.dasInstant) engine.playSE("move");
								engine.dasRepeat = true;
								engine.dasInstant = true;
							}

							//log.debug("Successful movement: move="+move);

							if((engine.ruleopt.lockresetMove == true) && (engine.isMoveCountExceed() == false)) {
								engine.lockDelayNow = 0;
								engine.nowPieceObject.setDarkness(0f);
							}

							engine.nowPieceMoveCount++;
							if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceMove++;
							engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);

							if(onGroundBeforeMove) {
								engine.extendedMoveCount++;
								engine.lastmove = GameEngine.LASTMOVE_SLIDE_GROUND;
							} else {
								engine.lastmove = GameEngine.LASTMOVE_SLIDE_AIR;
							}

							if(!engine.dasInstant) engine.playSE("move");

						} else if (engine.ruleopt.dasChargeOnBlockedMove) {
							engine.dasCount = engine.getDAS();
							engine.dasSpeedCount = engine.getDASDelay();
						}
					} else {
						engine.dasSpeedCount++;
					}
				}
			}

			// Hard drop
			if( (engine.ctrl.isPress(engine.getUp()) == true) &&
					(engine.harddropContinuousUse == false) &&
					(engine.ruleopt.harddropEnable == true) &&
					((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
					((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
					(engine.nowPieceY < engine.nowPieceBottomY) )
			{
				engine.harddropFall += engine.nowPieceBottomY - engine.nowPieceY;

				if(engine.nowPieceY != engine.nowPieceBottomY) {
					engine.nowPieceY = engine.nowPieceBottomY;
					engine.playSE("harddrop");
				}

				if(owner.mode != null) owner.mode.afterHardDropFall(engine, playerID, engine.harddropFall);
				owner.receiver.afterHardDropFall(engine, playerID, engine.harddropFall);

				engine.lastmove = GameEngine.LASTMOVE_FALL_SELF;
				if(engine.ruleopt.lockresetFall == true) {
					engine.lockDelayNow = 0;
					engine.nowPieceObject.setDarkness(0f);
					engine.extendedMoveCount = 0;
					engine.extendedRotateCount = 0;
				}
			}

			if(!engine.ruleopt.softdropGravitySpeedLimit || (engine.ruleopt.softdropSpeed < 1.0f)) {
				// Old Soft Drop codes
				if( (engine.ctrl.isPress(engine.getDown()) == true) &&
						(engine.softdropContinuousUse == false) &&
						(engine.ruleopt.softdropEnable == true) &&
						((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
						((engine.ruleopt.moveUpAndDown == true) || (updown == false)) )
				{
					if((engine.ruleopt.softdropMultiplyNativeSpeed == true) || (engine.speed.denominator <= 0))
						engine.gcount += (int)(engine.speed.gravity * engine.ruleopt.softdropSpeed);
					else
						engine.gcount += (int)(engine.speed.denominator * engine.ruleopt.softdropSpeed);

					softdropUsed = true;
				}
			} else {
				// New Soft Drop codes
				if( engine.ctrl.isPress(engine.getDown()) && !engine.softdropContinuousUse &&
						engine.ruleopt.softdropEnable && (engine.isDiagonalMoveEnabled() || !sidemoveflag) &&
						(engine.ruleopt.moveUpAndDown || !updown) &&
						(engine.ruleopt.softdropMultiplyNativeSpeed || (engine.speed.gravity < (int)(engine.speed.denominator * engine.ruleopt.softdropSpeed))) )
				{
					if((engine.ruleopt.softdropMultiplyNativeSpeed == true) || (engine.speed.denominator <= 0)) {
						// gcount += (int)(speed.gravity * ruleopt.softdropSpeed);
						engine.gcount = (int)(engine.speed.gravity * engine.ruleopt.softdropSpeed);
					} else {
						// gcount += (int)(speed.denominator * ruleopt.softdropSpeed);
						engine.gcount = (int)(engine.speed.denominator * engine.ruleopt.softdropSpeed);
					}

					softdropUsed = true;
				} else {
					// 落下
					// This prevents soft drop from adding to the gravity speed.
					engine.gcount += engine.speed.gravity;
				}
			}

			if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceActiveTime++;
		}

		if(!engine.ruleopt.softdropGravitySpeedLimit || (engine.ruleopt.softdropSpeed < 1.0f))
			engine.gcount += engine.speed.gravity;	// Part of Old Soft Drop

		while((engine.gcount >= engine.speed.denominator) || (engine.speed.gravity < 0)) {
			if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field) == false) {
				if(engine.speed.gravity >= 0) engine.gcount -= engine.speed.denominator;
				engine.nowPieceY++;

				if(engine.ruleopt.lockresetFall == true) {
					engine.lockDelayNow = 0;
					engine.nowPieceObject.setDarkness(0f);
				}

				if((engine.lastmove != GameEngine.LASTMOVE_ROTATE_GROUND) && (engine.lastmove != GameEngine.LASTMOVE_SLIDE_GROUND) && (engine.lastmove != GameEngine.LASTMOVE_FALL_SELF)) {
					engine.extendedMoveCount = 0;
					engine.extendedRotateCount = 0;
				}

				if(softdropUsed == true) {
					engine.lastmove = GameEngine.LASTMOVE_FALL_SELF;
					engine.softdropFall++;
					softdropFallNow++;
					engine.playSE("softdrop");
				} else {
					engine.lastmove = GameEngine.LASTMOVE_FALL_AUTO;
				}
			} else {
				break;
			}
		}

		if(softdropFallNow > 0) {
			if(owner.mode != null) owner.mode.afterSoftDropFall(engine, playerID, softdropFallNow);
			owner.receiver.afterSoftDropFall(engine, playerID, softdropFallNow);
		}

		// 接地と固定
		if( (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field) == true) &&
				((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame == true)) )
		{
			if((engine.lockDelayNow == 0) && (engine.getLockDelay() > 0))
				engine.playSE("step");

			if(engine.lockDelayNow < engine.getLockDelay())
				engine.lockDelayNow++;

			if((engine.getLockDelay() >= 99) && (engine.lockDelayNow > 98))
				engine.lockDelayNow = 98;

			if(engine.lockDelayNow < engine.getLockDelay()) {
				if(engine.lockDelayNow >= engine.getLockDelay() - 1)
					engine.nowPieceObject.setDarkness(0.5f);
				else
					engine.nowPieceObject.setDarkness((engine.lockDelayNow * 7 / engine.getLockDelay()) * 0.05f);
			}

			if(engine.getLockDelay() != 0)
				engine.gcount = engine.speed.gravity;

			// trueになると即固定
			boolean instantlock = false;

			// Hard drop固定
			if( (engine.ctrl.isPress(engine.getUp()) == true) &&
					(engine.harddropContinuousUse == false) &&
					(engine.ruleopt.harddropEnable == true) &&
					((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
					((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
					(engine.ruleopt.harddropLock == true) )
			{
				engine.harddropContinuousUse = true;
				engine.manualLock = true;
				instantlock = true;
			}

			// Soft drop固定
			if( (engine.ctrl.isPress(engine.getDown()) == true) &&
					(engine.softdropContinuousUse == false) &&
					(engine.ruleopt.softdropEnable == true) &&
					((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
					((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
					(engine.ruleopt.softdropLock == true) )
			{
				engine.softdropContinuousUse = true;
				engine.manualLock = true;
				instantlock = true;
			}

			// 接地状態でソフドドロップ固定
			if( (engine.ctrl.isPush(engine.getDown()) == true) &&
					(engine.ruleopt.softdropEnable == true) &&
					((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
					((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
					(engine.ruleopt.softdropSurfaceLock == true) )
			{
				engine.softdropContinuousUse = true;
				engine.manualLock = true;
				instantlock = true;
			}

			if((engine.manualLock == true) && (engine.ruleopt.shiftLockEnable)) {
				// bit 1 and 2 are button_up and button_down currently
				engine.shiftLock = engine.ctrl.getButtonBit() & 3;
			}

			// 移動＆rotationcount制限超過
			if( (engine.ruleopt.lockresetLimitOver == RuleOptions.LOCKRESET_LIMIT_OVER_INSTANT) && (engine.isMoveCountExceed() || engine.isRotateCountExceed()) ) {
				instantlock = true;
			}

			// 接地即固定
			if( (engine.getLockDelay() == 0) && ((engine.gcount >= engine.speed.denominator) || (engine.speed.gravity < 0)) ) {
				instantlock = true;
			}

			// 固定
			if( ((engine.lockDelayNow >= engine.getLockDelay()) && (engine.getLockDelay() > 0)) || (instantlock == true) ) {
				if(engine.ruleopt.lockflash > 0) engine.nowPieceObject.setDarkness(-0.8f);

				/*if((lastmove == LASTMOVE_ROTATE_GROUND) && (tspinEnable == true)) {

					tspinmini = false;

					// T-Spin Mini判定

					if(!useAllSpinBonus) {
						if(spinCheckType == SPINTYPE_4POINT) {
							if(tspinminiType == TSPINMINI_TYPE_ROTATECHECK) {
								if(nowPieceObject.checkCollision(nowPieceX, nowPieceY, getRotateDirection(-1), field) &&
								   nowPieceObject.checkCollision(nowPieceX, nowPieceY, getRotateDirection( 1), field))
									tspinmini = true;
							} else if(tspinminiType == TSPINMINI_TYPE_WALLKICKFLAG) {
								tspinmini = kickused;
							}
						} else if(spinCheckType == SPINTYPE_IMMOBILE) {
							Field copyField = new Field(field);
							nowPieceObject.placeToField(nowPieceX, nowPieceY, copyField);
							if((copyField.checkLineNoFlag() == 1) && (kickused == true)) tspinmini = true;
						}
					}
				}*/

				// T-Spin判定
				if((engine.lastmove == GameEngine.LASTMOVE_ROTATE_GROUND) && (engine.tspinEnable == true)) {
					if(engine.useAllSpinBonus)
						setAllSpin(engine, engine.nowPieceX, engine.nowPieceY, engine.nowPieceObject, engine.field);
					else
						engine.setTSpin(engine.nowPieceX, engine.nowPieceY, engine.nowPieceObject, engine.field);
				}

				engine.nowPieceObject.setAttribute(Block.BLOCK_ATTRIBUTE_SELFPLACED, true);

				boolean partialLockOut = engine.nowPieceObject.isPartialLockOut(engine.nowPieceX, engine.nowPieceY, engine.field);
				boolean put = engine.nowPieceObject.placeToField(engine.nowPieceX, engine.nowPieceY, engine.field);

				engine.playSE("lock");

				engine.holdDisable = false;

				if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceLocked++;

				if (engine.clearMode == GameEngine.CLEAR_LINE)
					engine.lineClearing = engine.field.checkLineNoFlag();
				else if (engine.clearMode == GameEngine.CLEAR_COLOR)
					engine.lineClearing = engine.field.checkColor(engine.colorClearSize, false, engine.garbageColorClear, engine.gemSameColor, engine.ignoreHidden);
				else if (engine.clearMode == GameEngine.CLEAR_LINE_COLOR)
					engine.lineClearing = engine.field.checkLineColor(engine.colorClearSize, false, engine.lineColorDiagonals, engine.gemSameColor);
				else if (engine.clearMode == GameEngine.CLEAR_GEM_COLOR)
					engine.lineClearing = engine.field.gemColorCheck(engine.colorClearSize, false, engine.garbageColorClear, engine.ignoreHidden);
				engine.chain = 0;
				engine.lineGravityTotalLines = 0;

				if(engine.lineClearing == 0) {
					engine.combo = 0;

					if(engine.tspin) {
						engine.playSE("tspin0");

						if((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
							if(engine.tspinmini) engine.statistics.totalTSpinZeroMini++;
							else engine.statistics.totalTSpinZero++;
						}
					}

					if(owner.mode != null) owner.mode.calcScore(engine, playerID, engine.lineClearing);
					owner.receiver.calcScore(engine, playerID, engine.lineClearing);
				}

				if(owner.mode != null) owner.mode.pieceLocked(engine, playerID, engine.lineClearing);
				owner.receiver.pieceLocked(engine, playerID, engine.lineClearing);

				engine.dasRepeat = false;
				engine.dasInstant = false;

				// Next 処理を決める(Mode 側でステータスを弄っている場合は何もしない)
				if((engine.stat == GameEngine.STAT_MOVE) || (engine.versionMajor <= 6.3f)) {
					engine.resetStatc();

					if((engine.ending == 1) && (engine.versionMajor >= 6.6f) && (engine.versionMinorOld >= 0.1f)) {
						// Ending
						engine.stat = GameEngine.STAT_ENDINGSTART;
					} else if( (!put && engine.ruleopt.fieldLockoutDeath) || (partialLockOut && engine.ruleopt.fieldPartialLockoutDeath) ) {
						// 画面外に置いて死亡
						engine.stat = GameEngine.STAT_GAMEOVER;
						if((engine.ending == 2) && (engine.staffrollNoDeath)) engine.stat = engine.STAT_NOTHING;
					} else if ((engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE || engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE_SLOW)
							&& !engine.connectBlocks) {
						engine.stat = GameEngine.STAT_LINECLEAR;
						engine.statc[0] = engine.getLineDelay();
						engine.statLineClear();
					} else if( (engine.lineClearing > 0) && ((engine.ruleopt.lockflash <= 0) || (!engine.ruleopt.lockflashBeforeLineClear)) ) {
						// Line clear
						engine.stat = GameEngine.STAT_LINECLEAR;
						engine.statLineClear();
					} else if( ((engine.getARE() > 0) || (engine.lagARE) || (engine.ruleopt.lockflashBeforeLineClear)) &&
							(engine.ruleopt.lockflash > 0) && (engine.ruleopt.lockflashOnlyFrame) )
					{
						// AREあり (光あり）
						engine.stat = GameEngine.STAT_LOCKFLASH;
					} else if((engine.getARE() > 0) || (engine.lagARE)) {
						// AREあり (光なし）
						engine.statc[1] = engine.getARE();
						engine.stat = GameEngine.STAT_ARE;
					} else if(engine.interruptItemNumber != GameEngine.INTERRUPTITEM_NONE) {
						// 中断効果のあるアイテム処理
						engine.nowPieceObject = null;
						engine.interruptItemPreviousStat = GameEngine.STAT_MOVE;
						engine.stat = GameEngine.STAT_INTERRUPTITEM;
					} else {
						// AREなし
						engine.stat = GameEngine.STAT_MOVE;
						if(engine.ruleopt.moveFirstFrame == false) engine.statMove();
					}
				}
				return true;
			}
		}

		// 横溜め
		if((engine.statc[0] > 0) || (engine.ruleopt.dasInMoveFirstFrame)) {
			if( (moveDirection != 0) && (moveDirection == engine.dasDirection) && ((engine.dasCount < engine.getDAS()) || (engine.getDAS() <= 0)) ) {
				engine.dasCount++;
			}
		}

		engine.statc[0]++;

		return true;
	}

	/**
	 * Spin判定(全スピンルールのとき用)
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param piece Current Blockピース
	 * @param fld field
	 */
	public void setAllSpin(GameEngine engine, int x, int y, Piece piece, Field fld) {
		engine.tspin = false;
		engine.tspinmini = false;
		engine.tspinez = false;

		if(piece == null) return;
		if(!engine.tspinAllowKick && engine.kickused) return;

		if(spinCheckType == GameEngine.SPINTYPE_4POINT) {

			int offsetX = engine.ruleopt.pieceOffsetX[piece.id][piece.direction];
			int offsetY = engine.ruleopt.pieceOffsetY[piece.id][piece.direction];

			for(int i = 0; i < Piece.SPINBONUSDATA_HIGH_X[piece.id][piece.direction].length / 2; i++) {
				boolean isHighSpot1 = false;
				boolean isHighSpot2 = false;
				boolean isLowSpot1 = false;
				boolean isLowSpot2 = false;

				if (piece.big) {
					if(!fld.getBlockEmptyF(
							x + Piece.SPINBONUSDATA_HIGH_X[piece.id][piece.direction][i * 2 + 0] * 2 + offsetX,
							y + Piece.SPINBONUSDATA_HIGH_Y[piece.id][piece.direction][i * 2 + 0] * 2 + offsetY))
					{
						isHighSpot1 = true;
					}
					if(!fld.getBlockEmptyF(
							x + Piece.SPINBONUSDATA_HIGH_X[piece.id][piece.direction][i * 2 + 1] * 2 + offsetX,
							y + Piece.SPINBONUSDATA_HIGH_Y[piece.id][piece.direction][i * 2 + 1] * 2 + offsetY))
					{
						isHighSpot2 = true;
					}
					if(!fld.getBlockEmptyF(
							x + Piece.SPINBONUSDATA_LOW_X[piece.id][piece.direction][i * 2 + 0] * 2 + offsetX,
							y + Piece.SPINBONUSDATA_LOW_Y[piece.id][piece.direction][i * 2 + 0] * 2 + offsetY))
					{
						isLowSpot1 = true;
					}
					if(!fld.getBlockEmptyF(
							x + Piece.SPINBONUSDATA_LOW_X[piece.id][piece.direction][i * 2 + 1] * 2 + offsetX,
							y + Piece.SPINBONUSDATA_LOW_Y[piece.id][piece.direction][i * 2 + 1] * 2 + offsetY))
					{
						isLowSpot2 = true;
					}
				} else {
					if(!fld.getBlockEmptyF(
							x + Piece.SPINBONUSDATA_HIGH_X[piece.id][piece.direction][i * 2 + 0] + offsetX,
							y + Piece.SPINBONUSDATA_HIGH_Y[piece.id][piece.direction][i * 2 + 0] + offsetY))
					{
						isHighSpot1 = true;
					}
					if(!fld.getBlockEmptyF(
							x + Piece.SPINBONUSDATA_HIGH_X[piece.id][piece.direction][i * 2 + 1] + offsetX,
							y + Piece.SPINBONUSDATA_HIGH_Y[piece.id][piece.direction][i * 2 + 1] + offsetY))
					{
						isHighSpot2 = true;
					}
					if(!fld.getBlockEmptyF(
							x + Piece.SPINBONUSDATA_LOW_X[piece.id][piece.direction][i * 2 + 0] + offsetX,
							y + Piece.SPINBONUSDATA_LOW_Y[piece.id][piece.direction][i * 2 + 0] + offsetY))
					{
						isLowSpot1 = true;
					}
					if(!fld.getBlockEmptyF(
							x + Piece.SPINBONUSDATA_LOW_X[piece.id][piece.direction][i * 2 + 1] + offsetX,
							y + Piece.SPINBONUSDATA_LOW_Y[piece.id][piece.direction][i * 2 + 1] + offsetY))
					{
						isLowSpot2 = true;
					}
				}

				//log.debug(isHighSpot1 + "," + isHighSpot2 + "," + isLowSpot1 + "," + isLowSpot2);

				if(isHighSpot1 && isHighSpot2 && (isLowSpot1 || isLowSpot2)) {
					engine.tspin = true;
				} else if(!engine.tspin && isLowSpot1 && isLowSpot2 && (isHighSpot1 || isHighSpot2)) {
					engine.tspin = true;
					engine.tspinmini = true;
				}
			}
		} else if(spinCheckType == GameEngine.SPINTYPE_IMMOBILE) {
			//int y2 = y - 1;
			//log.debug(x + "," + y2 + ":" + piece.checkCollision(x, y2, fld));

			if( piece.checkCollision(x, y - 1, fld) &&
					piece.checkCollision(x + 1, y, fld) &&
					piece.checkCollision(x - 1, y, fld) ) {
				engine.tspin = true;
				Field copyField = new Field(fld);
				piece.placeToField(x, y, copyField);
				if((piece.getHeight() + 1 != copyField.checkLineNoFlag()) && (engine.kickused == true)) engine.tspinmini = true;
				//if((copyField.checkLineNoFlag() == 1) && (kickused == true)) tspinmini = true;
			} else if((tspinEnableEZ) && (engine.kickused == true)) {
				engine.tspin = true;
				engine.tspinez = true;
			}
		}
	}
	// endregion

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
				if (lines == 3) {
					if(engine.b2b) {
						pts += 2400 * (engine.statistics.level + 1);
					} else {
						pts += 1600 * (engine.statistics.level + 1);
					}
				} else {
					pts = (int)(1.5 * getLineScore(lines));
					if (engine.b2b) pts *= 1.5;
				}

				lastevent = EVENT_TSPIN_TRIPLE;

				if (lines >= 4) engine.playSE("tspin3");
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
				if (lines == 4) {
					if(engine.b2b) {
						pts += 1200 * (engine.statistics.level + 1);
					} else {
						pts += 800 * (engine.statistics.level + 1);
					}
				} else {
					pts = getLineScore(lines);
					if (engine.b2b) pts *= 1.5;
				}

				lastevent = EVENT_FOUR;

				if (lines >= 5) engine.playSE("erase4");
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
			lastscore = pts;
			lastpiece = engine.nowPieceObject.id;
			scgettime = 0;
			if(lines >= 1) engine.statistics.scoreFromLineClear += pts;
			else engine.statistics.scoreFromOtherBonus += pts;
			previousScore = engine.statistics.score;
			engine.statistics.score += pts;
		}

		if (setBigOrNot(engine, engine.getNextObject(engine.nextPieceCount), pieceIDQueue.get(0))) {
			appendQueue(true);
		}

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
		if((owner.replayMode == false) && (big == false) && (engine.ai == null)) {
			updateRanking(engine.statistics.score, engine.statistics.lines, engine.statistics.time, goaltype);

			if (playerProperties.isLoggedIn()) {
				prop.setProperty("singledeath.playerName", playerProperties.getNameDisplay());
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

	private void loadSetting(CustomProperties prop) {
		startlevel = prop.getProperty("singledeath.startlevel", 0);
		tspinEnableType = prop.getProperty("singledeath.tspinEnableType", 1);
		enableTSpin = prop.getProperty("singledeath.enableTSpin", true);
		enableTSpinKick = prop.getProperty("singledeath.enableTSpinKick", true);
		spinCheckType = prop.getProperty("singledeath.spinCheckType", 0);
		tspinEnableEZ = prop.getProperty("singledeath.tspinEnableEZ", false);
		enableB2B = prop.getProperty("singledeath.enableB2B", true);
		enableCombo = prop.getProperty("singledeath.enableCombo", true);
		goaltype = prop.getProperty("singledeath.gametype", 0);
		// big = prop.getProperty("singledeath.big", false);
		version = prop.getProperty("singledeath.version", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("singledeath.startlevel", startlevel);
		prop.setProperty("singledeath.tspinEnableType", tspinEnableType);
		prop.setProperty("singledeath.enableTSpin", enableTSpin);
		prop.setProperty("singledeath.enableTSpinKick", enableTSpinKick);
		prop.setProperty("singledeath.spinCheckType", spinCheckType);
		prop.setProperty("singledeath.tspinEnableEZ", tspinEnableEZ);
		prop.setProperty("singledeath.enableB2B", enableB2B);
		prop.setProperty("singledeath.enableCombo", enableCombo);
		prop.setProperty("singledeath.gametype", goaltype);
		// prop.setProperty("singledeath.big", big);
		prop.setProperty("singledeath.version", version);
	}

	private void loadSettingPlayer(ProfileProperties prop) {
		if (!prop.isLoggedIn()) return;
		startlevel = prop.getProperty("singledeath.startlevel", 0);
		tspinEnableType = prop.getProperty("singledeath.tspinEnableType", 1);
		enableTSpin = prop.getProperty("singledeath.enableTSpin", true);
		enableTSpinKick = prop.getProperty("singledeath.enableTSpinKick", true);
		spinCheckType = prop.getProperty("singledeath.spinCheckType", 0);
		tspinEnableEZ = prop.getProperty("singledeath.tspinEnableEZ", false);
		enableB2B = prop.getProperty("singledeath.enableB2B", true);
		enableCombo = prop.getProperty("singledeath.enableCombo", true);
		goaltype = prop.getProperty("singledeath.gametype", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSettingPlayer(ProfileProperties prop) {
		if (!prop.isLoggedIn()) return;
		prop.setProperty("singledeath.startlevel", startlevel);
		prop.setProperty("singledeath.tspinEnableType", tspinEnableType);
		prop.setProperty("singledeath.enableTSpin", enableTSpin);
		prop.setProperty("singledeath.enableTSpinKick", enableTSpinKick);
		prop.setProperty("singledeath.spinCheckType", spinCheckType);
		prop.setProperty("singledeath.tspinEnableEZ", tspinEnableEZ);
		prop.setProperty("singledeath.enableB2B", enableB2B);
		prop.setProperty("singledeath.enableCombo", enableCombo);
		prop.setProperty("singledeath.gametype", goaltype);
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
				rankingScore[j][i] = prop.getProperty("singledeath.ranking." + ruleName + "." + j + ".score." + i, 0);
				rankingLines[j][i] = prop.getProperty("singledeath.ranking." + ruleName + "." + j + ".lines." + i, 0);
				rankingTime[j][i] = prop.getProperty("singledeath.ranking." + ruleName + "." + j + ".time." + i, 0);
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
				prop.setProperty("singledeath.ranking." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
				prop.setProperty("singledeath.ranking." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
				prop.setProperty("singledeath.ranking." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
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
				rankingScorePlayer[j][i] = prop.getProperty("singledeath.ranking." + ruleName + "." + j + ".score." + i, 0);
				rankingLinesPlayer[j][i] = prop.getProperty("singledeath.ranking." + ruleName + "." + j + ".lines." + i, 0);
				rankingTimePlayer[j][i] = prop.getProperty("singledeath.ranking." + ruleName + "." + j + ".time." + i, 0);
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
				prop.setProperty("singledeath.ranking." + ruleName + "." + j + ".score." + i, rankingScorePlayer[j][i]);
				prop.setProperty("singledeath.ranking." + ruleName + "." + j + ".lines." + i, rankingLinesPlayer[j][i]);
				prop.setProperty("singledeath.ranking." + ruleName + "." + j + ".time." + i, rankingTimePlayer[j][i]);
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

	/*
	 * Gets score given lines scanned.
	 * Follows the 100, 300, 500, 800... pattern.
	 *
	 * NOTE: int truncating is usefulAF ~0xFC
	 *
	 * @param lines Number of Lines
	 */
	private int getLineScore(int lines) {
		int x = 1;
		if (lines == 0) x = 0;

		if (lines >= 2) {
			for (int i = 2; i <= lines; i++) {
				x += (i / 2) + 1;
			}
		}

		return x * 100;
	}
}
