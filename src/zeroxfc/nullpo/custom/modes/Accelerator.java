package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.RendererExtension;

import java.util.ArrayList;

public class Accelerator extends MarathonModeBase {
	// Speed check time.
	private static final int bufferTime = 600;
	
	// Goal coefficient
	private static final int GOAL_COEFFICIENT = 20;
	
	// Goals
	private static final int[] GOALS = { 300, 400, -1 };
	
	/** Line counts when BGM changes occur */
	private static final int tableBGMChange[] = { 100, 200, 300, 400, -1};
	
	// Maximum bonusbar charge
	private static final double MAX_BONUS_CHARGE = 360;
	
	// Current bonusbar charge
	private double currentBonusCharge;
	
	// is bonus active?
	private boolean bonusActive;
	
	// Piece time array
	private int[] timeSincePlacementArray;
	
	// Current speed in the past 3 seconds.
	private double currentSpeed;
	
	// Current progress
	private double progressCoeffeicient;
	
	// score before increase
	private double scoreBeforeIncrease;
	
	/** Rankings' pts counts */
	public double[][] rankingLines;

	/** The good hard drop effect */
	private ArrayList<int[]> pCoordList;
	private Piece cPiece;
	
	/*
	 * Get mode name.
	 */
	@Override
	public String getName() {
		return "ACCELERATOR";
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
		
		progressCoeffeicient = 0.0;
		currentBonusCharge = 0.0;
		bonusActive = false;
		scoreBeforeIncrease = 0;

		pCoordList = new ArrayList<>();
		cPiece = null;

		rankingRank = -1;
		rankingScore = new int[RANKING_TYPE][RANKING_MAX];
		rankingLines = new double[RANKING_TYPE][RANKING_MAX];
		rankingTime = new int[RANKING_TYPE][RANKING_MAX];

		netPlayerInit(engine, playerID);

		if(owner.replayMode == false) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
			version = CURRENT_VERSION;
		} else {
			loadSetting(owner.replayProp);
			if((version == 0) && (owner.replayProp.getProperty("accelerator.endless", false) == true)) goaltype = 2;

			// NET: Load name
			netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}
		
		timeSincePlacementArray = new int[301];
		for (int i = 0; i < timeSincePlacementArray.length; i++) {
			timeSincePlacementArray[i] = -1;
		}
		
		currentSpeed = 0;

		engine.owner.backgroundStatus.bg = startlevel;
		engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
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
			int change = updateCursor(engine, 8, playerID);

