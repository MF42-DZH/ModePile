package zeroxfc.nullpo.custom.libs.backgroundtypes;

import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;

import java.util.ArrayList;
import java.util.Collections;

public class BackgroundHorizontalBars extends AnimatedBackgroundHook {
	private static int AMT = 480 / 3;
	private static double TWO_PI = Math.PI * 2;
	private static float BASE_SCALE = 1f;
	private static float SCALE_VARIANCE = 1f;

	// private ResourceHolderCustomAssetExtension customHolder;
	private ImageChunk[] chunks;
	private int pulsePhaseMax;
	private int currentPulsePhase;
	private Float pulseBaseScale, pulseScaleVariance;
	private boolean reverse;

	{
		ID = AnimatedBackgroundHook.ANIMATION_PULSE_HORIZONTAL_BARS;
		setImageName("localBG");
	}

	public BackgroundHorizontalBars(int bgNumber, int pulseFrames, Integer sliceSize, Float pulseBaseScale, Float pulseScaleVariance, boolean reverse) {
		if (bgNumber < 0 || bgNumber > 19) bgNumber = 0;

		customHolder = new ResourceHolderCustomAssetExtension();
		customHolder.loadImage("res/graphics/back" + bgNumber + ".png", imageName);

		setup(pulseFrames, sliceSize, pulseBaseScale, pulseScaleVariance, reverse);

		log.debug("Non-custom horizontal bars background (" + bgNumber + ") created.");
	}

	public BackgroundHorizontalBars(String filePath, int pulseFrames, Integer sliceSize, Float pulseBaseScale, Float pulseScaleVariance, boolean reverse) {
		customHolder = new ResourceHolderCustomAssetExtension();
		customHolder.loadImage(filePath, imageName);

		setup(pulseFrames, sliceSize, pulseBaseScale, pulseScaleVariance, reverse);

		log.debug("Custom horizontal bars background created (File Path: " + filePath + ").");
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

	private void setup(int pulseFrames, Integer sliceSize, Float pulseBaseScale, Float pulseScaleVariance, boolean reverse) {
		if (pulseBaseScale == null || pulseScaleVariance == null || sliceSize == null) {
			chunks = new ImageChunk[AMT];
			for (int i = 0; i < chunks.length; i++) {
				chunks[i] = new ImageChunk(ImageChunk.ANCHOR_POINT_ML, new int[] { 0, ((480 / AMT) * i) + ((480 / AMT) / 2) }, new int[] { 0, (480 / AMT) * i }, new int[] { 640, (480 / AMT) }, new float[] { 1f, BASE_SCALE } );
			}

			this.reverse = reverse;
			pulsePhaseMax = pulseFrames;
			currentPulsePhase = pulsePhaseMax;
		} else {
			this.pulseBaseScale = pulseBaseScale;
			this.pulseScaleVariance = pulseScaleVariance;

			chunks = new ImageChunk[AMT];
			for (int i = 0; i < chunks.length; i++) {
				chunks[i] = new ImageChunk(ImageChunk.ANCHOR_POINT_ML, new int[] { 0, ((480 / sliceSize) * i) + ((480 / sliceSize) / 2) }, new int[] { 0, (480 / sliceSize) * i }, new int[] { 640, (480 / sliceSize) }, new float[] { 1f, pulseBaseScale } );
			}

			this.reverse = reverse;
			pulsePhaseMax = pulseFrames;
			currentPulsePhase = pulsePhaseMax;
		}

	}

	public void modifyValues(Integer pulseFrames, Float pulseBaseScale, Float pulseScaleVariance, Boolean reverse) {
		if (reverse != null) this.reverse = reverse;
		if (pulseFrames != null) pulsePhaseMax = pulseFrames;
		if (pulseBaseScale != null) this.pulseBaseScale = pulseBaseScale;
		if (pulseScaleVariance != null) this.pulseScaleVariance = pulseScaleVariance;

		if (currentPulsePhase > pulsePhaseMax) currentPulsePhase = pulsePhaseMax;
	}

	public void resetPulseScaleValues() {
		if (pulseBaseScale != null) pulseBaseScale = null;
		if (pulseScaleVariance != null) pulseScaleVariance = null;
	}

	@Override
	public void update() {
		currentPulsePhase = (currentPulsePhase + 1) % pulsePhaseMax;

		for (int i = 0; i < chunks.length; i++) {
			int j = i;
			if (reverse) j = chunks.length - i - 1;

			int ppu = (currentPulsePhase + i) % pulsePhaseMax;

			float baseScale = (pulseBaseScale == null) ? BASE_SCALE : pulseBaseScale;
			float scaleVariance = (pulseScaleVariance == null) ? SCALE_VARIANCE : pulseScaleVariance;

			double newScale = baseScale + (Math.sin(TWO_PI * ((double)ppu / pulsePhaseMax)) * scaleVariance);
			if (newScale < 1d) newScale = 1d;

			chunks[j].setScale(new float[] { 1f, (float)newScale });
		}
	}

	@Override
	public void reset() {
		currentPulsePhase = pulsePhaseMax;
		update();
	}

	@Override
	public void draw(GameEngine engine, int playerID) {
		ArrayList<ImageChunk> priorityList = new ArrayList<>();
		Collections.addAll(priorityList, chunks);
		priorityList.sort((c1, c2) -> Float.compare(c1.getScale()[1], c2.getScale()[1]));

		float baseScale = (pulseBaseScale == null) ? BASE_SCALE : pulseBaseScale;
		if (almostEqual(baseScale, 1, 0.005)) {
			customHolder.drawImage(engine, imageName, 0, 0);
			priorityList.removeIf(imageChunk -> almostEqual(imageChunk.getScale()[0], 1, 0.005));
		}
		for (ImageChunk i : priorityList) {
			int[] pos = i.getDrawLocation();
			int[] ddim = i.getDrawDimensions();
			int[] sloc = i.getSourceLocation();
			int[] sdim = i.getSourceDimensions();
			customHolder.drawImage(engine, imageName, pos[0], pos[1], ddim[0], ddim[1], sloc[0], sloc[1], sdim[0], sdim[1], 255, 255, 255, 255, 0);
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

