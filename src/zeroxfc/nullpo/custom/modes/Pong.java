package zeroxfc.nullpo.custom.modes;

import java.util.Random;
import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.CustomProperties;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.PhysicsObject;
import zeroxfc.nullpo.custom.libs.ProfileProperties;

public class Pong extends PuzzleGameEngine {
    // private static Logger log = Logger.getLogger(Pong.class);

    private static final int LOCALSTATE_IDLE = 0,
        LOCALSTATE_SPAWNING = 1,
        LOCALSTATE_INGAME = 2;

    private static final int COLLISION_NONE = 0,
        COLLISION_FIELD_TOP = 1,
        COLLISION_FIELD_BOTTOM = 2,
        COLLISION_PADDLE_PLAYER = 3,
        COLLISION_PADDLE_COMPUTER = 4;

    private static final int FIELD_WIDTH = 20,
        FIELD_HEIGHT = 15;

    private static final int DIFFICULTY_EASY = 0,
        DIFFICULTY_MEDIUM = 1,
        DIFFICULTY_HARD = 2;

    private static final String[] DIFFICULTY_NAMES = {
        "EASY",
        "MEDIUM",
        "HARD"
    };

    private static final int DIFFICULTY_COUNT = 3;

    private static final double PLAYER_PADDLE_VELOCITY = 8.0;

    private static final double[] COMPUTER_PADDLE_VELOCITY = {
        2.25, 3.5, 6.0
    };

    private static final double[] COMPUTER_POWER_HIT_CHANCE = {
        0.05, 0.1, 0.2
    };

    private static final double INITIAL_SPEED = 4.0;
    private static final double MAXIMUM_SPEED = 24.0;
    private static final double SPEED_MULTIPLIER = 1.25;

    private static final int VERSION = 1;
    private static final int headerColour = EventReceiver.COLOR_PINK;
    private GameManager owner;
    private EventReceiver receiver;
    private PhysicsObject paddlePlayer, paddleComputer, ball;
    private Random initialDirectionRandomiser, computerActionRandomiser;
    private int fieldBoxMinX, fieldBoxMinY, fieldBoxMaxX, fieldBoxMaxY;  // FIELD COLLISION BOUNDS.
    private int bg, bgm, difficulty;
    private int recentCollision, lastCollision;
    private int playerScore, computerScore;
    private double computerRange;
    private int version;
    private ProfileProperties playerProperties;
    private String PLAYER_NAME;

    @Override
    public String getName() {
        return "PONG";
    }

    @Override
    public void playerInit(GameEngine engine, int playerID) {
        owner = engine.owner;
        receiver = engine.owner.receiver;

        bg = 0;
        bgm = -1;
        difficulty = 0;
        localState = LOCALSTATE_IDLE;
        recentCollision = 0;
        lastCollision = 0;
        playerScore = 0;
        computerScore = 0;
        computerRange = 0;

        if (playerProperties == null) {
            playerProperties = new ProfileProperties(headerColour);
        }

        if (owner.replayMode) {
            loadSetting(owner.replayProp);

            if (playerProperties.isLoggedIn()) {
                loadSettingPlayer(playerProperties);
            }

            PLAYER_NAME = "";
        } else {
            loadSetting(owner.modeConfig);
            version = VERSION;

            PLAYER_NAME = owner.replayProp.getProperty("pong.playerName", "");
        }

        owner.backgroundStatus.bg = bg;
        owner.bgmStatus.bgm = -1;
        engine.framecolor = GameEngine.FRAME_COLOR_PINK;
    }

    @Override
    public boolean onSetting(GameEngine engine, int playerID) {
        owner.backgroundStatus.bg = bg;
        owner.bgmStatus.bgm = -1;

        // Menu
        if (!engine.owner.replayMode) {
            // Configuration changes
            int change = updateCursor(engine, 2, playerID);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        difficulty += change;
                        if (difficulty >= DIFFICULTY_COUNT) difficulty = 0;
                        if (difficulty < 0) difficulty = DIFFICULTY_COUNT - 1;
                        break;
                    case 1:
                        bg += change;
                        if (bg > 19) bg = 0;
                        if (bg < 0) bg = 19;
                        break;
                    case 2:
                        bgm += change;
                        if (bgm > 15) bgm = -1;
                        if (bgm < -1) bgm = 15;
                        break;
                }
            }

