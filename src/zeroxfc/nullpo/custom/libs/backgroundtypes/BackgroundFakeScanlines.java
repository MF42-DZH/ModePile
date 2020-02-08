package zeroxfc.nullpo.custom.libs.backgroundtypes;

import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;

import java.util.Random;

public class BackgroundFakeScanlines extends AnimatedBackgroundHook {
	private static final int AMT = 480 / 2;
	private static final int PERIOD = 480;  // Frames
	private static final float BASE_LUMINANCE_OFFSET = 0.25f;

	// private ResourceHolderCustomAssetExtension customHolder;
	private Random colourRandom;
	private ImageChunk[] chunks;
	private int phase;

	{
		ID = AnimatedBackgroundHook.ANIMATION_FAKE_SCANLINES;
		setImageName("localBG");
	}

	public BackgroundFakeScanlines(int bgNumber) {
		if (bgNumber < 0 || bgNumber > 19) bgNumber = 0;

		customHolder = new ResourceHolderCustomAssetExtension();
		customHolder.loadImage("res/graphics/back" + bgNumber + ".png", imageName);

		setup();

		log.debug("Non-custom fake scanline background (" + bgNumber + ") created.");
	}

	public BackgroundFakeScanlines(String filePath) {
		customHolder = new ResourceHolderCustomAssetExtension();
		customHolder.loadImage(filePath, imageName);

		setup();

		log.debug("Custom fake scanline background created (File Path: " + filePath + ").");
	}

	@Override
	public void setBG(int bg) {
		customHolder.loadImage("res/graphics/back" + bg + ".png", imageName);
		log.debug("Non-custom horizontal bars background modified (New BG: " + bg + ").");
	}

	@Override
	public void setBG(String filePath) {
		customHolder.loadImage(filePath, imageName);
		log.debug("Custom horizontal bars background modified (New File Path: " + filePath + ").");
	}

	/**
	 * Allows the hot-swapping of pre-loaded BGs from a storage instance of a <code>ResourceHolderCustomAssetExtension</code>.
	 *
	 * @param holder Storage instance
	 * @param name   Image name
	 */
	@Override
	public void setBGFromHolder(ResourceHolderCustomAssetExtension holder, String name) {
		customHolder.putImageAt(holder.getImageAt(name), imageName);
		log.debug("Custom horizontal bars background modified (New Image Reference: " + name + ").");
	}

	private void setup() {
		colourRandom = new Random();

		// Generate chunks
		chunks = new ImageChunk[AMT];
		for (int i = 0; i < chunks.length; i++) {
			chunks[i] = new ImageChunk(ImageChunk.ANCHOR_POINT_TL, new int[] { 0, ((480 / AMT) * i) + ((480 / AMT) / 2) }, new int[] { 0, (480 / AMT) * i }, new int[] { 640, (480 / AMT) }, new float[] { 1f, 1f } );
		}

		phase = 0;
	}

	@Override
	public void update() {
		if (colourRandom == null) return;
		for (ImageChunk chunk : chunks) {
			float newScale = (float) (0.01f * colourRandom.nextDouble()) + 0.995f;
			chunk.setScale(new float[]{newScale, 1f});
		}

		phase = (phase + 1) % PERIOD;
	}

	@Override
	public void reset() {
		phase = 0;
		update();
	}

	@Override
	public void draw(GameEngine engine, int playerID) {
		for (int id = 0; id < chunks.length; id++) {
			float col = 1f - BASE_LUMINANCE_OFFSET;
			if ((id & 2) == 0) col -= BASE_LUMINANCE_OFFSET;

			if (phase >= PERIOD / 2 && (id == phase - (PERIOD / 2) || id == 1 + phase - (PERIOD / 2) || id == -1 + phase - (PERIOD / 2))) {
				col += BASE_LUMINANCE_OFFSET;
			}

			// Randomness offset
			col -= (0.025 * colourRandom.nextDouble());
			int colour = (int)(255 * col);

			int[] pos = chunks[id].getDrawLocation();
			int[] ddim = chunks[id].getDrawDimensions();
			int[] sloc = chunks[id].getSourceLocation();
			int[] sdim = chunks[id].getSourceDimensions();
			customHolder.drawImage(engine, imageName, pos[0], pos[1], ddim[0], ddim[1], sloc[0], sloc[1], sdim[0], sdim[1], colour, colour, colour, 255, 0);
		}
	}

	/**
	 * This last one is important. In the case that any of the child types are used, it allows identification.
	 * The identification can be used to allow casting during operations.
	 * @return Identification number of child class.
	 */
	@Override
	public int getID() {
		return ID;
	}
}
