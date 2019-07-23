package zeroxfc.nullpo.custom.modes.objects.expressshipping;

public class PieceFactory {
	/** Pieces Available */
	public static final int PIECE_I1 = 0,
	                        PIECE_I2 = 1,
	                        PIECE_I3 = 2,
	                        PIECE_L3 = 3,
	                        PIECE_I4 = 4,
	                        PIECE_L4 = 5,
	                        PIECE_J4 = 6,
	                        PIECE_S4 = 7,
	                        PIECE_Z4 = 8,
	                        PIECE_O4 = 9,
	                        PIECE_T4 = 10,
	                        PIECE_P5 = 11,
	                        PIECE_Q5 = 12,
	                        PIECE_U5 = 13,
	                        PIECE_X5 = 14,
	                        PIECE_S5 = 15,
	                        PIECE_Z5 = 16,
	                        PIECE_Stairs6 = 17,
	                        PIECE_Cross6 = 18;

	public static final int PIECE_COUNT = 19;

	// TODO: Make piece creation algorithm here.
	public static GamePiece getPiece(int id, int x, int y) {
		switch (id) {
			case PIECE_I1:
				return new I1(x, y);
			case PIECE_I2:
				return new I2(x, y);
			case PIECE_I3:
				return new I3(x, y);
			case PIECE_L3:
				return new L3(x, y);
			case PIECE_I4:
				return new I4(x, y);
			case PIECE_O4:
				return new O4(x, y);
			case PIECE_T4:
				return new T4(x, y);
			default:
				return null;
		}
	}
}