            engine.owner.backgroundStatus.bg = bg;

            // Confirm
            if (engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
                engine.playSE("decide");
                if (playerProperties.isLoggedIn()) {
                    saveSettingPlayer(playerProperties);
                    playerProperties.saveProfileConfig();
                } else {
                    saveSetting(owner.modeConfig);
                    receiver.saveModeConfig(owner.modeConfig);
                }
                return false;
            }

            // New acc
            if (engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null) {
                playerProperties = new ProfileProperties(headerColour);
                engine.playSE("decide");

                engine.stat = GameEngine.STAT_CUSTOM;
                engine.resetStatc();
                return true;
            }

            // Cancel
            if (engine.ctrl.isPush(Controller.BUTTON_B)) {
                engine.quitflag = true;
                playerProperties = new ProfileProperties(headerColour);
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

    @Override
    public void renderSetting(GameEngine engine, int playerID) {
        drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
            "DIFFICULTY", DIFFICULTY_NAMES[difficulty],
            "BG", String.valueOf(bg),
            "BGM", (bgm >= 0) ? String.valueOf(bgm) : "DISABLED");
    }

    @Override
    public boolean onReady(GameEngine engine, int playerID) {
        // 横溜め
        if (engine.ruleopt.dasInReady && engine.gameActive) engine.padRepeat();
        else if (engine.ruleopt.dasRedirectInDelay) {
            engine.dasRedirect();
        }

        // Initialization
        if (engine.statc[0] == 0) {
            // fieldInitialization
            engine.ruleopt.fieldWidth = FIELD_WIDTH;
            engine.ruleopt.fieldHeight = FIELD_HEIGHT;
            engine.ruleopt.fieldHiddenHeight = 0;
            engine.displaysize = 0;

            engine.ruleopt.nextDisplay = 0;
            engine.ruleopt.holdEnable = false;

            engine.fieldWidth = engine.ruleopt.fieldWidth;
            engine.fieldHeight = engine.ruleopt.fieldHeight;
            engine.fieldHiddenHeight = engine.ruleopt.fieldHiddenHeight;
            engine.field = new Field(engine.fieldWidth, engine.fieldHeight, engine.fieldHiddenHeight, true);

            fieldBoxMinX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
            fieldBoxMinY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
            fieldBoxMaxX = fieldBoxMinX + (FIELD_WIDTH * 16) - 1;
            fieldBoxMaxY = fieldBoxMinY + (FIELD_HEIGHT * 16) - 1;

            initialDirectionRandomiser = new Random(engine.randSeed);
            computerActionRandomiser = new Random(engine.randSeed + 1);

            resetPhysicsObjects(true);
            playerScore = 0;
            computerScore = 0;


            if (!engine.readyDone) {
                //  button input状態リセット
                engine.ctrl.reset();
                // ゲーム中 flagON
                engine.gameActive = true;
                engine.gameStarted = true;
                engine.isInGame = true;
            }

            rankingRank = -1;
        }

        // READY音
        if (engine.statc[0] == engine.readyStart) engine.playSE("ready");

        // GO音
        if (engine.statc[0] == engine.goStart) engine.playSE("go");

        // 開始
        if (engine.statc[0] >= engine.goEnd) {
            if (!engine.readyDone) engine.owner.bgmStatus.bgm = bgm;
            if (engine.owner.mode != null) engine.owner.mode.startGame(engine, playerID);
            engine.owner.receiver.startGame(engine, playerID);
            engine.stat = GameEngine.STAT_CUSTOM;
            localState = LOCALSTATE_SPAWNING;
            engine.timerActive = true;
            engine.resetStatc();
            if (!engine.readyDone) {
                engine.startTime = System.nanoTime();
                //startTime = System.nanoTime()/1000000L;
            }
            engine.readyDone = true;
            return true;
        }

        engine.statc[0]++;

        return true;
    }

