package zeroxfc.nullpo.custom.modes.objects.expressshipping;

public interface GamePiece {
	int getScore();
	int getWidth();
	int getHeight();
	int getX();
	int getY();
	int[] getLocation();
	int[][] getContents();
	int getState();

	void rotate();
	void setLocation(int x, int y);
}
