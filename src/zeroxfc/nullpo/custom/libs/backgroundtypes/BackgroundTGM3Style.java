package zeroxfc.nullpo.custom.libs.backgroundtypes;

import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;
import zeroxfc.nullpo.custom.libs.ValueWrapper;

import java.util.Random;

public class BackgroundTGM3Style extends AnimatedBackgroundHook {
	private static final int MAX_ROTATED_SCREEN_REQUIREMENT = (int)Math.ceil(Math.sin(45) * (640 + 480));

	private static final double MIN_ANGLE = -60d, MAX_ANGLE = 60d;
	private static final int MIN_TRAVEL_TIME = 600, MAX_TRAVEL_TIME = 1800;
	private static final float MIN_SCALE = 2f, MAX_SCALE = 4f;

	/**
	 * Inside each instance:
	 *
	 * valueDouble - angle (current / limit)
	 * valueFloat - scale (current / limit)
	 * valueInt - frame (timer / limit)
	 */
	private ValueWrapper lastValues, currentValues, targetValues;
	private Random valueRandomiser;

	/**
	 * Panning amount variables.
	 */
	private int[] lastPan, currentPan, targetPan;

	private int holderType, dimTimer;
	private String localPath;

	{
		ID = AnimatedBackgroundHook.ANIMATION_TGM3TI_STYLE;
		setImageName("localBG");
		holderType = getResourceHook();

		lastValues = new ValueWrapper();
		currentValues = new ValueWrapper();
		targetValues = new ValueWrapper();

		lastPan = new int[2];
		currentPan = new int[2];
		targetPan = new int[2];

		dimTimer = 0;
	}

	public BackgroundTGM3Style(GameEngine engine, String filePath, Random valueRandomiser) {
		customHolder = new ResourceHolderCustomAssetExtension(engine);
		customHolder.loadImage(filePath, imageName);
		customHolder.loadImage("res/graphics/blank_black.png", "blackBG");
		localPath = filePath;

		this.valueRandomiser = valueRandomiser;

		int[] imgDim = customHolder.getImageDimensions(imageName);
		if (holderType == HOLDER_SLICK) customHolder.setRotationCentre(imageName,(float)imgDim[0] / 2, (float)imgDim[1] / 2);

		reset();

		log.debug("TGM3-Style background created (File Path: " + filePath + ").");
	}

	public BackgroundTGM3Style(GameEngine engine, String filePath, long seed) {
		customHolder = new ResourceHolderCustomAssetExtension(engine);
		customHolder.loadImage(filePath, imageName);
		customHolder.loadImage("res/graphics/blank_black.png", "blackBG");
		localPath = filePath;

		this.valueRandomiser = new Random(seed);

		int[] imgDim = customHolder.getImageDimensions(imageName);
		if (holderType == HOLDER_SLICK) customHolder.setRotationCentre(imageName,(float)imgDim[0] / 2, (float)imgDim[1] / 2);

		reset();

		log.debug("TGM3-Style background created (File Path: " + filePath + ").");
	}

	/**
	 * Sets a new target rotation and panning location for the background image.
	 * Rather complex and somewhat heavy.
	 */
	private void setNewTarget() {
		// Set current as last for LERP.
		lastPan[0] = currentPan[0];
		lastPan[1] = currentPan[1];
		lastValues.copy(currentValues);

		// Reset frame timer
		currentValues.valueInt = 0;

		// Set new time limit
		targetValues.valueInt = valueRandomiser.nextInt(MAX_TRAVEL_TIME - MIN_TRAVEL_TIME + 1) + MIN_TRAVEL_TIME;

		if (holderType == HOLDER_SLICK) {
			// Set new rotation
			targetValues.valueDouble = (valueRandomiser.nextDouble() * (MAX_ANGLE - MIN_ANGLE)) + MIN_ANGLE;
		}

		// Set new scale
		float ns;
		do {
			ns = (float)(valueRandomiser.nextDouble() * (MAX_SCALE - MIN_SCALE)) + MIN_SCALE;
		} while (!almostEqual(ns, currentValues.valueFloat, 1f));
		targetValues.valueFloat = ns;


		// Find max pan from centre
		int[] imgDim = customHolder.getImageDimensions(imageName);
		imgDim[0] *= MIN_SCALE;
		imgDim[1] *= MIN_SCALE;

		int[] differences;

		if (holderType == HOLDER_SLICK) {
			differences = new int[] { imgDim[0] - MAX_ROTATED_SCREEN_REQUIREMENT, imgDim[1] - MAX_ROTATED_SCREEN_REQUIREMENT };
		} else {
			differences = new int[] { imgDim[0] - 640, imgDim[1] - 480 };
		}

		if (holderType == HOLDER_SLICK) {
			differences[0] /= 16;
			differences[1] /= 16;
		} else {
			differences[0] /= 2;
			differences[1] /= 2;
			differences[0] *= targetValues.valueFloat; differences[1] *= targetValues.valueFloat;
		}



		// Set new target pan
		double coefficientX, coefficientY;
		coefficientX = (valueRandomiser.nextDouble() - 0.5d) * 2;
		coefficientY = (valueRandomiser.nextDouble() - 0.5d) * 2;

		targetPan[0] = (int)(differences[0] * coefficientX);
		targetPan[1] = (int)(differences[1] * coefficientY);
	}