    private void resetPhysicsObjects(boolean resetPaddles) {
        DoubleVector position = new DoubleVector((fieldBoxMinX + fieldBoxMaxX) / 2d, (fieldBoxMinY + fieldBoxMaxY) / 2d, false);
        ball = new PhysicsObject(position, DoubleVector.zero(), -1, 1, 1, PhysicsObject.ANCHOR_POINT_MM, Block.BLOCK_COLOR_GREEN);
        ball.PROPERTY_Static = false;

        recentCollision = 0;
        lastCollision = 0;

        if (resetPaddles) {
            DoubleVector playerPosition = new DoubleVector(fieldBoxMinX, (fieldBoxMinY + fieldBoxMaxY) / 2d, false);
            DoubleVector computerPosition = new DoubleVector(fieldBoxMaxX, (fieldBoxMinY + fieldBoxMaxY) / 2d, false);

            paddlePlayer = new PhysicsObject(playerPosition, DoubleVector.zero(), -1, 2, 4, PhysicsObject.ANCHOR_POINT_ML, Block.BLOCK_COLOR_CYAN);
            paddleComputer = new PhysicsObject(computerPosition, DoubleVector.zero(), -1, 2, 4, PhysicsObject.ANCHOR_POINT_MR, Block.BLOCK_COLOR_RED);

            paddlePlayer.PROPERTY_Static = false;
            paddleComputer.PROPERTY_Static = false;

            computerRange = computerActionRandomiser.nextDouble() * 32;
        }
    }

    private void setBallRandomDirection() {
        double coeff = initialDirectionRandomiser.nextDouble();
        final double pot = Math.PI / 2;
        double angle;

        if (coeff < 0.5) {
            final double tpof = Math.PI * 0.75;
            angle = tpof + (pot * initialDirectionRandomiser.nextDouble());
        } else {
            final double optpof = (Math.PI * 0.75) + Math.PI;
            angle = optpof + (pot * initialDirectionRandomiser.nextDouble());
            if (angle >= Math.PI * 2) angle -= (Math.PI * 2);
        }

        ball.velocity.setDirection(angle);
        ball.velocity.setMagnitude(INITIAL_SPEED);
    }

