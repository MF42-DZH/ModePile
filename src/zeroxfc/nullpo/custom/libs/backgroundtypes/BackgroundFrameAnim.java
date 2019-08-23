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
 *     - Only binaries of this library are allowed to be distributed unless at the
 *       permission of the Library Creator.
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
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;

public class BackgroundFrameAnim extends AnimatedBackgroundHook {
	public static final int SEQUENCE_LINEAR_HORIZONTAL = 0;
	public static final int SEQUENCE_LINEAR_VERTICAL = 1;
	public static final int SEQUENCE_GRID_HFTV = 2;
	public static final int SEQUENCE_GRID_VFTH = 3;

	private ImageChunk[] chunkSequence;
	// private ResourceHolderCustomAssetExtension customHolder;
	private int frameTime, currentTick, type, frameCount, currentFrame;
	private boolean pingPong, forward;

	{
		ID = AnimatedBackgroundHook.ANIMATION_FRAME_ANIM;
		setImageName("localBG");
	}

	public BackgroundFrameAnim(String filePath, int type, int frameTime, boolean pingPong) {
		customHolder = new ResourceHolderCustomAssetExtension();
		customHolder.loadImage(filePath, imageName);

		this.type = type;
		this.frameTime = frameTime;
		this.pingPong = pingPong;

		setup();

		log.debug("Type " + type + " frame animation background created (File Path: " + filePath + ").");
	}

	private void setup() {
		forward = true;
		currentFrame = 0;
		currentTick = 0;

		switch (type) {
			case SEQUENCE_LINEAR_HORIZONTAL:
				int[] hDim = customHolder.getImageDimensions(imageName);
				int hAmount = hDim[0] / 640;

				chunkSequence = new ImageChunk[hAmount];
				for (int i = 0; i < hAmount; i++) {
					chunkSequence[i] = new ImageChunk(ImageChunk.ANCHOR_POINT_TL, new int[] { 0, 0 }, new int[] { i * 640, 0 }, new int[] { 640, 480 }, new float[] { 1f, 1f });
				}

				frameCount = hAmount;
				break;
			case SEQUENCE_LINEAR_VERTICAL:
				int[] vDim = customHolder.getImageDimensions(imageName);
				int vAmount = vDim[1] / 480;

				chunkSequence = new ImageChunk[vAmount];
				for (int i = 0; i < vAmount; i++) {
					chunkSequence[i] = new ImageChunk(ImageChunk.ANCHOR_POINT_TL, new int[] { 0, 0 }, new int[] { 0, i * 480 }, new int[] { 640, 480 }, new float[] { 1f, 1f });
				}

				frameCount = vAmount;
				break;
			case SEQUENCE_GRID_HFTV:
				int[] gDim1 = customHolder.getImageDimensions(imageName);
				int hCells1 = gDim1[0] / 640;
				int vCells1 = gDim1[1] / 480;

				chunkSequence = new ImageChunk[vCells1 * hCells1];
				for (int y = 0; y < vCells1; y++) {
					for (int x = 0; x < hCells1; x++) {
						int chunk = (y * vCells1) + hCells1;
						chunkSequence[chunk] = new ImageChunk(ImageChunk.ANCHOR_POINT_TL, new int[] { 0, 0 }, new int[] { 640 * x, 480 * x }, new int[] { 640, 480 }, new float[] { 1f, 1f });
					}
				}

				frameCount = hCells1 * vCells1;
				break;
			case SEQUENCE_GRID_VFTH:
				int[] gDim2 = customHolder.getImageDimensions(imageName);
				int hCells2 = gDim2[0] / 640;
				int vCells2 = gDim2[1] / 480;

				chunkSequence = new ImageChunk[vCells2 * hCells2];
				for (int x = 0; x < hCells2; x++) {
					for (int y = 0; y < vCells2; y++) {
						int chunk = (y * hCells2) + vCells2;
						chunkSequence[chunk] = new ImageChunk(ImageChunk.ANCHOR_POINT_TL, new int[] { 0, 0 }, new int[] { 640 * x, 480 * x }, new int[] { 640, 480 }, new float[] { 1f, 1f });
					}
				}

				frameCount = hCells2 * vCells2;
				break;
			default:
				break;
		}
	}

	public void setSpeed(int frameTime) {
		this.frameTime = frameTime;
		reset();
	}

	public void setPingPong(boolean pingPong) {
		this.pingPong = pingPong;
		reset();
	}

	@Override
	public void update() {
		currentTick++;
		if (currentTick >= frameTime) {
			currentTick = 0;

			if (pingPong) {
				if (forward) currentFrame++;
				else currentFrame--;

				if (currentFrame >= frameCount) {
					currentFrame -= 2;
					forward = false;
				} else if (currentFrame < 0) {
					currentFrame++;
					forward = true;
				}
			} else {
				currentFrame = (currentFrame + 1) % frameCount;
			}
		}
	}

	@Override
	public void reset() {
		forward = true;
		currentFrame = 0;
		currentTick = 0;
	}

	@Override
	public void draw(GameEngine engine, int playerID) {
		ImageChunk i = chunkSequence[currentFrame];
		int[] pos = i.getDrawLocation();
		int[] ddim = i.getDrawDimensions();
		int[] sloc = i.getSourceLocation();
		int[] sdim = i.getSourceDimensions();
		customHolder.drawImage(engine, imageName, pos[0], pos[1], ddim[0], ddim[1], sloc[0], sloc[1], sdim[0], sdim[1], 255, 255, 255, 255, 0);
	}

	@Override
	public void setBG(int bg) {
		log.warn("Frame animation backgrounds do not support in-game backgrounds.");
	}

	@Override
	public void setBG(String filePath) {
		customHolder.loadImage(filePath, imageName);
		setup();
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