	public void setSeed(long seed) {
		valueRandomiser = new Random(seed);
		reset();
	}

	@Override
	public void update() {
		currentValues.valueInt++;
		if (currentValues.valueInt > targetValues.valueInt) {
			currentValues.valueInt = 0;
			setNewTarget();
		} else {
			double t = (double)currentValues.valueInt / (double)targetValues.valueInt;

			if (holderType == HOLDER_SLICK) {
				currentValues.valueDouble = Interpolation.lerp(lastValues.valueDouble, targetValues.valueDouble, t);
				customHolder.setRotation(imageName, currentValues.valueDouble.floatValue());
			}
			currentValues.valueFloat = Interpolation.lerp(lastValues.valueFloat, targetValues.valueFloat, t);

			currentPan[0] = Interpolation.lerp(lastPan[0], targetPan[0], t);
			currentPan[1] = Interpolation.lerp(lastPan[1], targetPan[1], t);
		}

		if (dimTimer > 0) changeImage();
	}

	private void changeImage() {
		dimTimer--;
		if (dimTimer == 15) {
			customHolder.copyImage("transitory", imageName);
			reset();
		}
	}

	@Override
	public void reset() {
		lastValues = new ValueWrapper();
		currentValues = new ValueWrapper();
		targetValues = new ValueWrapper();

		lastValues.valueFloat = 1f;
		currentValues.valueFloat = 1f;
		targetValues.valueFloat = 1f;

		lastPan = new int[2];
		currentPan = new int[2];
		targetPan = new int[2];

		customHolder.setRotation(imageName, 0);

		setNewTarget();
	}

	@Override
	public void draw(GameEngine engine, int playerID) {
		customHolder.drawImage(engine, "blackBG", 0, 0);

		int[] rawImgDim = customHolder.getImageDimensions(imageName);
		int[] imgDim = customHolder.getImageDimensions(imageName);

		// log.debug(String.format("%d, %d", imgDim[0], imgDim[1]));

		imgDim[0] *= currentValues.valueFloat; imgDim[1] *= currentValues.valueFloat;

		int v = 255;
		if (dimTimer > 0) {
			int t = dimTimer - 15;
			v = Interpolation.lerp(0, 255,(double)Math.abs(t) / 15d);
		}

		customHolder.drawImage(engine, imageName, currentPan[0] - (rawImgDim[0] / 2), currentPan[1] - (rawImgDim[1] / 2), imgDim[0], imgDim[1], 0, 0, rawImgDim[0], rawImgDim[1], v, v, v, 255, 0);
	}

	@Override
	public void setBG(int bg) {
		log.warn("TGM3-Style backgrounds do not support the default backgrounds due to their small size.");
		log.info("Minimum recommended size: 1024 x 1024.");
	}

	@Override
	public void setBG(String filePath) {
		if (filePath != localPath) {
			int[] dimOld = customHolder.getImageDimensions(imageName);

			customHolder.loadImage(filePath, "transitory");

			int[] dim = customHolder.getImageDimensions("transitory");

			if (dimOld[0] != dim[0] || dimOld[1] != dim[1]) {
				log.warn("Using differently-sized backgrounds stop seamless transitions from occurring.");
			}

			if (dim[0] < 1024 || dim[1] < 1024) {
				// Too small.
				log.warn("Background size is smaller than recommended minimum size.");
				log.info("Minimum recommended size: 1024 x 1024.");
			} else {
				// Successful.
				dimTimer = 30;

				localPath = filePath;
				if (holderType == HOLDER_SLICK) customHolder.setRotationCentre(imageName,(float)dim[0] / 2, (float)dim[1] / 2);

				log.debug("TGM3-Sytle background modified (New File Path: " + filePath + ").");
			}
		}
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
