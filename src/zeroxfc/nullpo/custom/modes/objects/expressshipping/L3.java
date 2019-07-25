package zeroxfc.nullpo.custom.modes.objects.expressshipping;

import mu.nu.nullpo.game.component.Block;

import java.util.ArrayList;

public class L3 implements GamePiece {
	private static int[][][] contents = {
			new int[][] {
					new int[] { 1, 0 },
					new int[] { 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1 },
					new int[] { 1, 0 }
			},
			new int[][] {
					new int[] { 1, 1 },
					new int[] { 0, 1 }
			},
			new int[][] {
					new int[] { 0, 1 },
					new int[] { 1, 1 }
			},
	};

	private int[] location;
	private int state;

	public L3(int x, int y) {
		location = new int[] { x, y };
		state = 0;
	}

	@Override
	public int getScore() {
		return 75;
	}

	@Override
	public int getWidth() {
		return 2;
	}

	@Override
	public int getHeight() {
		return 2;
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
		return new int[] { 0, 0 };
	}

	@Override
	public double getConveyorYOffset() {
		return 0.5;
	}

	@Override
	public int getState() {
		return state;
	}

	@Override
	public int getColour() {
		return Block.BLOCK_COLOR_GRAY;
	}

	@Override
	public int[][] getConveyorBoundingBox() {
		int minX, minY, maxX, maxY;

		minX = getWidth();
		minY = getHeight();
		maxX = 0;
		maxY = 0;

		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				if (contents[state][y][x] != 0) {
					if (x < minX) minX = x;
					if (y < minY) minY = y;
					if (x > maxX) maxX = x;
					if (y > maxY) maxY = y;
				}
			}
		}

		maxX++;
		maxY++;

		int[] tlc, brc;
		tlc = new int[]{
				location[0] + (minX * 16),
				location[1] + (minY * 16) - (int)(getConveyorYOffset() * 16)
		};
		brc = new int[]{
				location[0] + (maxX * 16),
				location[1] + (maxY * 16) - (int)(getConveyorYOffset() * 16)
		};

		return new int[][] { tlc, brc };
	}

	@Override
	public int[][][] getCursorBoundingBox() {
		ArrayList<int[][]> boxes = new ArrayList<>();

		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				if (contents[state][y][x] != 0) {
					boxes.add(
							new int[][] {
									new int[] {
											location[0] + (x * 16),
											location[1] + (y * 16) - (int)(getConveyorYOffset() * 16)
									},
									new int[] {
											location[0] + ((x + 1) * 16),
											location[1] + ((y + 1) * 16) - (int)(getConveyorYOffset() * 16)
									}
							}
					);
				}
			}
		}

		int[][][] arr =  new int[boxes.size()][][];

		int i = 0;
		for (int[][] iss: boxes) {
			arr[i] = iss.clone();
			i++;
		}

		return arr;
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
