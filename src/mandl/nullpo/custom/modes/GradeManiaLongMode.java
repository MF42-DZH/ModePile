package mandl.nullpo.custom.modes;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;

/**
 * GRADE MANIA LONG Mode
 * Like Grade Mania 2 or Grade Mania 3, but even longer!
 */
public class GradeManiaLongMode extends DummyMode {
	/** Current version */
	private static final int CURRENT_VERSION = 2;

	/** Gravity table */
	private static final int[] tableDenominator =
	{
		  4,   8,  12,  16,  24,  32,  48,  64,  80,  96, 112, 128, 144, 160, 176, 192, 224, 256,
		  4,  16,  32,  48,  64,  96, 128, 160, 192, 224, 256, 512, 768, 1024, 1280, -1
	};

	/** Gravity increase level */
	private static final int[] tableGravityChangeLevel =
	{
		 25,  50,  75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400, 450, 500,
		510, 520, 530, 540, 550, 560, 570, 580, 590, 600, 650, 700, 800, 900, 1000, 10000
	};

	/** ARE table */
	private static final int[] tableAre =
	{
		25, 25, 25, 25, 25,
		25, 25, 20, 20, 20,
		20, 20, 16, 16, 16,
		12, 12, 12,  6,  5,
		 4,  4,  4,  4,  4,
		 4,  4,  4,  4,  4,  5
	};

	/** ARE after line clear table */
	private static final int[] tableAreLine =
	{
		25, 25, 25, 25, 25,
		25, 25, 20, 20, 20,
		16, 16, 12, 12, 12,
		 6,  6,  6,  6,  5,
		 3,  3,  3,  3,  3,
		 3,  3,  3,  3,  3,  5
	};

	/** Line delay table */
	private static final int[] tableLineDelay =
	{
		40, 40, 40, 40, 40,
		40, 40, 35, 30, 25,
		16, 16, 12, 12, 12,
		 6,  6,  6,  6,  5,
		 3,  3,  3,  3,  3,
		 3,  3,  3,  3,  3,  5
	};

	/** Lock delay table */
	private static final int[] tableLockDelay =
	{
		30, 30, 30, 30, 30,
		30, 30, 30, 30, 30,
		30, 30, 30, 25, 25,
		20, 20, 20, 17, 17,
		15, 15, 13, 13, 12,
		12, 12, 10, 10,  8, 17
	};

	/** DAS table */
	private static final int[] tableDAS =
	{
		14, 14, 14, 14, 14,
		14, 14, 14, 14, 14,
		 8,  8,  8,  8,  8,
		 6,  6,  6,  6,  4,
		 4,  4,  4,  4,  4,
		 4,  4,  4,  4,  4,  4
	};

	/** Garbage line speed table */
	private static final int[] tableGarbage =
	{
		 0,  0,  0,  0,  0,
		 0,  0,  0,  0,  0,
		 0,  0,  0,  0,  0,
		20, 18, 16, 14, 12,
		10,  9,  9,  8,  8,
		 8,  8,  8,  8,  8,  0
	};

	/** BGM fadeout levels */
	private static final int[] tableBGMFadeout = { 485, 985,1480,1975,2470,-1};

	/** BGM change levels */
	private static final int[] tableBGMChange  = { 500,1000,1500,2000,2500,-1};

	/** Line clear時に入る段位 point */
	private static final int[][] tableGradePoint =
	{
		{10,10,10,10,10, 5, 5, 5, 5, 5, 2},
		{20,20,20,15,15,15,10,10,10,10,12},
		{40,30,30,30,20,20,20,15,15,15,13},
		{50,40,40,40,40,30,30,30,30,30,30}
	};

	/** 段位 pointのCombo bonus */
	private static final float[][] tableGradeComboBonus =
	{
		{1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f},
		{1.0f,1.2f,1.2f,1.4f,1.4f,1.4f,1.4f,1.5f,1.5f,2.0f},
		{1.0f,1.4f,1.5f,1.6f,1.7f,1.8f,1.9f,2.0f,2.1f,2.5f},
		{1.0f,1.5f,1.8f,2.0f,2.2f,2.3f,2.4f,2.5f,2.6f,3.0f}
	};

	/** Internal grades to raise displayed grade */
	private static final int[] tableGradeChange =
	{
		 1,  2,  3,  4,  5,  7,  9, 12, 15,
		18, 19, 20, 23, 25, 27, 29, 31, 33,
		35, 37, 40, 43, 46, 49, 52, 55, 59,
		64, 70, 77, 85, 95,107,122, -1, -1
	};

	/** Grade point decay time */
	private static final int[] tableGradeDecayRate =
	{
		125, 80, 80, 50, 45, 45, 45, 40, 40, 40, 40, 40, 30, 30, 30,
		 20, 20, 20, 20, 20, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
		 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
		 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
		 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
		 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10
	};

	/** Grade name */
	private static final String[] tableGradeName =
	{
		 "9",  "8",  "7",  "6",  "5",  "4",  "3",  "2",  "1",	//  0- 8
		"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9",	//  9-17
		"M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9",	// 18-26
		 "M", "MK", "MV", "MO", "ME", "MQ", "MS", "MM",			// 27-34
		"MG", "GM"												// 35-36
	};

	/** Secret grade name */
	private static final String[] tableSecretGradeName =
	{
		 "9",  "8",  "7",  "6",  "5",  "4",  "3",  "2",  "1",	//  0- 8
		"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9",	//  9-17
		 "M"													// 18
	};

	/** LV999 roll time */
	private static final int ROLLTIMELIMIT = 3238;

