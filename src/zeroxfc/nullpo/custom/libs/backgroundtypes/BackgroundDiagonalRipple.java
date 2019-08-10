package zeroxfc.nullpo.custom.libs.backgroundtypes;

import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;

import java.util.ArrayList;
import java.util.Collections;

public class BackgroundDiagonalRipple extends AnimatedBackgroundHook {
	private static final double TWO_PI = Math.PI * 2;
	private static final int DEF_FIELD_DIM = 8;
	private static final int DEF_GRID_WIDTH = 640 / DEF_FIELD_DIM;
	private static final int DEF_GRID_HEIGHT = 480 / DEF_FIELD_DIM;
	private static float BASE_SCALE = 1f;
	private static float SCALE_VARIANCE = 1f;

	private ImageChunk[][] chunkGrid;
	private boolean reverse;
	private boolean reverseSlant;
	private int pulsePhaseMax, currentPulsePhase;
	private Float pulseBaseScale, pulseScaleVariance;

	{
		ID = AnimatedBackgroundHook.ANIMATION_DIAGONAL_RIPPLE;
		setImageName("localBG");
	}

	public BackgroundDiagonalRipple(GameEngine engine, int bgNumber, Integer cellWidth, Integer cellHeight, int pulseFrames, Float pulseBaseScale, Float pulseScaleVariance, boolean reverse, boolean reverseSlant) {
		if (bgNumber < 0 || bgNumber > 19) bgNumber = 0;

		customHolder = new ResourceHolderCustomAssetExtension(engine);
		customHolder.loadImage("res/graphics/back" + bgNumber + ".png", imageName);

		setup(cellWidth, cellHeight, pulseFrames, pulseBaseScale, pulseScaleVariance, reverse, reverseSlant);

		log.debug("Non-custom diagonal ripple background (" + bgNumber + ") created.");
	}

	public BackgroundDiagonalRipple(GameEngine engine, String filePath, Integer cellWidth, Integer cellHeight, int pulseFrames, Float pulseBaseScale, Float pulseScaleVariance, boolean reverse, boolean reverseSlant) {
		customHolder = new ResourceHolderCustomAssetExtension(engine);
		customHolder.loadImage(filePath, imageName);

		setup(cellWidth, cellHeight, pulseFrames, pulseBaseScale, pulseScaleVariance, reverse, reverseSlant);

		log.debug("Custom diagonal ripple background created (File Path: " + filePath + ").");
	}

	public void modifyValues(Integer pulseFrames, Float pulseBaseScale, Float pulseScaleVariance, Boolean reverse, Boolean reverseSlant) {
		if (reverse != null) this.reverse = reverse;
		if (reverseSlant != null) this.reverseSlant = reverseSlant;
		if (pulseFrames != null) pulsePhaseMax = pulseFrames;
		if (pulseBaseScale != null) this.pulseBaseScale = pulseBaseScale;
		if (pulseScaleVariance != null) this.pulseScaleVariance = pulseScaleVariance;

		if (currentPulsePhase > pulsePhaseMax) currentPulsePhase = pulsePhaseMax;
	}

	public void resetPulseScaleValues() {
		if (pulseBaseScale != null) pulseBaseScale = null;
		if (pulseScaleVariance != null) pulseScaleVariance = null;
	}

	private void setup(Integer cellWidth, Integer cellHeight, int pulseFrames, Float pulseBaseScale, Float pulseScaleVariance, boolean reverse, boolean reverseSlant) {
		this.reverse = reverse;
		this.reverseSlant = reverseSlant;
		pulsePhaseMax = pulseFrames;
		currentPulsePhase = pulsePhaseMax;

		if (pulseBaseScale == null || pulseScaleVariance == null || cellWidth == null || cellHeight == null) {
			chunkGrid = new ImageChunk[DEF_GRID_HEIGHT][DEF_GRID_WIDTH];
			for (int y = 0; y < DEF_GRID_HEIGHT; y++) {
				for (int x = 0; x < DEF_GRID_WIDTH; x++) {
					chunkGrid[y][x] = new ImageChunk(ImageChunk.ANCHOR_POINT_MM, new int[] { (DEF_FIELD_DIM * x) + (DEF_FIELD_DIM / 2), (DEF_FIELD_DIM * y) + (DEF_FIELD_DIM / 2) }, new int[] { (DEF_FIELD_DIM * x), (DEF_FIELD_DIM * y) }, new int[] { DEF_FIELD_DIM, DEF_FIELD_DIM }, new float[] { BASE_SCALE, BASE_SCALE } );
				}
			}
		} else {
			this.pulseBaseScale = pulseBaseScale;
			this.pulseScaleVariance = pulseScaleVariance;

			int w;
			if (640 % cellWidth != 0) w = 8;
			else w = 640 / cellWidth;

			int h;
			if (480 % cellHeight != 0) h = 8;
			else h = 480 / cellHeight;

			chunkGrid = new ImageChunk[h][w];
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					chunkGrid[y][x] = new ImageChunk(ImageChunk.ANCHOR_POINT_MM, new int[] { (cellWidth * x) + (cellWidth / 2), (cellHeight * y) + (cellHeight / 2) }, new int[] { (cellWidth * x), (cellHeight * y) }, new int[] { cellWidth, cellHeight }, new float[] { pulseBaseScale, pulseBaseScale } );
				}
			}
		}

	}

	@Override
	public void update() {
		currentPulsePhase = (currentPulsePhase + 1) % pulsePhaseMax;

		for (int y = 0; y < chunkGrid.length; y++) {
			for (int x = 0; x < chunkGrid[y].length; x++) {
				int j = currentPulsePhase;
				if (reverse) j = pulsePhaseMax - currentPulsePhase - 1;

				if (reverseSlant) {
					j -= (x + y);
				} else {
					j += (x + y);
				}

				int ppu = j % pulsePhaseMax;
				if (ppu < 0) ppu = pulsePhaseMax - ppu;

				float baseScale = (pulseBaseScale == null) ? BASE_SCALE : pulseBaseScale;
				float scaleVariance = (pulseScaleVariance == null) ? SCALE_VARIANCE : pulseScaleVariance;

				double newScale = baseScale + (Math.sin(TWO_PI * ((double)ppu / pulsePhaseMax)) * scaleVariance);
				if (newScale < 1d) newScale = 1d;

				chunkGrid[y][x].setScale(new float[] { (float)newScale, (float)newScale });
			}
		}
	}

	@Override
	public void reset() {
		currentPulsePhase = pulsePhaseMax;
		update();
	}

	@Override
	public void draw(GameEngine engine) {
		ArrayList<ImageChunk> priorityList = new ArrayList<>();
		for (ImageChunk[] imageChunks : chunkGrid) {
			Collections.addAll(priorityList, imageChunks);
		}
		priorityList.sort((c1, c2) -> Float.compare(c1.getScale()[0], c2.getScale()[0]));

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

	@Override
	public void setBG(int bg) {
		customHolder.loadImage("res/graphics/back" + bg + ".png", imageName);
		log.debug("Non-custom diagonal ripple background modified (New BG: " + bg + ").");
	}

	@Override
	public void setBG(String filePath) {
		customHolder.loadImage(filePath, imageName);
		log.debug("Custom diagonal ripple background modified (New File Path: " + filePath + ").");
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
