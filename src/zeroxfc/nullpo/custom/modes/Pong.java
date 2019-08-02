package zeroxfc.nullpo.custom.modes;

import java.util.Random;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.CustomProperties;
import zeroxfc.nullpo.custom.libs.*;

public class Pong extends PuzzleGameEngine {
	private static final int FIELD_WIDTH = 20,
	                         FIELD_HEIGHT = 12;

	private static final int DIFFICULTY_EASY = 0,
	                         DIFFICULTY_MEDIUM = 1,
	                         DIFFICULTY_HARD = 2;

	private static final double PLAYER_PADDLE_VELOCITY = 2.0,
	                            COMPUTER_EASY_VELOCITY = 1.0,
			                    COMPUTER_MEDIUM_VELOCITY = 1.5,
			                    COMPUTER_HARD_VELOCITY = 2.0;

	private static final double UP = Math.PI / 2;
	private static final double DOWN = UP * 3;

	private GameManager owner;
	private EventReceiver receiver;
	private PhysicsObject paddle_player, paddle_computer, ball;
	private Random initialDirectionRandomiser;
	private int fieldBoxMinX, fieldBoxMinY, fieldBoxMaxX, fieldBoxMaxY;
	private int bg, bgm, difficulty;

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

		loadSetting(owner.modeConfig);
	}

	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSetting(CustomProperties prop) {
		bg = prop.getProperty("pong.bg", 0);
		bgm = prop.getProperty("pong.bgm", -1);
		difficulty = prop.getProperty("pong.difficulty", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		// Override this.
	}
}