	/** M-Roll time requirement (by level 3000) */
	private static final int M_ROLL_TIME_REQUIRE = 73800;

	/** Number of entries in rankings */
	private static final int RANKING_MAX = 10;

	/** Number of sections */
	private static final int SECTION_MAX = 30;

	/** Default section time */
	private static final int DEFAULT_SECTION_TIME = 5400;

	/** GameManager that owns this mode */
	private GameManager owner;

	/** Drawing and event handling EventReceiver */
	private EventReceiver receiver;

	/** Current gravity section index (changes at tableGravityChangeLevel) */
	private int gravityindex;

	/** Next Section の level (これ-1のときに levelストップする) */
	private int nextseclv;

	/** Levelが増えた flag */
	private boolean lvupflag;

	/** 画面に表示されている実際の段位 */
	private int grade;

	/** 内部段位 */
	private int gradeInternal;

	/** 段位 point */
	private int gradePoint;

	/** 段位 pointが1つ減る time */
	private int gradeDecay;

	/** 最後に段位が上がった time */
	private int lastGradeTime;

	/** Hard dropした段count */
	private int harddropBonus;

	/** Combo bonus */
	private int comboValue;

	/** Most recent increase in score */
	private int lastscore;

	/** 獲得Render scoreがされる残り time */
	private int scgettime;

	/** Roll 経過 time */
	private int rolltime;

	/** Roll completely cleared flag */
	private int rollclear;

	/** Roll started flag */
	private boolean rollstarted;

	/** Garbage line counter */
	private int garbageCount;

	/** 裏段位 */
	private int secretGrade;

	/** Current BGM */
	private int bgmlv;

	/** 段位表示を光らせる残り frame count */
	private int gradeflash;

	/** Section Time */
	private int[] sectiontime;

	/** 新記録が出たSection はtrue */
	private boolean[] sectionIsNewRecord;

	/** Cleared Section count */
	private int sectionscomp;

	/** Average Section Time */
	private int sectionavgtime;

	/** 直前のSection Time */
	private int sectionlasttime;

	/** 消えRoll  flag１ (Section Time) */
	private boolean mrollSectiontime;

	/** 消えRoll  flag２ (4-line clear) */
	private boolean mrollFourline;

	/** 消えRoll started flag */
	private boolean mrollFlag;

	/** 消えRoll 中に消したline count */
	private int mrollLines;

	/** AC medal 状態 */
	private int medalAC;

	/** ST medal 状態 */
	private int medalST;

	/** SK medal 状態 */
	private int medalSK;

	/** RE medal 状態 */
	private int medalRE;

	/** CO medal 状態 */
	private int medalCO;

	/** 150個以上Blockがあるとtrue, 70個まで減らすとfalseになる */
	private boolean recoveryFlag;

	/** Section Time記録表示中ならtrue */
	private boolean isShowBestSectionTime;

	/** Level at start */
	private int startlevel;

	/** When true, always ghost ON */
	private boolean alwaysghost;

	/** When true, always 20G */
	private boolean always20g;

	/** When true, levelstop sound is enabled */
	private boolean lvstopse;

	/** When true, section time display is enabled */
	private boolean showsectiontime;

	private boolean gradedisp;

	/** Version */
	private int version;

	/** Current round's ranking rank */
	private int rankingRank;

	/** Rankings' 段位 */
	private int[] rankingGrade;

	/** Rankings'  level */
	private int[] rankingLevel;

	/** Rankings' times */
	private int[] rankingTime;

	/** Rankings' Roll completely cleared flag */
	private int[] rankingRollclear;

