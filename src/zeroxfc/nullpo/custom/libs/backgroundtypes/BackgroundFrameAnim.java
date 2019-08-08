package zeroxfc.nullpo.custom.libs.backgroundtypes;

import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;

public class BackgroundFrameAnim extends AnimatedBackgroundHook {
	public static final int SEQUENCE_LINEAR_HORIZONTAL = 0;
	public static final int SEQUENCE_LINEAR_VERTICAL = 1;
	public static final int SEQUENCE_GRID_HFTV = 2;
	public static final int SEQUENCE_GRID_VFTH = 3;

	private ImageChunk[] chunkSequence;
	private ResourceHolderCustomAssetExtension customHolder;
	private int frameTime, currentTick, type, frameCount, currentFrame;
	private boolean pingPong, forward;

	{
		ID = AnimatedBackgroundHook.ANIMATION_FRAME_ANIM;
	}

	public BackgroundFrameAnim(GameEngine engine, String filePath, int type, int frameTime, boolean pingPong) {
		customHolder = new ResourceHolderCustomAssetExtension(engine);
		customHolder.loadImage(filePath, "localBG");

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
				int[] hDim = customHolder.getImageDimensions("localBG");
				int hAmount = hDim[0] / 640;

				chunkSequence = new ImageChunk[hAmount];
				for (int i = 0; i < hAmount; i++) {
					chunkSequence[i] = new ImageChunk(ImageChunk.ANCHOR_POINT_TL, new int[] { 0, 0 }, new int[] { i * 640, 0 }, new int[] { 640, 480 }, new float[] { 1f, 1f });
				}

				frameCount = hAmount;
				break;
			case SEQUENCE_LINEAR_VERTICAL:
				int[] vDim = customHolder.getImageDimensions("localBG");
				int vAmount = vDim[1] / 480;

				chunkSequence = new ImageChunk[vAmount];
				for (int i = 0; i < vAmount; i++) {
					chunkSequence[i] = new ImageChunk(ImageChunk.ANCHOR_POINT_TL, new int[] { 0, 0 }, new int[] { 0, i * 480 }, new int[] { 640, 480 }, new float[] { 1f, 1f });
				}

				frameCount = vAmount;
				break;
			case SEQUENCE_GRID_HFTV:
				int[] gDim1 = customHolder.getImageDimensions("localBG");
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
				int[] gDim2 = customHolder.getImageDimensions("localBG");
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
	public void draw(GameEngine engine) {
		ImageChunk i = chunkSequence[currentFrame];
		int[] pos = i.getDrawLocation();
		int[] ddim = i.getDrawDimensions();
		int[] sloc = i.getSourceLocation();
		int[] sdim = i.getSourceDimensions();
		customHolder.drawImage(engine, "localBG", pos[0], pos[1], ddim[0], ddim[1], sloc[0], sloc[1], sdim[0], sdim[1], 255, 255, 255, 255, 0);
	}

	@Override
	public void setBG(int bg) {
		log.warn("Frame animation backgrounds do not support in-game backgrounds.");
	}

	@Override
	public void setBG(String filePath) {
		customHolder.loadImage(filePath, "localBG");
		setup();
	}

	@Override
	public int getID() {
		return ID;
	}
}
