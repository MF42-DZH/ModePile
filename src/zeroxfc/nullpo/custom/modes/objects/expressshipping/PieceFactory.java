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

	/**
	 * Creates a piece.
	 * @param id Piece ID. Use one of the static IDs in this class.
	 * @param x Pixel X-coordinate.
	 * @param y Pixel Y-coordinate.
	 * @return The piece, whose top-left located at (x, y) in the pixel grid.
	 */
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
			case PIECE_L4:
				return new L4(x, y);
			case PIECE_J4:
				return new J4(x, y);
			case PIECE_S4:
				return new S4(x, y);
			case PIECE_Z4:
				return new Z4(x, y);
			case PIECE_O4:
				return new O4(x, y);
			case PIECE_T4:
				return new T4(x, y);
			case PIECE_P5:
				return new P5(x, y);
			case PIECE_Q5:
				return new Q5(x, y);
			case PIECE_U5:
				return new U5(x, y);
			case PIECE_X5:
				return new X5(x, y);
			case PIECE_S5:
				return new S5(x, y);
			case PIECE_Z5:
				return new Z5(x, y);
			case PIECE_Stairs6:
				return new Stairs6(x, y);
			case PIECE_Cross6:
				return new Cross6(x, y);
			default:
				return null;
		}
	}
}
