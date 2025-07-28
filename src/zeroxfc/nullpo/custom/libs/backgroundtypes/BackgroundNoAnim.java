package zeroxfc.nullpo.custom.libs.backgroundtypes;

import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;

public class BackgroundNoAnim extends AnimatedBackgroundHook {
    // private CustomResourceHolder customHolder;

    {
        ID = AnimatedBackgroundHook.ANIMATION_NONE;
        setImageName("localBG");
    }

    /**
     * Almost redundant background.
     */
    public BackgroundNoAnim(int bgNumber) {
        if (bgNumber < 0 || bgNumber > 19) bgNumber = 0;

        customHolder = new CustomResourceHolder();
        customHolder.loadImage("res/graphics/back" + bgNumber + ".png", imageName);

        log.debug("Non-custom static background (" + bgNumber + ") created.");
    }

    public BackgroundNoAnim(String filePath) {
        customHolder = new CustomResourceHolder();
        customHolder.loadImage(filePath, imageName);

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
    public void draw(GameEngine engine, int playerID) {
        customHolder.drawImage(engine, imageName, 0, 0);
    }

    @Override
    public void setBG(int bg) {
        customHolder.loadImage("res/graphics/back" + bg + ".png", imageName);
        log.debug("Non-custom static background modified (New BG: " + bg + ").");
    }

    @Override
    public void setBG(String filePath) {
        customHolder.loadImage(filePath, imageName);
        log.debug("Custom static background modified (New File Path: " + filePath + ").");
    }

    /**
     * Allows the hot-swapping of pre-loaded BGs from a storage instance of a <code>CustomResourceHolder</code>.
     *
     * @param holder Storage instance
     * @param name   Image name
     */
    @Override
    public void setBGFromHolder(CustomResourceHolder holder, String name) {
        customHolder.putImageAt(holder.getImageAt(name), imageName);
        log.debug("Custom static background modified (New Image Reference: " + name + ").");
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
