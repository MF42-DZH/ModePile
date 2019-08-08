package zeroxfc.nullpo.custom.libs.backgroundtypes;

import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;

public class BackgroundNoAnim extends AnimatedBackgroundHook {
	private ResourceHolderCustomAssetExtension customHolder;

	{
		ID = AnimatedBackgroundHook.ANIMATION_NONE;
	}

	public BackgroundNoAnim(GameEngine engine, int bgNumber) {
		if (bgNumber < 0 || bgNumber > 19) bgNumber = 0;

		customHolder = new ResourceHolderCustomAssetExtension(engine);
		customHolder.loadImage("res/graphics/back" + bgNumber + ".png", "localBG");

		log.debug("Non-custom static background (" + bgNumber + ") created.");
	}

	public BackgroundNoAnim(GameEngine engine, String filePath) {
		customHolder = new ResourceHolderCustomAssetExtension(engine);
		customHolder.loadImage(filePath, "localBG");

		log.debug("Custom static background created (File Path: " + filePath + ").");
	}

	@Override
	public void update() {
		// EMPTY
	}

	@Override
	public void reset() {
		// EMPTY
	}

	@Override
	public void draw(GameEngine engine) {
		customHolder.drawImage(engine,"localBG", 0, 0);
	}

	@Override
	public void setBG(int bg) {
		customHolder.loadImage("res/graphics/back" + bg + ".png", "localBG");
		log.debug("Non-custom static background modified (New BG: " + bg + ").");
	}

	@Override
	public void setBG(String filePath) {
		customHolder.loadImage(filePath, "localBG");
		log.debug("Custom static background modified (New File Path: " + filePath + ").");
	}

	@Override
	public int getID() {
		return ID;
	}
}
