/*
 * This library class was created by 0xFC963F18DC21 / Shots243
 * It is part of an extension library for the game NullpoMino (copyright 2010)
 *
 * Herewith shall the term "Library Creator" be given to 0xFC963F18DC21.
 * Herewith shall the term "Game Creator" be given to the original creator of NullpoMino.
 *
 * THIS LIBRARY AND MODE PACK WAS NOT MADE IN ASSOCIATION WITH THE GAME CREATOR.
 *
 * Repository: https://github.com/Shots243/ModePile
 *
 * When using this library in a mode / library pack of your own, the following
 * conditions must be satisfied:
 *     - This license must remain visible at the top of the document, unmodified.
 *     - You are allowed to use this library for any modding purpose.
 *         - If this is the case, the Library Creator must be credited somewhere.
 *             - Source comments only are fine, but in a README is recommended.
 *     - Modification of this library is allowed, but only in the condition that a
 *       pull request is made to merge the changes to the repository.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
	private static final float MIN_SCALE = 1f, MAX_SCALE = 4f;

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
	private boolean hasUpdated;

	{
		ID = AnimatedBackgroundHook.ANIMATION_TGM3TI_STYLE;
		setImageName("localBG");
		holderType = getResourceHook();

		lastValues = new ValueWrapper();
		currentValues = new ValueWrapper();
		currentValues.valueFloat = MIN_SCALE;

		targetValues = new ValueWrapper();

		lastPan = new int[2];
		currentPan = new int[2];
		targetPan = new int[2];

		dimTimer = 0;
		hasUpdated = true;
	}

	public BackgroundTGM3Style(String filePath, Random valueRandomiser) {
		customHolder = new ResourceHolderCustomAssetExtension();
		customHolder.loadImage(filePath, imageName);
		customHolder.loadImage("res/graphics/blank_black_24b.png", "blackBG");
		localPath = filePath;

		this.valueRandomiser = valueRandomiser;

		int[] imgDim = customHolder.getImageDimensions(imageName);
		if (holderType == HOLDER_SLICK) customHolder.setRotationCentre(imageName,(float)imgDim[0] / 2, (float)imgDim[1] / 2);

		reset();

		log.debug("TGM3-Style background created (File Path: " + filePath + ").");
	}

	public BackgroundTGM3Style(String filePath, long seed) {
		customHolder = new ResourceHolderCustomAssetExtension();
		customHolder.loadImage(filePath, imageName);
		customHolder.loadImage("res/graphics/blank_black_24b.png", "blackBG");
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

		int[] differences;

		if (holderType == HOLDER_SLICK) {
			differences = new int[] { imgDim[0] - MAX_ROTATED_SCREEN_REQUIREMENT, imgDim[1] - MAX_ROTATED_SCREEN_REQUIREMENT };
		} else {
			differences = new int[] { imgDim[0] - 640, imgDim[1] - 480 };
		}

		differences[0] /= 4;
		differences[1] /= 4;
		differences[0] *= targetValues.valueFloat; differences[1] *= targetValues.valueFloat;

		// Set new target pan
		double r = (differences[0] * differences[1]) / Math.sqrt( (differences[0] * differences[0] * Math.sin(targetValues.valueDouble) * Math.sin(targetValues.valueDouble)) + (differences[1] * differences[1] * Math.cos(targetValues.valueDouble) * Math.cos(targetValues.valueDouble)) );
		double coefficientX, coefficientY;
		do {
			coefficientX = (valueRandomiser.nextDouble() - 0.5d) * 2;
			coefficientY = (valueRandomiser.nextDouble() - 0.5d) * 2;

			targetPan[0] = (int) (differences[0] * coefficientX);
			targetPan[1] = (int) (differences[1] * coefficientY);
		} while (
				Math.sqrt(Math.pow(targetPan[0], 2) + Math.pow(targetPan[1], 2)) > r
		);
	}

	public void setSeed(long seed) {
		valueRandomiser = new Random(seed);
		reset();
	}

	@Override
	public void update() {
		hasUpdated = true;
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
			int[] dim = customHolder.getImageDimensions("transitory");
			customHolder.copyImage("transitory", imageName);
			if (holderType == HOLDER_SLICK) customHolder.setRotationCentre(imageName,(float)dim[0] / 2, (float)dim[1] / 2);
			reset();
		}
	}

	@Override
	public void reset() {
		if (hasUpdated) {
			lastValues = new ValueWrapper();
			currentValues = new ValueWrapper();
			currentValues.valueFloat = MIN_SCALE;
			targetValues = new ValueWrapper();

			lastValues.valueFloat = 1f;
			currentValues.valueFloat = MIN_SCALE;
			targetValues.valueFloat = 1f;

			lastPan = new int[2];
			currentPan = new int[2];
			targetPan = new int[2];

			customHolder.setRotation(imageName, 0);

			setNewTarget();
			hasUpdated = false;
		}
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

		customHolder.drawImage(engine, imageName, currentPan[0] - ((imgDim[0] / 2) - 320), currentPan[1] - ((imgDim[1] / 2) - 240), imgDim[0], imgDim[1], 0, 0, rawImgDim[0], rawImgDim[1], v, v, v, 255, 0);
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

				log.debug("TGM3-Sytle background modified (New File Path: " + filePath + ").");
			}
		}
	}

	/**
	 * Allows the hot-swapping of pre-loaded BGs from a storage instance of a <code>ResourceHolderCustomAssetExtension</code>.
	 * @param holder Storage instance
	 * @param name Image name
	 */
	public void setBGFromHolder(ResourceHolderCustomAssetExtension holder, String name) {
		final Object image = holder.getImageAt(name);
		if (image == null) return;
		if (!name.equals(localPath)) {
			int[] dimOld = customHolder.getImageDimensions(imageName);

			customHolder.putImageAt(image, "transitory");

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
				localPath = name;

				log.debug("TGM3-Sytle background modified (New Image Name: " + name + ").");
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
