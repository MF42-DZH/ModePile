package zeroxfc.nullpo.custom.modes.objects.expressshipping;

import mu.nu.nullpo.game.component.Block;

public class P5 implements GamePiece {
	private static int[][][] contents = {
			new int[][] {
					new int[] { 0, 1, 1 },
					new int[] { 0, 1, 1 },
					new int[] { 0, 1, 0 }
			},
			new int[][] {
					new int[] { 0, 0, 0 },
					new int[] { 1, 1, 1 },
					new int[] { 0, 1, 1 },
			},
			new int[][] {
					new int[] { 0, 1, 0 },
					new int[] { 1, 1, 0 },
					new int[] { 1, 1, 0 }
			},
			new int[][] {
					new int[] { 1, 1, 0 },
					new int[] { 1, 1, 1 },
					new int[] { 0, 0, 0 },
			},
	};

	private int[] location;
	private int state;

	public P5(int x, int y) {
		location = new int[] { x, y };
		state = 0;
	}

	@Override
	public int getScore() {
		return 350;
	}

	@Override
	public int getWidth() {
		return (state == 0 || state == 2) ? 2 : 3;
	}

	@Override
	public int getHeight() {
		return (state == 0 || state == 2) ? 3 : 2;
	}

	@Override
	public int getX() {
		return location[0];
	}

	@Override
	public int getY() {
		return location[1];
	}

	@Override
	public int[] getLocation() {
		return location;
	}

	@Override
	public int[][] getContents() {
		return contents[state];
	}

	@Override
	public int[] getCursorOffset() {
		return new int[] { 1, 1 };
	}

	@Override
	public double getConveyorYOffset() {
		return 1.0;
	}
	@Override
	public int getState() {
		return state;
	}

	@Override
	public int getColour() {
		return Block.BLOCK_COLOR_PURPLE;
	}

	@Override
	public void rotate() {
		state = (state + 1) % 4;
	}

	@Override
	public void setLocation(int x, int y) {
		location = new int[] { x, y };
	}
}