			if(change != 0) {
				engine.playSE("change");

				switch(engine.statc[2]) {
				case 0:
					startlevel += change;
					if(GOALS[goaltype] >= 0) {
						if(startlevel < 0) startlevel = (GOALS[goaltype] - 1) / 20;
						if(startlevel > (GOALS[goaltype] - 1) / 20) startlevel = 0;
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

					if((startlevel > (GOALS[goaltype] - 1) / 20) && (GOALS[goaltype] >= 0)) {
						startlevel = (GOALS[goaltype] - 1) / 20;
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

			// Confirm
			if(engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
				engine.playSE("decide");
				saveSetting(owner.modeConfig);
				receiver.saveModeConfig(owner.modeConfig);

				// NET: Signal start of the game
				if(netIsNetPlay) netLobby.netPlayerClient.send("start1p\n");

				return false;
			}

			// Cancel
			if(engine.ctrl.isPush(Controller.BUTTON_B) && !netIsNetPlay) {
				engine.quitflag = true;
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
					"GOAL",  (goaltype == 2) ? "ENDLESS" : GOALS[goaltype] + " PTS",
					"BIG", GeneralUtil.getONorOFF(big));
		}
	}
	
	/*
	 * Called for initialization during "Ready" screen
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.speed.are = 0;
		
		currentSpeed = 0;
		timeSincePlacementArray = new int[301];
		for (int i = 0; i < timeSincePlacementArray.length; i++) {
			timeSincePlacementArray[i] = -1;
		}
		
		progressCoeffeicient = 0.0;
		currentBonusCharge = 0.0;
		bonusActive = false;
		
		engine.statistics.level = startlevel;
		engine.statistics.levelDispAdd = 1;
		engine.b2bEnable = enableB2B;
		if(enableCombo == true) {
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
		scoreBeforeIncrease = 0;

		setSpeed(engine);

		if(netIsWatch) {
			owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
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
	 * Render score
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;

		receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_GREEN);

		if(GOALS[goaltype] == -1) {
			receiver.drawScoreFont(engine, playerID, 0, 1, "(ENDLESS GAME)", EventReceiver.COLOR_GREEN);
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 1, "(" + GOALS[goaltype] + " SPEED GOAL GAME)", EventReceiver.COLOR_GREEN);
		}

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false)) ) {
			if((owner.replayMode == false) && (big == false) && (engine.ai == null)) {
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
				int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
				receiver.drawScoreFont(engine, playerID, 3, topY-1, "SCORE  SPDPT TIME", EventReceiver.COLOR_BLUE, scale);

				for(int i = 0; i < RANKING_MAX; i++) {
					receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
					String s = String.valueOf(rankingScore[goaltype][i]);
					receiver.drawScoreFont(engine, playerID, (s.length() > 6 && receiver.getNextDisplayType() != 2) ? 6 : 3, (s.length() > 6 && receiver.getNextDisplayType() != 2) ? (topY+i) * 2 : (topY+i), s, (i == rankingRank), (s.length() > 6 && receiver.getNextDisplayType() != 2) ? scale * 0.5f : scale);
					receiver.drawScoreFont(engine, playerID, 10, topY+i, String.format("%.1f", rankingLines[goaltype][i]), (i == rankingRank), scale);
					receiver.drawScoreFont(engine, playerID, 16, topY+i, GeneralUtil.getTime(rankingTime[goaltype][i]), (i == rankingRank), scale);
				}
			}
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 3, "SCORE", EventReceiver.COLOR_BLUE);
			String strScore;
			if((lastscore == 0) || (scgettime >= 120)) {
				strScore = String.valueOf(engine.statistics.score);
			} else {
				strScore = String.valueOf((int)Interpolation.lerp((int)scoreBeforeIncrease, (int)engine.statistics.score, (scgettime / 120.0)) + "(+" + String.valueOf(lastscore) + ")");
			}
			receiver.drawScoreFont(engine, playerID, 0, 4, strScore);

			receiver.drawScoreFont(engine, playerID, 0, 6, "SPEED GOAL", EventReceiver.COLOR_BLUE);
			if((engine.statistics.level >= 19) && (GOALS[goaltype] < 0))
				receiver.drawScoreFont(engine, playerID, 0, 7, String.format("%.1f", progressCoeffeicient) + "");
			else
				receiver.drawScoreFont(engine, playerID, 0, 7, String.format("%.1f", progressCoeffeicient) + "/" + ((engine.statistics.level + 1) * GOAL_COEFFICIENT) + ".0");

			receiver.drawScoreFont(engine, playerID, 0, 9, "LEVEL", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(engine.statistics.level + 1));

			receiver.drawScoreFont(engine, playerID, 0, 12, "TIME", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 13, GeneralUtil.getTime(engine.statistics.time));
			
			int speedColor = EventReceiver.COLOR_RED;
			if(currentSpeed >= 0.5) speedColor = EventReceiver.COLOR_ORANGE;
			if(currentSpeed >= 1) speedColor = EventReceiver.COLOR_YELLOW;
			if(currentSpeed >= 2) speedColor = EventReceiver.COLOR_GREEN;
			
			receiver.drawScoreFont(engine, playerID, 0, 15, "SPEED (/10S)", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 16, String.format("%.1f", currentSpeed), speedColor);
			
			//int speedBar = (int)((currentBonusCharge / MAX_BONUS_CHARGE) * 40);
			double percentage = (currentBonusCharge / MAX_BONUS_CHARGE);
			
			receiver.drawScoreFont(engine, playerID, 0, 18, "BONUS", EventReceiver.COLOR_BLUE);
			if (bonusActive) receiver.drawScoreFont(engine, playerID, 6, 18, "(ACTIVE!)", EventReceiver.COLOR_GREEN);
			receiver.drawScoreFont(engine, playerID, 0, 19, String.format("%.2f%%", percentage * 100), bonusActive ? ((engine.statistics.time % 2 == 0) ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_YELLOW) : EventReceiver.COLOR_WHITE, 1f + (float)percentage);

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
	
	/*
	 * Calculate score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Get speed
		addZero();
		
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
		
		// Add to goal
		if (lines >= 1) {
			progressCoeffeicient += currentSpeed;
		}
		
		double usedSpeed = (currentSpeed <= 6.5) ? currentSpeed : 6.5;
		double usedMultiplier = (currentSpeed >= 2.0) ? 1.5 : 0.5;
		double finalMultiplier = usedMultiplier + usedSpeed;
		if(bonusActive) finalMultiplier *= 4;
		
		pts *= finalMultiplier;

		// Add to score
		if(pts > 0) {
			lastscore = pts;
			lastpiece = engine.nowPieceObject.id;
			scgettime = 0;
			scoreBeforeIncrease = engine.statistics.score;
			if(lines >= 1) engine.statistics.scoreFromLineClear += pts;
			else engine.statistics.scoreFromOtherBonus += pts;
			engine.statistics.score += pts;
		}

		// BGM fade-out effects and BGM changes
		if(tableBGMChange[bgmlv] != -1) {
			if(progressCoeffeicient >= tableBGMChange[bgmlv] - 5) owner.bgmStatus.fadesw = true;

			if( (progressCoeffeicient >= tableBGMChange[bgmlv]) &&
				((progressCoeffeicient < GOALS[goaltype]) || (GOALS[goaltype] < 0)) )
			{
				bgmlv++;
				owner.bgmStatus.bgm = bgmlv;
				owner.bgmStatus.fadesw = false;
			}
		}

		if((progressCoeffeicient >= GOALS[goaltype]) && (GOALS[goaltype] >= 0)) {
			// Ending
			engine.ending = 1;
			engine.gameEnded();
		} else if((progressCoeffeicient >= (engine.statistics.level + 1) * GOAL_COEFFICIENT) && (engine.statistics.level < 19)) {
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
	 * Called during movetime
	 */
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		currentSpeed = getSpeed(engine);
		double usedSpeed = (currentSpeed <= 6.5) ? currentSpeed : 6.5;
		
		for (int i = 0; i < timeSincePlacementArray.length; i++) {
			if (timeSincePlacementArray[i] >= bufferTime) {
				timeSincePlacementArray[i] = -1;
			} else if (timeSincePlacementArray[i] != -1) {
				timeSincePlacementArray[i]++;
			}
		}
		
		if (usedSpeed >= 2 && !bonusActive) {
			currentBonusCharge += (usedSpeed >= 2.5) ? 0.1 : 0.25;
		} else if (bonusActive) {
			currentBonusCharge -= 0.2;
			engine.nowPieceColorOverride = Block.BLOCK_COLOR_ORANGE;
			if (engine.nowPieceObject != null) {
				if (engine.nowPieceObject.getColors()[0] != Block.BLOCK_COLOR_ORANGE) {
					engine.nowPieceObject.setColor(Block.BLOCK_COLOR_ORANGE);
				}
			}
		}
		
		if (currentBonusCharge >= MAX_BONUS_CHARGE && !bonusActive) {
			currentBonusCharge = MAX_BONUS_CHARGE;
			bonusActive = true;
			engine.playSE("cool");
			engine.framecolor = GameEngine.FRAME_COLOR_RED;
		}
		
		if (bonusActive && currentBonusCharge <= 0) {
			currentBonusCharge = 0;
			bonusActive = false;
			engine.nowPieceColorOverride = -1;
			engine.playSE("bravo");
			engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
		}
		
		// Meter
		engine.meterValue = (int)(((usedSpeed / 0.25) * receiver.getMeterMax(engine)) / 10);
		engine.meterColor = GameEngine.METER_COLOR_RED;
		if(usedSpeed >= 0.5) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		if(usedSpeed >= 1) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		if(usedSpeed >= 2.5) engine.meterColor = GameEngine.METER_COLOR_GREEN;
		
		return false;
	}
	
	// Get current speed of play
	private double getSpeed(GameEngine engine) {
		int placed = 0;
		for (int i = 0; i < timeSincePlacementArray.length; i++) {
			if (timeSincePlacementArray[i] != -1) {
				placed++;
			}
		}
		
		int usedTime = (engine.statistics.time < bufferTime) ? engine.statistics.time : bufferTime;
		if(usedTime <= 0) usedTime = 1;
		double speedScore = (double)placed / ((double)usedTime / 60);
		return speedScore;
	}
	
	// place new 0 in placement array
	private void addZero() {
		for (int i = 0; i < timeSincePlacementArray.length; i++) {
			if (timeSincePlacementArray[i] == -1) {
				timeSincePlacementArray[i] = 0;
				break;
			}
		}
	}
	
	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSetting(CustomProperties prop) {
		startlevel = prop.getProperty("accelerator.startlevel", 0);
		tspinEnableType = prop.getProperty("accelerator.tspinEnableType", 1);
		enableTSpin = prop.getProperty("accelerator.enableTSpin", true);
		enableTSpinKick = prop.getProperty("accelerator.enableTSpinKick", true);
		spinCheckType = prop.getProperty("accelerator.spinCheckType", 0);
		tspinEnableEZ = prop.getProperty("accelerator.tspinEnableEZ", false);
		enableB2B = prop.getProperty("accelerator.enableB2B", true);
		enableCombo = prop.getProperty("accelerator.enableCombo", true);
		goaltype = prop.getProperty("accelerator.gametype", 0);
		big = prop.getProperty("accelerator.big", false);
		version = prop.getProperty("accelerator.version", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("accelerator.startlevel", startlevel);
		prop.setProperty("accelerator.tspinEnableType", tspinEnableType);
		prop.setProperty("accelerator.enableTSpin", enableTSpin);
		prop.setProperty("accelerator.enableTSpinKick", enableTSpinKick);
		prop.setProperty("accelerator.spinCheckType", spinCheckType);
		prop.setProperty("accelerator.tspinEnableEZ", tspinEnableEZ);
		prop.setProperty("accelerator.enableB2B", enableB2B);
		prop.setProperty("accelerator.enableCombo", enableCombo);
		prop.setProperty("accelerator.gametype", goaltype);
		prop.setProperty("accelerator.big", big);
		prop.setProperty("accelerator.version", version);
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
				rankingScore[j][i] = prop.getProperty("accelerator.ranking." + ruleName + "." + j + ".score." + i, 0);
				rankingLines[j][i] = prop.getProperty("accelerator.ranking." + ruleName + "." + j + ".lines." + i, 0.0);
				rankingTime[j][i] = prop.getProperty("accelerator.ranking." + ruleName + "." + j + ".time." + i, 0);
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
				prop.setProperty("accelerator.ranking." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
				prop.setProperty("accelerator.ranking." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
				prop.setProperty("accelerator.ranking." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
			}
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
			updateRanking(engine.statistics.score, progressCoeffeicient, engine.statistics.time, goaltype);

			if(rankingRank != -1) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				receiver.saveModeConfig(owner.modeConfig);
			}
		}
	}
	
	/**
	 * Update rankings
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 */
	private void updateRanking(int sc, double li, int time, int type) {
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
	}

	/**
	 * Calculate ranking position
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 * @return Position (-1 if unranked)
	 */
	private int checkRanking(int sc, double li, int time, int type) {
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

}