	/** Section Time記録 */
	private int[] bestSectionTime;

	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "GRADE MANIA LONG";
	}

	/*
	 * Initialization
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		receiver = engine.owner.receiver;

		gravityindex = 0;
		nextseclv = 0;
		lvupflag = true;
		grade = 0;
		gradeInternal = 0;
		gradePoint = 0;
		gradeDecay = 0;
		lastGradeTime = 0;
		harddropBonus = 0;
		comboValue = 0;
		lastscore = 0;
		scgettime = 0;
		rolltime = 0;
		rollclear = 0;
		rollstarted = false;
		garbageCount = 0;
		secretGrade = 0;
		bgmlv = 0;
		gradeflash = 0;
		sectiontime = new int[SECTION_MAX];
		sectionIsNewRecord = new boolean[SECTION_MAX];
		sectionscomp = 0;
		sectionavgtime = 0;
		sectionlasttime = 0;
		mrollSectiontime = true;
		mrollFourline = true;
		mrollFlag = false;
		mrollLines = 0;
		medalAC = 0;
		medalST = 0;
		medalSK = 0;
		medalRE = 0;
		medalCO = 0;
		recoveryFlag = false;
		isShowBestSectionTime = false;
		startlevel = 0;
		alwaysghost = false;
		always20g = false;
		lvstopse = false;

		rankingRank = -1;
		rankingGrade = new int[RANKING_MAX];
		rankingLevel = new int[RANKING_MAX];
		rankingTime = new int[RANKING_MAX];
		rankingRollclear = new int[RANKING_MAX];
		bestSectionTime = new int[SECTION_MAX];

		engine.tspinEnable = false;
		engine.b2bEnable = false;
		engine.comboType = GameEngine.COMBO_TYPE_DOUBLE;
		engine.bighalf = true;
		engine.bigmove = true;
		engine.staffrollEnable = true;
		engine.staffrollNoDeath = false;

		if(owner.replayMode == false) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
			version = CURRENT_VERSION;
		} else {
			for(int i = 0; i < SECTION_MAX; i++) {
				bestSectionTime[i] = DEFAULT_SECTION_TIME;
			}
			loadSetting(owner.replayProp);
			version = owner.replayProp.getProperty("grademanialong.version", 0);
		}

		owner.backgroundStatus.bg = Math.min(startlevel, 19);
	}

	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSetting(CustomProperties prop) {
		startlevel = prop.getProperty("grademanialong.startlevel", 0);
		alwaysghost = prop.getProperty("grademanialong.alwaysghost", false);
		always20g = prop.getProperty("grademanialong.always20g", false);
		lvstopse = prop.getProperty("grademanialong.lvstopse", false);
		showsectiontime = prop.getProperty("grademanialong.showsectiontime", false);
		gradedisp = prop.getProperty("grademanialong.gradedisp", false);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("grademanialong.startlevel", startlevel);
		prop.setProperty("grademanialong.alwaysghost", alwaysghost);
		prop.setProperty("grademanialong.always20g", always20g);
		prop.setProperty("grademanialong.lvstopse", lvstopse);
		prop.setProperty("grademanialong.gradedisp", gradedisp);
		prop.setProperty("grademanialong.showsectiontime", showsectiontime);
	}

	/**
	 * Set BGM at start of game
	 * @param engine GameEngine
	 */
	private void setStartBgmlv(GameEngine engine) {
		bgmlv = 0;
		while((tableBGMChange[bgmlv] != -1) && (engine.statistics.level >= tableBGMChange[bgmlv])) bgmlv++;
	}

	/**
	 * Update falling speed
	 * @param engine GameEngine
	 */
	private void setSpeed(GameEngine engine) {
		while (engine.statistics.level >= tableGravityChangeLevel[gravityindex]) gravityindex++;
		engine.speed.gravity = tableDenominator[gravityindex];

		int section = engine.statistics.level / 100;
		if (section > tableAre.length - 1) section = tableAre.length - 1;
		engine.speed.das = tableDAS[section];

		engine.speed.are = tableAre[section];
		engine.speed.areLine = tableAreLine[section];
		engine.speed.lineDelay = tableLineDelay[section];
		engine.speed.lockDelay = tableLockDelay[section];
	}

	/**
	 * Update average section time
	 */
	private void setAverageSectionTime() {
		if(sectionscomp > 0) {
			int temp = 0;
			for(int i = startlevel; i < startlevel + sectionscomp; i++) {
				if((i >= 0) && (i < sectiontime.length)) temp += sectiontime[i];
			}
			sectionavgtime = temp / sectionscomp;
		} else {
			sectionavgtime = 0;
		}
	}

	/**
	 * ST medal check
	 * @param engine GameEngine
	 * @param sectionNumber Section number
	 */
	private void stMedalCheck(GameEngine engine, int sectionNumber) {
		int best = bestSectionTime[sectionNumber];

		if(sectionlasttime < best) {
			if(medalST < 3) {
				engine.playSE("medal");
				medalST = 3;
			}
			if(!owner.replayMode) {
				sectionIsNewRecord[sectionNumber] = true;
			}
		} else if((sectionlasttime < best + 300) && (medalST < 2)) {
			engine.playSE("medal");
			medalST = 2;
		} else if((sectionlasttime < best + 600) && (medalST < 1)) {
			engine.playSE("medal");
			medalST = 1;
		}
	}

	/**
	 *  medal の文字色を取得
	 * @param medalColor  medal 状態
	 * @return  medal の文字色
	 */
	private int getMedalFontColor(int medalColor) {
		if(medalColor == 1) return EventReceiver.COLOR_RED;
		if(medalColor == 2) return EventReceiver.COLOR_WHITE;
		if(medalColor == 3) return EventReceiver.COLOR_YELLOW;
		return -1;
	}

	/*
	 * Called at settings screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// Menu
		if(engine.owner.replayMode == false) {
			// Configuration changes
			int change = updateCursor(engine, 5);

			if(change != 0) {
				engine.playSE("change");

				switch(engine.statc[2]) {
				case 0:
					startlevel += change;
					if(startlevel < 0) startlevel = 29;
					if(startlevel > 29) startlevel = 0;
					owner.backgroundStatus.bg = Math.min(startlevel, 19);
					break;
				case 1:
					alwaysghost = !alwaysghost;
					break;
				case 2:
					always20g = !always20g;
					break;
				case 3:
					lvstopse = !lvstopse;
					break;
				case 4:
					gradedisp = !gradedisp;
					break;
				case 5:
					showsectiontime = !showsectiontime;
					break;
				}
			}

			//  section time display切替
			if(engine.ctrl.isPush(Controller.BUTTON_F) && (engine.statc[3] >= 5)) {
				engine.playSE("change");
				isShowBestSectionTime = !isShowBestSectionTime;
			}

			// 決定
			if(engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
				engine.playSE("decide");
				saveSetting(owner.modeConfig);
				receiver.saveModeConfig(owner.modeConfig);
				isShowBestSectionTime = false;
				sectionscomp = 0;
				return false;
			}

			// Cancel
			if(engine.ctrl.isPush(Controller.BUTTON_B)) {
				engine.quitflag = true;
			}

			engine.statc[3]++;
		} else {
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
		drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
				"LEVEL", String.valueOf(startlevel * 100),
				"FULL GHOST", GeneralUtil.getONorOFF(alwaysghost),
				"20G MODE", GeneralUtil.getONorOFF(always20g),
				"LVSTOPSE", GeneralUtil.getONorOFF(lvstopse),
				"GRADE DISP", GeneralUtil.getONorOFF(gradedisp),
				"SHOW STIME", GeneralUtil.getONorOFF(showsectiontime));
	}

	/*
	 * Called at game start
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.statistics.level = startlevel * 100;

		nextseclv = engine.statistics.level + 100;
		if(engine.statistics.level < 0) nextseclv = 100;
		if(engine.statistics.level >= 2900) nextseclv = 3000;
		if (engine.statistics.level >= 2500) engine.bone = true;

		owner.backgroundStatus.bg = Math.min(engine.statistics.level / 100, 19);

		setSpeed(engine);
		setStartBgmlv(engine);
		owner.bgmStatus.bgm = bgmlv;
	}

	/*
	 * Render score
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		receiver.drawScoreFont(engine, playerID, 0, 0, "GRADE MANIA LONG", EventReceiver.COLOR_CYAN);

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false)) ) {
			if((owner.replayMode == false) && (startlevel == 0) && (always20g == false) && (engine.ai == null)) {
				if(!isShowBestSectionTime) {
					// Rankings
					float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
					int topY = (receiver.getNextDisplayType() == 2) ? 5 : 3;
					receiver.drawScoreFont(engine, playerID, 3, topY-1, "GRADE LEVEL TIME", EventReceiver.COLOR_BLUE, scale);

					for(int i = 0; i < RANKING_MAX; i++) {
						int gcolor = EventReceiver.COLOR_WHITE;
						if((rankingRollclear[i] == 1) || (rankingRollclear[i] == 3)) gcolor = EventReceiver.COLOR_GREEN;
						if((rankingRollclear[i] == 2) || (rankingRollclear[i] == 4)) gcolor = EventReceiver.COLOR_ORANGE;

						receiver.drawScoreFont(engine, playerID, 0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
						if((rankingGrade[i] >= 0) && (rankingGrade[i] < tableGradeName.length))
							receiver.drawScoreFont(engine, playerID, 3, topY+i, tableGradeName[rankingGrade[i]], gcolor, scale);
						receiver.drawScoreFont(engine, playerID, 9, topY+i, String.valueOf(rankingLevel[i]), (i == rankingRank), scale);
						receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingTime[i]), (i == rankingRank), scale);
					}

					receiver.drawScoreFont(engine, playerID, 0, 17, "F:VIEW SECTION TIME", EventReceiver.COLOR_GREEN);
				} else {
					// Section Time
					receiver.drawScoreFont(engine, playerID, 0, 2, "SECTION TIME", EventReceiver.COLOR_BLUE);

					int totalTime = 0;
					for(int i = 0; i < SECTION_MAX; i++) {
						int temp = Math.min(i * 100, 3000);
						int temp2 = Math.min(((i + 1) * 100) - 1, 3000);

						String strSectionTime;
						strSectionTime = String.format("%3d-%3d %s", temp, temp2, GeneralUtil.getTime(bestSectionTime[i]));

						receiver.drawScoreFont(engine, playerID, 0, 3 + i, strSectionTime, sectionIsNewRecord[i]);

						totalTime += bestSectionTime[i];
					}

					receiver.drawScoreFont(engine, playerID, 0, 14, "TOTAL", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(totalTime));
					receiver.drawScoreFont(engine, playerID, 9, 14, "AVERAGE", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, 9, 15, GeneralUtil.getTime(totalTime / SECTION_MAX));

					receiver.drawScoreFont(engine, playerID, 0, 17, "F:VIEW RANKING", EventReceiver.COLOR_GREEN);
				}
			}
		} else {
			// Graade
			if (gradedisp) {
				receiver.drawScoreFont(engine, playerID, 0, 2, "GRADE", EventReceiver.COLOR_BLUE);
				if ((grade >= 0) && (grade < tableGradeName.length))
					receiver.drawScoreFont(engine, playerID, 0, 3, tableGradeName[grade], ((gradeflash > 0) && (gradeflash % 4 == 0)));
				receiver.drawScoreFont(engine, playerID, 4, 3, String.format("%2d", gradeInternal), EventReceiver.COLOR_WHITE);
			}

			// Score
			receiver.drawScoreFont(engine, playerID, 0, 5, "SCORE", EventReceiver.COLOR_BLUE);
			String strScore;
			if((lastscore == 0) || (scgettime <= 0)) {
				strScore = String.valueOf(engine.statistics.score);
			} else {
				strScore = String.valueOf(engine.statistics.score) + "\n(+" + String.valueOf(lastscore) + ")";
			}
			receiver.drawScoreFont(engine, playerID, 0, 6, strScore);

			//  level
			receiver.drawScoreFont(engine, playerID, 0, 9, "LEVEL", EventReceiver.COLOR_BLUE);
			int tempLevel = engine.statistics.level;
			if(tempLevel < 0) tempLevel = 0;
			String strLevel = String.format("%3d", tempLevel);
			receiver.drawScoreFont(engine, playerID, 0, 10, strLevel);

			int speed = engine.speed.gravity / 128;
			if(engine.speed.gravity < 0) speed = 40;
			receiver.drawSpeedMeter(engine, playerID, 0, 11, speed);

			receiver.drawScoreFont(engine, playerID, 0, 12, String.format("%3d", nextseclv));

			// Time
			receiver.drawScoreFont(engine, playerID, 0, 14, "TIME", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(engine.statistics.time));

			// Roll 残り time
			if((engine.gameActive) && (engine.ending == 2)) {
				int time = ROLLTIMELIMIT - rolltime;
				if(time < 0) time = 0;
				receiver.drawScoreFont(engine, playerID, 0, 17, "ROLL TIME", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 0, 18, GeneralUtil.getTime(time), ((time > 0) && (time < 10 * 60)));
			}

			//  medal
			if(medalAC >= 1) receiver.drawScoreFont(engine, playerID, 0, 20, "AC", getMedalFontColor(medalAC));
			if(medalST >= 1) receiver.drawScoreFont(engine, playerID, 3, 20, "ST", getMedalFontColor(medalST));
			if(medalSK >= 1) receiver.drawScoreFont(engine, playerID, 0, 21, "SK", getMedalFontColor(medalSK));
			if(medalRE >= 1) receiver.drawScoreFont(engine, playerID, 0, 22, "RE", getMedalFontColor(medalRE));
			if(medalCO >= 1) receiver.drawScoreFont(engine, playerID, 3, 21, "CO", getMedalFontColor(medalCO));

			// Section Time
			if((showsectiontime == true) && (sectiontime != null)) {
				int x = (receiver.getNextDisplayType() == 2) ? 8 : 12;
				int x2 = (receiver.getNextDisplayType() == 2) ? 9 : 12;
				receiver.drawScoreFont(engine, playerID, x, 2, "SECTION TIME", EventReceiver.COLOR_BLUE);

				for(int i = 0; i < sectiontime.length; i++) {
					if(sectiontime[i] > 0) {
						int temp = i * 100;
						if(temp > 3000) temp = 3000;
						int skip = 0;

						int section = engine.statistics.level / 100;
						if (section > 9) skip = section - 9;
						if (i < skip) continue;
						String strSeparator = " ";
						if((i == section) && (engine.ending == 0)) strSeparator = "b";

						String strSectionTime;
						strSectionTime = String.format("%4d%s%s", temp, strSeparator, GeneralUtil.getTime(sectiontime[i]));

						receiver.drawScoreFont(engine, playerID, x-1, 3 + i - skip, strSectionTime, sectionIsNewRecord[i]);
					}
				}

				if(sectionavgtime > 0) {
					receiver.drawScoreFont(engine, playerID, x2, 14, "AVERAGE", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, x2, 15, GeneralUtil.getTime(sectionavgtime));
				}
			}
		}
	}

	/*
	 * 移動中の処理
	 */
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		// 新規ピース出現時
		if((engine.ending == 0) && (engine.statc[0] == 0) && (engine.holdDisable == false) && (!lvupflag)) {
			// Level up
			if(engine.statistics.level < nextseclv - 1) {
				engine.statistics.level++;
				if((engine.statistics.level == nextseclv - 1) && (lvstopse == true)) engine.playSE("levelstop");
			}
			levelUp(engine);

			// 旧Version用
			if(version <= 1) {
				// Hard drop bonusInitialization
				harddropBonus = 0;

				// RE medal
				if((engine.timerActive == true) && (medalRE < 3)) {
					int blocks = engine.field.getHowManyBlocks();

					if(recoveryFlag == false) {
						if(blocks >= 150) {
							recoveryFlag = true;
						}
					} else {
						if(blocks <= 70) {
							recoveryFlag = false;
							engine.playSE("medal");
							medalRE++;
						}
					}
				}
			}
		}
		if( (engine.ending == 0) && (engine.statc[0] > 0) && ((version >= 1) || (engine.holdDisable == false)) ) {
			lvupflag = false;
		}

		if((engine.ending == 0) && (engine.statc[0] == 0)&& (engine.holdDisable == false)) {
			if(tableGarbage[engine.statistics.level / 100] != 0) garbageCount++;

			// せり上がり
			if((garbageCount >= tableGarbage[engine.statistics.level / 100]) && (tableGarbage[engine.statistics.level / 100] != 0)) {
				engine.playSE("garbage");
				if (engine.statistics.level < 2500) {
					engine.field.addBottomCopyGarbage(Block.BLOCK_COLOR_GRAY,
							engine.getSkin(),
							Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE,
							1);
				}
				else {
					engine.field.addBottomCopyGarbage(Block.BLOCK_COLOR_GRAY,
							engine.getSkin(),
							Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE | Block.BLOCK_ATTRIBUTE_BONE,
							1);
				}
				garbageCount = 0;
			}
		}

		// 段位 point減少
		if((engine.timerActive == true) && (gradePoint > 0) && (engine.combo <= 0) && (engine.lockDelayNow < engine.getLockDelay() - 1)) {
			gradeDecay++;

			int index = gradeInternal;
			if(index > tableGradeDecayRate.length - 1) index = tableGradeDecayRate.length - 1;

			if(gradeDecay >= tableGradeDecayRate[index]) {
				gradeDecay = 0;
				gradePoint--;
			}
		}

		// Ending start
		if((engine.ending == 2) && (rollstarted == false)) {
			rollstarted = true;

			if(mrollFlag) {
				engine.bone = true;
				engine.blockHidden = engine.ruleopt.lockflash;
				engine.blockHiddenAnim = false;
				engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NONE;
			} else {
				engine.bone = false;
				engine.blockHidden = 300;
				engine.blockHiddenAnim = true;
				engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NONE;
			}

			owner.bgmStatus.bgm = BGMStatus.BGM_ENDING1;
		}

		return false;
	}

	/*
	 * ARE中の処理
	 */
	@Override
	public boolean onARE(GameEngine engine, int playerID) {
		// 最後の frame
		if((engine.ending == 0) && (engine.statc[0] >= engine.statc[1] - 1) && (!lvupflag)) {
			if(engine.statistics.level < nextseclv - 1) {
				engine.statistics.level++;
				if((engine.statistics.level == nextseclv - 1) && (lvstopse == true)) engine.playSE("levelstop");
			}
			levelUp(engine);
			lvupflag = true;
		}

		return false;
	}

	/**
	 *  levelが上がったときの共通処理
	 */
	private void levelUp(GameEngine engine) {
		// Meter
		engine.meterValue = ((engine.statistics.level % 100) * receiver.getMeterMax(engine)) / 99;
		engine.meterColor = GameEngine.METER_COLOR_GREEN;
		if(engine.statistics.level % 100 >= 50) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		if(engine.statistics.level % 100 >= 80) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		if(engine.statistics.level == nextseclv - 1) engine.meterColor = GameEngine.METER_COLOR_RED;

		// 速度変更
		setSpeed(engine);

		// LV100到達でghost を消す
		if((engine.statistics.level >= 100) && (!alwaysghost)) engine.ghost = false;

		// BGM fadeout
		if((tableBGMFadeout[bgmlv] != -1) && (engine.statistics.level >= tableBGMFadeout[bgmlv]))
			owner.bgmStatus.fadesw  = true;

		if(version >= 2) {
			// Hard drop bonusInitialization
			harddropBonus = 0;

			// RE medal
			if((engine.timerActive == true) && (medalRE < 3)) {
				int blocks = engine.field.getHowManyBlocks();

				if(recoveryFlag == false) {
					if(blocks >= 150) {
						recoveryFlag = true;
					}
				} else {
					if(blocks <= 70) {
						recoveryFlag = false;
						engine.playSE("medal");
						medalRE++;
					}
				}
			}
		}
	}

	/*
	 * Calculate score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Combo
		if(lines == 0) {
			comboValue = 1;
		} else {
			comboValue = comboValue + (2 * lines) - 2;
			if(comboValue < 1) comboValue = 1;
		}

		if((lines >= 1) && (engine.ending == 0)) {
			// 段位 point
			int index = gradeInternal;
			if(index > 10) index = 10;
			int basepoint = tableGradePoint[lines - 1][index];

			int indexcombo = engine.combo - 1;
			if(indexcombo < 0) indexcombo = 0;
			if(indexcombo > tableGradeComboBonus[lines - 1].length - 1) indexcombo = tableGradeComboBonus[lines - 1].length - 1;
			float combobonus = tableGradeComboBonus[lines - 1][indexcombo];

			int levelbonus = 1 + (engine.statistics.level / 500);

			float point = (basepoint * combobonus) * levelbonus;
			gradePoint += (int)point;

			// 内部段位上昇
			if(gradePoint >= 100) {
				gradePoint = 0;
				gradeDecay = 0;
				gradeInternal++;

				if((tableGradeChange[grade] != -1) && (gradeInternal >= tableGradeChange[grade])) {
					grade++;
					lastGradeTime = engine.statistics.time;
					if (gradedisp) {
						engine.playSE("gradeup");
						gradeflash = 180;
					}
				}
			}

			// 4-line clearカウント
			if(lines >= 4) {
				// SK medal
				if ((engine.statistics.totalFour == 20) || (engine.statistics.totalFour == 40) || (engine.statistics.totalFour == 70)) {
					engine.playSE("medal");
					medalSK++;
				}
			}

			// AC medal
			if(engine.field.isEmpty()) {
				engine.playSE("bravo");

				if(medalAC < 3) {
					engine.playSE("medal");
					medalAC++;
				}
			}

			// CO medal
			if((engine.combo >= 4) && (medalCO < 1)) {
				engine.playSE("medal");
				medalCO = 1;
			} else if((engine.combo >= 5) && (medalCO < 2)) {
				engine.playSE("medal");
				medalCO = 2;
			} else if((engine.combo >= 7) && (medalCO < 3)) {
				engine.playSE("medal");
				medalCO = 3;
			}

			// Level up
			int levelb = engine.statistics.level;
			engine.statistics.level += lines;
			levelUp(engine);

			if(engine.statistics.level >= 3000) {
				// Ending
				engine.statistics.level = 3000;
				engine.timerActive = false;
				engine.ending = 1;
				rollclear = 1;

				lastGradeTime = engine.statistics.time;

				// Section Timeを記録
				sectionlasttime = sectiontime[levelb / 100];
				sectionscomp++;
				setAverageSectionTime();

				// ST medal
				stMedalCheck(engine, levelb / 100);

				// M-Roll (invisible roll) criteria
				if((engine.statistics.time <= M_ROLL_TIME_REQUIRE) && (grade >= 34))
					mrollFlag = true;
			} else if(engine.statistics.level >= nextseclv) {
				// Next Section
				engine.playSE("levelup");

				// Bone blocks
				if (engine.statistics.level >= 2500) {
					engine.bone = true;
				}

				// Background切り替え
				if (owner.backgroundStatus.bg < 19) {
					owner.backgroundStatus.fadesw = true;
					owner.backgroundStatus.fadecount = 0;
					owner.backgroundStatus.fadebg = nextseclv / 100;
				}

				// BGM切り替え
				if((tableBGMChange[bgmlv] != -1) && (engine.statistics.level >= tableBGMChange[bgmlv])) {
					bgmlv++;
					owner.bgmStatus.fadesw = false;
					owner.bgmStatus.bgm = bgmlv;
				}

				// Section Timeを記録
				sectionlasttime = sectiontime[levelb / 100];
				sectionscomp++;
				setAverageSectionTime();

				// ST medal
				stMedalCheck(engine, levelb / 100);

				// Update level for next section
				nextseclv += 100;
				if(nextseclv > 3000) nextseclv = 3000;
			} else if((engine.statistics.level == nextseclv - 1) && (lvstopse == true)) {
				engine.playSE("levelstop");
			}

			// Calculate score
			int manuallock = 0;
			if(engine.manualLock == true) manuallock = 1;

			int bravo = 1;
			if(engine.field.isEmpty()) bravo = 4;

			int speedBonus = engine.getLockDelay() - engine.statc[0];
			if(speedBonus < 0) speedBonus = 0;

			lastscore = ((levelb + lines)/4 + engine.softdropFall + manuallock + harddropBonus) * lines * comboValue * bravo +
						(engine.statistics.level / 2) + (speedBonus * 7);
			engine.statistics.score += lastscore;
			scgettime = 120;
		} else if((lines >= 1) && (mrollFlag == true) && (engine.ending == 2)) {
			// 消えRoll 中のLine clear
			mrollLines += lines;
		}
	}

	/*
	 * Called when hard drop used
	 */
	@Override
	public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
		if(fall * 2 > harddropBonus) harddropBonus = fall * 2;
	}

	/*
	 * 各 frame の終わりの処理
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		// 段位上昇時のフラッシュ
		if(gradeflash > 0) gradeflash--;

		// 獲得Render score
		if(scgettime > 0) scgettime--;

		// 15分経過
		if(engine.statistics.time >= 54000) {
			setSpeed(engine);
		}

		// Section Time増加
		if((engine.timerActive) && (engine.ending == 0)) {
			int section = engine.statistics.level / 100;

			if((section >= 0) && (section < sectiontime.length)) {
				sectiontime[section]++;
			}
		}

		// Ending
		if((engine.gameActive) && (engine.ending == 2)) {
			rolltime++;

			// Time meter
			int remainRollTime = ROLLTIMELIMIT - rolltime;
			engine.meterValue = (remainRollTime * receiver.getMeterMax(engine)) / ROLLTIMELIMIT;
			engine.meterColor = GameEngine.METER_COLOR_GREEN;
			if(remainRollTime <= 30*60) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
			if(remainRollTime <= 20*60) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
			if(remainRollTime <= 10*60) engine.meterColor = GameEngine.METER_COLOR_RED;

			// Roll 終了
			if(rolltime >= ROLLTIMELIMIT) {
				rollclear = 2;

				if(mrollFlag == true) {
					grade = 36;
					gradeflash = 180;
					lastGradeTime = engine.statistics.time;
					engine.playSE("gradeup");

					rollclear = 3;
					if(mrollLines >= 32) rollclear = 4;
				}

				engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NORMAL;

				engine.gameEnded();
				engine.resetStatc();
				engine.stat = GameEngine.STAT_EXCELLENT;
			}
		}
	}

	/*
	 * game over
	 */
	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		// 段位M
		if((mrollFlag == true) && (grade < 35) && (engine.ending == 2) && (engine.statc[0] == 0)) {
			grade = 35;
			gradeflash = 180;
			lastGradeTime = engine.statistics.time;
			engine.playSE("gradeup");
		}

		if(engine.statc[0] == 0) {
			// Blockの表示を元に戻す
			engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NORMAL;
			// 裏段位
			secretGrade = engine.field.getSecretGrade();
		}

		return false;
	}

	/*
	 * 結果画面
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		receiver.drawMenuFont(engine, playerID, 0, 0, "kn PAGE" + (engine.statc[1] + 1) + "/3", EventReceiver.COLOR_RED);

		if(engine.statc[1] == 0) {
			int gcolor = EventReceiver.COLOR_WHITE;
			if((rollclear == 1) || (rollclear == 3)) gcolor = EventReceiver.COLOR_GREEN;
			if((rollclear == 2) || (rollclear == 4)) gcolor = EventReceiver.COLOR_ORANGE;
			receiver.drawMenuFont(engine, playerID, 0, 2, "GRADE", EventReceiver.COLOR_BLUE);
			String strGrade = String.format("%10s", tableGradeName[grade]);
			receiver.drawMenuFont(engine, playerID, 0, 3, strGrade, gcolor);

			drawResultStats(engine, playerID, receiver, 4, EventReceiver.COLOR_BLUE,
					STAT_SCORE, STAT_LINES, STAT_LEVEL_MANIA, STAT_TIME);
			drawResultRank(engine, playerID, receiver, 12, EventReceiver.COLOR_BLUE, rankingRank);
			if(secretGrade > 4) {
				drawResult(engine, playerID, receiver, 14, EventReceiver.COLOR_BLUE,
						"S. GRADE", String.format("%10s", tableSecretGradeName[secretGrade-1]));
			}
		} else if(engine.statc[1] == 1) {
			receiver.drawMenuFont(engine, playerID, 0, 2, "SECTION", EventReceiver.COLOR_BLUE);

			for(int i = 0; i < sectiontime.length; i++) {
				if(sectiontime[i] > 0) {
					receiver.drawMenuFont(engine, playerID, 2, 3 + i, GeneralUtil.getTime(sectiontime[i]), sectionIsNewRecord[i]);
				}
			}

			if(sectionavgtime > 0) {
				receiver.drawMenuFont(engine, playerID, 0, 14, "AVERAGE", EventReceiver.COLOR_BLUE);
				receiver.drawMenuFont(engine, playerID, 2, 15, GeneralUtil.getTime(sectionavgtime));
			}
		} else if(engine.statc[1] == 2) {
			receiver.drawMenuFont(engine, playerID, 0, 2, "MEDAL", EventReceiver.COLOR_BLUE);
			if(medalAC >= 1) receiver.drawMenuFont(engine, playerID, 5, 3, "AC", getMedalFontColor(medalAC));
			if(medalST >= 1) receiver.drawMenuFont(engine, playerID, 8, 3, "ST", getMedalFontColor(medalST));
			if(medalSK >= 1) receiver.drawMenuFont(engine, playerID, 5, 4, "SK", getMedalFontColor(medalSK));
			if(medalRE >= 1) receiver.drawMenuFont(engine, playerID, 8, 4, "RE", getMedalFontColor(medalRE));
			if(medalCO >= 1) receiver.drawMenuFont(engine, playerID, 8, 5, "CO", getMedalFontColor(medalCO));

			drawResultStats(engine, playerID, receiver, 6, EventReceiver.COLOR_BLUE,
					STAT_LPM, STAT_SPM, STAT_PIECE, STAT_PPS);
		}
	}

	/*
	 * 結果画面の処理
	 */
	@Override
	public boolean onResult(GameEngine engine, int playerID) {
		// ページ切り替え
		if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_UP)) {
			engine.statc[1]--;
			if(engine.statc[1] < 0) engine.statc[1] = 2;
			engine.playSE("change");
		}
		if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) {
			engine.statc[1]++;
			if(engine.statc[1] > 2) engine.statc[1] = 0;
			engine.playSE("change");
		}
		//  section time display切替
		if(engine.ctrl.isPush(Controller.BUTTON_F)) {
			engine.playSE("change");
			isShowBestSectionTime = !isShowBestSectionTime;
		}

		return false;
	}

	/*
	 * リプレイ保存
	 */
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		saveSetting(owner.replayProp);
		owner.replayProp.setProperty("result.grade.name", tableGradeName[grade]);
		owner.replayProp.setProperty("result.grade.number", grade);
		owner.replayProp.setProperty("grademanialong.version", version);

		// Update rankings
		if((owner.replayMode == false) && (startlevel == 0) && (always20g == false) && (engine.ai == null)) {
			updateRanking(grade, engine.statistics.level, lastGradeTime, rollclear);
			if(medalST == 3) updateBestSectionTime();

			if((rankingRank != -1) || (medalST == 3)) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				receiver.saveModeConfig(owner.modeConfig);
			}
		}
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void loadRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			rankingGrade[i] = prop.getProperty("grademanialong.ranking." + ruleName + ".grade." + i, 0);
			rankingLevel[i] = prop.getProperty("grademanialong.ranking." + ruleName + ".level." + i, 0);
			rankingTime[i] = prop.getProperty("grademanialong.ranking." + ruleName + ".time." + i, 0);
			rankingRollclear[i] = prop.getProperty("grademanialong.ranking." + ruleName + ".rollclear." + i, 0);
		}
		for(int i = 0; i < SECTION_MAX; i++) {
			bestSectionTime[i] = prop.getProperty("grademanialong.bestSectionTime." + ruleName + "." + i, DEFAULT_SECTION_TIME);
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			prop.setProperty("grademanialong.ranking." + ruleName + ".grade." + i, rankingGrade[i]);
			prop.setProperty("grademanialong.ranking." + ruleName + ".level." + i, rankingLevel[i]);
			prop.setProperty("grademanialong.ranking." + ruleName + ".time." + i, rankingTime[i]);
			prop.setProperty("grademanialong.ranking." + ruleName + ".rollclear." + i, rankingRollclear[i]);
		}
		for(int i = 0; i < SECTION_MAX; i++) {
			prop.setProperty("grademanialong.bestSectionTime." + ruleName + "." + i, bestSectionTime[i]);
		}
	}

	/**
	 * Update rankings
	 * @param gr 段位
	 * @param lv  level
	 * @param time Time
	 */
	private void updateRanking(int gr, int lv, int time, int clear) {
		rankingRank = checkRanking(gr, lv, time, clear);

		if(rankingRank != -1) {
			// Shift down ranking entries
			for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingGrade[i] = rankingGrade[i - 1];
				rankingLevel[i] = rankingLevel[i - 1];
				rankingTime[i] = rankingTime[i - 1];
				rankingRollclear[i] = rankingRollclear[i - 1];
			}

			// Add new data
			rankingGrade[rankingRank] = gr;
			rankingLevel[rankingRank] = lv;
			rankingTime[rankingRank] = time;
			rankingRollclear[rankingRank] = clear;
		}
	}

	/**
	 * Calculate ranking position
	 * @param gr grade
	 * @param lv  level
	 * @param time Time
	 * @return Position (-1 if unranked)
	 */
	private int checkRanking(int gr, int lv, int time, int clear) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if(clear > rankingRollclear[i]) {
				return i;
			} else if((clear == rankingRollclear[i]) && (gr > rankingGrade[i])) {
				return i;
			} else if((clear == rankingRollclear[i]) && (gr == rankingGrade[i]) && (lv > rankingLevel[i])) {
				return i;
			} else if((clear == rankingRollclear[i]) && (gr == rankingGrade[i]) && (lv == rankingLevel[i]) && (time < rankingTime[i])) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Update best section time records
	 */
	private void updateBestSectionTime() {
		for(int i = 0; i < SECTION_MAX; i++) {
			if(sectionIsNewRecord[i]) {
				bestSectionTime[i] = sectiontime[i];
			}
		}
	}
}