    @Override
    public boolean onGameOver(GameEngine engine, int playerID) {
        if (engine.lives <= 0) {
            // もう復活できないとき
            if (engine.statc[0] == 0) {
                engine.gameEnded();
                engine.blockShowOutlineOnly = false;
                if (owner.getPlayers() < 2) owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;

                if (engine.field.isEmpty()) {
                    engine.statc[0] = engine.field.getHeight() + 1;
                } else {
                    engine.resetFieldVisible();
                }
            }

            if (engine.statc[0] < engine.field.getHeight() + 1) {
                for (int i = 0; i < engine.field.getWidth(); i++) {
                    if (engine.field.getBlockColor(i, engine.field.getHeight() - engine.statc[0]) != Block.BLOCK_COLOR_NONE) {
                        Block blk = engine.field.getBlock(i, engine.field.getHeight() - engine.statc[0]);

                        if (blk != null) {
                            if (blk.color > Block.BLOCK_COLOR_NONE) {
                                if (!blk.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE)) {
                                    blk.color = Block.BLOCK_COLOR_GRAY;
                                    blk.setAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE, true);
                                }
                                blk.darkness = 0.3f;
                                blk.elapsedFrames = -1;
                            }
                        }
                    }
                }
                engine.statc[0]++;
            } else if (engine.statc[0] == engine.field.getHeight() + 1) {
                engine.playSE("gameover");
                engine.statc[0]++;
            } else if (engine.statc[0] < engine.field.getHeight() + 1 + 180) {
                if ((engine.statc[0] >= engine.field.getHeight() + 1 + 60) && (engine.ctrl.isPush(Controller.BUTTON_A))) {
                    engine.statc[0] = engine.field.getHeight() + 1 + 180;
                }

                engine.statc[0]++;
            } else {
                if (!owner.replayMode || owner.replayRerecord) owner.saveReplay();

                for (int i = 0; i < owner.getPlayers(); i++) {
                    if ((i == playerID) || (engine.gameoverAll)) {
                        if (owner.engine[i].field != null) {
                            owner.engine[i].field.reset();
                        }
                        owner.engine[i].resetStatc();
                        owner.engine[i].stat = GameEngine.STAT_RESULT;
                    }
                }
            }
        } else {
            // 復活できるとき
            if (engine.statc[0] == 0) {
                engine.blockShowOutlineOnly = false;
                engine.playSE("died");

                engine.resetFieldVisible();

                for (int i = (engine.field.getHiddenHeight() * -1); i < engine.field.getHeight(); i++) {
                    for (int j = 0; j < engine.field.getWidth(); j++) {
                        if (engine.field.getBlockColor(j, i) != Block.BLOCK_COLOR_NONE) {
                            engine.field.setBlockColor(j, i, Block.BLOCK_COLOR_GRAY);
                        }
                    }
                }

                engine.statc[0] = 1;
            }

            if (!engine.field.isEmpty()) {
                engine.field.pushDown();
            } else if (engine.statc[1] < engine.getARE()) {
                engine.statc[1]++;
            } else {
                engine.lives--;
                engine.resetStatc();
                engine.stat = GameEngine.STAT_CUSTOM;
            }
        }
        return true;
    }

    /*
     * Called when saving replay
     */
    @Override
    public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
        saveSetting(prop);

        if ((!owner.replayMode)) {
            receiver.saveModeConfig(owner.modeConfig);

            if (playerProperties.isLoggedIn()) {
                prop.setProperty("pong.playerName", playerProperties.getNameDisplay());
            }
        }
    }

    @Override
    public boolean onCustom(GameEngine engine, int playerID) {
        if (engine.gameActive) {
            boolean updateTimer = false;
            // Override this.

            // Player movement.
            final DoubleVector playerVelocity = new DoubleVector(0, 0, false);
            if (engine.ctrl.isPress(Controller.BUTTON_UP)) {
                playerVelocity.setY(-1 * PLAYER_PADDLE_VELOCITY);
                paddlePlayer.move(playerVelocity);
                if (paddlePlayer.getMinY() < fieldBoxMinY) paddlePlayer.position.setY(fieldBoxMinY + 32);
            }

            if (engine.ctrl.isPress(Controller.BUTTON_DOWN)) {
                playerVelocity.setY(PLAYER_PADDLE_VELOCITY);
                paddlePlayer.move(playerVelocity);
                if (paddlePlayer.getMaxY() > fieldBoxMaxY) paddlePlayer.position.setY(fieldBoxMaxY - 32);
            }

            // Computer movement.
            // Use an approx. equal for this.
            final DoubleVector computerVelocity = new DoubleVector(0, 0, false);
            if (!almostEqual(ball.position.getY(), paddleComputer.position.getY(), computerRange)) {
                if (ball.position.getY() > paddleComputer.position.getY()) {
                    computerVelocity.setY(COMPUTER_PADDLE_VELOCITY[difficulty]);
                    paddleComputer.move(computerVelocity);
                    if (paddleComputer.getMaxY() > fieldBoxMaxY) paddleComputer.position.setY(fieldBoxMaxY - 32);
                }
                if (ball.position.getY() < paddleComputer.position.getY()) {
                    computerVelocity.setY(-1 * COMPUTER_PADDLE_VELOCITY[difficulty]);
                    paddleComputer.move(computerVelocity);
                    if (paddleComputer.getMinY() < fieldBoxMinY) paddleComputer.position.setY(fieldBoxMinY + 32);
                }
            }

            switch (localState) {
                case LOCALSTATE_SPAWNING:
                    updateTimer = statSpawning(engine);
                    break;
                case LOCALSTATE_INGAME:
                    updateTimer = statIngame(engine);
                    break;
                default:
                    break;
            }

            if (updateTimer) engine.statc[0]++;
        } else {
            engine.isInGame = true;

            boolean s = playerProperties.loginScreen.updateScreen(engine, playerID);
            if (playerProperties.isLoggedIn()) {
                loadSettingPlayer(playerProperties);
            }

            if (engine.stat == GameEngine.STAT_SETTING) engine.isInGame = false;
        }
        return true;
    }

    private boolean statSpawning(GameEngine engine) {
        engine.meterValue = (int) (((double) engine.statc[0] / 60.0) * receiver.getMeterMax(engine));
        engine.meterColor = GameEngine.METER_COLOR_GREEN;

        if (engine.statc[0] == 0) {
            resetPhysicsObjects(false);
        } else if (engine.statc[0] == 60) {
            localState = LOCALSTATE_INGAME;
            setBallRandomDirection();
            engine.resetStatc();
            engine.playSE("levelup");
            return false;
        }

        return true;
    }

    // Fuzzy equals.
    private boolean almostEqual(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
    }

    private boolean statIngame(GameEngine engine) {
        engine.meterValue = receiver.getMeterMax(engine);
        engine.meterColor = GameEngine.METER_COLOR_GREEN;

        boolean playerPowerhit = false;
        boolean computerPowerhit = false;

		/*
		PhysicsObject[] testClones = new PhysicsObject[6];  // 6-step collision checker.
		for (int i = 0; i < testClones.length; i++) {
			testClones[i] = ball.clone();
			testClones[i].velocity.setMagnitude((testClones[i].velocity.getMagnitude() / 6) * (i + 1));
			testClones[i].move();
		}

		int index = 0;
		for (int i = 0; i < testClones.length; i++) {
			index = i;
			if (PhysicsObject.checkCollision(testClones[i], paddlePlayer)) {
				index = i - 1;
				recentCollision = COLLISION_PADDLE_PLAYER;
				break;
			} else if (PhysicsObject.checkCollision(testClones[i], paddleComputer)) {
				index = i - 1;
				recentCollision = COLLISION_PADDLE_COMPUTER;
				break;
			} else if (testClones[i].getMinY() < fieldBoxMinY) {
				index = i - 1;
				recentCollision = COLLISION_FIELD_TOP;
				break;
			} else if (testClones[i].getMaxY() > fieldBoxMaxY) {
				index = i - 1;
				recentCollision = COLLISION_FIELD_BOTTOM;
				break;
			}

			recentCollision = COLLISION_NONE;
		}

		if (index != -1) {
			ball = testClones[index].clone();
		} else {
			// Collision imminent. Must reflect;
			ball = testClones[0].clone();

			final double pof = Math.PI / 4;
			final double tpof = Math.PI * 0.75;
			final double optpof = (Math.PI * 0.75) + Math.PI;
			final double maxOffset = 32.0;
			double yCentreOffset;
			double angle;

			switch (recentCollision) {
				case COLLISION_FIELD_TOP:
				case COLLISION_FIELD_BOTTOM:
					PhysicsObject.reflectVelocity(ball.velocity, true);
					break;
				case COLLISION_PADDLE_PLAYER:
					yCentreOffset = ball.position.getY() - paddlePlayer.position.getY();
					angle = pof * (yCentreOffset / maxOffset);
					ball.velocity.setDirection(angle);

					break;
				case COLLISION_PADDLE_COMPUTER:
					yCentreOffset = ball.position.getY() - paddleComputer.position.getY();
					angle = (-1 * (pof * (yCentreOffset / maxOffset))) + Math.PI;
					ball.velocity.setDirection(angle);

					break;
				default:
					break;
			}
		}
		*/
        ball.move();

        if (PhysicsObject.checkCollision(ball, paddlePlayer) && lastCollision == COLLISION_NONE) {
            recentCollision = COLLISION_PADDLE_PLAYER;
            lastCollision = COLLISION_PADDLE_PLAYER;
            playerPowerhit = engine.ctrl.isPress(Controller.BUTTON_A);
        } else if (PhysicsObject.checkCollision(ball, paddleComputer) && lastCollision == COLLISION_NONE) {
            recentCollision = COLLISION_PADDLE_COMPUTER;
            lastCollision = COLLISION_PADDLE_COMPUTER;
            computerPowerhit = computerActionRandomiser.nextDouble() < COMPUTER_POWER_HIT_CHANCE[difficulty];
            computerRange = computerActionRandomiser.nextDouble() * 32;
        } else if (ball.getMinY() < fieldBoxMinY && lastCollision == COLLISION_NONE) {
            recentCollision = COLLISION_FIELD_TOP;
            lastCollision = COLLISION_FIELD_TOP;
        } else if (ball.getMaxY() > fieldBoxMaxY && lastCollision == COLLISION_NONE) {
            recentCollision = COLLISION_FIELD_BOTTOM;
            lastCollision = COLLISION_FIELD_BOTTOM;
        } else {
            recentCollision = COLLISION_NONE;
            lastCollision = COLLISION_NONE;
        }

        final double pof = Math.PI / 4;
        final double maxOffset = 32.0;
        double yCentreOffset;
        double angle;
        switch (recentCollision) {
            case COLLISION_FIELD_TOP:
            case COLLISION_FIELD_BOTTOM:
                PhysicsObject.reflectVelocity(ball.velocity, true);
                if (ball.position.getY() < fieldBoxMinY + 8) ball.position.setY(fieldBoxMinY + 8);
                if (ball.position.getY() > fieldBoxMaxY - 8) ball.position.setY(fieldBoxMaxY - 8);
                engine.playSE("move");
                break;
            case COLLISION_PADDLE_PLAYER:
                yCentreOffset = ball.position.getY() - paddlePlayer.position.getY();
                angle = pof * (yCentreOffset / maxOffset);
                ball.velocity.setDirection(angle);
                if (playerPowerhit) {
                    ball.velocity.setMagnitude(ball.velocity.getMagnitude() * SPEED_MULTIPLIER);
                    engine.playSE("step");
                } else {
                    engine.playSE("move");
                }

                break;
            case COLLISION_PADDLE_COMPUTER:
                yCentreOffset = ball.position.getY() - paddleComputer.position.getY();
                angle = (-1 * (pof * (yCentreOffset / maxOffset))) + Math.PI;
                ball.velocity.setDirection(angle);
                if (computerPowerhit) {
                    ball.velocity.setMagnitude(ball.velocity.getMagnitude() * SPEED_MULTIPLIER);
                    engine.playSE("step");
                } else {
                    engine.playSE("move");
                }

                break;
            default:
                break;
        }

        double maxSpeed = MAXIMUM_SPEED;
        if (version <= 0) maxSpeed = 16;

        if (ball.velocity.getMagnitude() > maxSpeed) ball.velocity.setMagnitude(maxSpeed);

        if (ball.getMinX() <= fieldBoxMinX) {
            engine.resetStatc();
            localState = LOCALSTATE_SPAWNING;
            computerScore++;
            engine.playSE("died");
        } else if (ball.getMaxX() >= fieldBoxMaxX) {
            engine.resetStatc();
            localState = LOCALSTATE_SPAWNING;
            playerScore++;
            engine.playSE("stageclear");
        }

        if (playerScore >= 10) {
            engine.resetStatc();
            engine.stat = GameEngine.STAT_EXCELLENT;
            engine.ending = 1;
            engine.gameEnded();
            localState = LOCALSTATE_IDLE;
        } else if (computerScore >= 10) {
            engine.resetStatc();
            engine.stat = GameEngine.STAT_GAMEOVER;
            engine.gameEnded();
            localState = LOCALSTATE_IDLE;
        }

        return false;
    }

    /*
     * Render score
     */
    @Override
    public void renderLast(GameEngine engine, int playerID) {
        if (owner.menuOnly) return;

        if (engine.stat == GameEngine.STAT_SETTING) {
            receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 1, "(" + DIFFICULTY_NAMES[difficulty] + " MODE)", EventReceiver.COLOR_BLUE);

            switch (difficulty) {
                case DIFFICULTY_EASY:
                    receiver.drawScoreFont(engine, playerID, 0, 3, "SLOW COMPUTER PADDLE");
                    receiver.drawScoreFont(engine, playerID, 0, 4, "5% SPEED-HIT CHANCE");
                    break;
                case DIFFICULTY_MEDIUM:
                    receiver.drawScoreFont(engine, playerID, 0, 3, "MEDIUM COMPUTER PADDLE");
                    receiver.drawScoreFont(engine, playerID, 0, 4, "10% SPEED-HIT CHANCE");
                    break;
                case DIFFICULTY_HARD:
                    receiver.drawScoreFont(engine, playerID, 0, 3, "FAST COMPUTER PADDLE");
                    receiver.drawScoreFont(engine, playerID, 0, 4, "20% SPEED-HIT CHANCE");
                    break;
                default:
                    break;
            }

            if (playerProperties.isLoggedIn()) {
                receiver.drawScoreFont(engine, playerID, 0, 6, "PLAYER", EventReceiver.COLOR_BLUE);
                receiver.drawScoreFont(engine, playerID, 0, 7, playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
            } else {
                receiver.drawScoreFont(engine, playerID, 0, 6, "LOGIN STATUS", EventReceiver.COLOR_BLUE);
                if (!playerProperties.isLoggedIn())
                    receiver.drawScoreFont(engine, playerID, 0, 7, "(NOT LOGGED IN)\n(E:LOG IN)");
            }
        } else if (engine.stat == GameEngine.STAT_CUSTOM && !engine.gameActive) {
            playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
        } else {
            receiver.drawScoreFont(engine, playerID, 8, 0, getName(), EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 8, 1, "(" + DIFFICULTY_NAMES[difficulty] + " MODE)", EventReceiver.COLOR_BLUE);

            receiver.drawScoreFont(engine, playerID, 8, 3, "1P PTS.", EventReceiver.COLOR_CYAN);
            receiver.drawScoreFont(engine, playerID, 8, 4, String.valueOf(playerScore));

            receiver.drawScoreFont(engine, playerID, 8, 6, "COM. PTS.", EventReceiver.COLOR_RED);
            receiver.drawScoreFont(engine, playerID, 8, 7, String.valueOf(computerScore));

            if (playerProperties.isLoggedIn() || PLAYER_NAME.length() > 0) {
                receiver.drawScoreFont(engine, playerID, 8, 9, "PLAYER", EventReceiver.COLOR_BLUE);
                receiver.drawScoreFont(engine, playerID, 8, 10, owner.replayMode ? PLAYER_NAME : playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
            }

            if (engine.stat != GameEngine.STAT_RESULT) {
                if (paddleComputer != null) paddleComputer.draw(receiver, engine, playerID);
                if (paddlePlayer != null) paddlePlayer.draw(receiver, engine, playerID);
                if (ball != null) ball.draw(receiver, engine, playerID);
            }
        }
    }

    /*
     * Render results screen
     */
    @Override
    public void renderResult(GameEngine engine, int playerID) {
        receiver.drawMenuFont(engine, playerID, 0, 0, "1P PTS.", EventReceiver.COLOR_CYAN);
        receiver.drawMenuFont(engine, playerID, 0, 1, String.format("%10s", playerScore));

        receiver.drawMenuFont(engine, playerID, 0, 2, "COM. PTS.", EventReceiver.COLOR_RED);
        receiver.drawMenuFont(engine, playerID, 0, 3, String.format("%10s", computerScore));

        if (playerScore >= 10) {
            receiver.drawMenuFont(engine, playerID, 0, 5, "P1 WIN!");
        } else {
            receiver.drawMenuFont(engine, playerID, 0, 6, "COM. WIN!");
        }
    }


    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSetting(CustomProperties prop) {
        bg = prop.getProperty("pong.bg", 0);
        bgm = prop.getProperty("pong.bgm", -1);
        difficulty = prop.getProperty("pong.difficulty", 0);
        version = prop.getProperty("pong.version", 0);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSetting(CustomProperties prop) {
        prop.setProperty("pong.bg", bg);
        prop.setProperty("pong.bgm", bgm);
        prop.setProperty("pong.difficulty", difficulty);
        prop.getProperty("pong.version", version);
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;
        bg = prop.getProperty("pong.bg", 0);
        bgm = prop.getProperty("pong.bgm", -1);
        difficulty = prop.getProperty("pong.difficulty", 0);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;
        prop.setProperty("pong.bg", bg);
        prop.setProperty("pong.bgm", bgm);
        prop.setProperty("pong.difficulty", difficulty);
    }
}
