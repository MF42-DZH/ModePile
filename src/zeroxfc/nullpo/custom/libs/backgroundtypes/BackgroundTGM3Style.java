package zeroxfc.nullpo.custom.libs.backgroundtypes;

import mu.nu.nullpo.game.play.GameEngine;

public class BackgroundTGM3Style extends AnimatedBackgroundHook {
	{
		ID = AnimatedBackgroundHook.ANIMATION_TGM3TI_STYLE;
	}

	@Override
	public void update() {

	}

	@Override
	public void reset() {

	}

	@Override
	public void draw(GameEngine engine, int playerID) {

	}

	@Override
	public void setBG(int bg) {

	}

	@Override
	public void setBG(String filePath) {

	}

	/**
	 * This last one is important. In the case that any of the child types are used, it allows identification.
	 * The identification can be used to allow casting during operations.
	 *
	 * @return Identification number of child class.
	 */
	@Override
	public int getID() {
		return ID;
	}
}
