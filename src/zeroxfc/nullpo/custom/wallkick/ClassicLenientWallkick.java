package zeroxfc.nullpo.custom.wallkick;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.WallkickResult;
import mu.nu.nullpo.game.subsystem.wallkick.Wallkick;

public class ClassicLenientWallkick implements Wallkick {
	private static final int[][] BASE_WALLKICK = {
		{-1, 0},
		{ 1, 0},
		{ 0, 1},
		{ 0,-1}
	};
	private static final int[][] I_WALLKICK = {
		{-1, 0},
		{ 1, 0},
		{-2, 0},
		{ 2, 0},
		{ 0, 1},
		{ 0,-1}
	};
	
	/**
	 * Execute a wallkick
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param rtDir Rotation button used (-1: left rotation, 1: right rotation, 2: 180-degree rotation)
	 * @param rtOld Direction before rotation
	 * @param rtNew Direction after rotation
	 * @param allowUpward If true, upward wallkicks are allowed.
	 * @param piece Current piece
	 * @param field Current field
	 * @param ctrl Button input status (it may be null, when controlled by an AI)
	 * @return WallkickResult object, or null if you don't want a kick
	 */
	public WallkickResult executeWallkick(int x, int y, int rtDir, int rtOld, int rtNew, boolean allowUpward, Piece piece, Field field, Controller ctrl) {
		int x2, y2;
		
		int[][] WALLKICK = (piece.id == Piece.PIECE_I) ? I_WALLKICK : BASE_WALLKICK;

		for (int i = 0; i < WALLKICK.length; i++)
		{
			if (rtDir < 0 || rtDir == 2)
			{
				x2 = WALLKICK[i][0];
			}
			else
			{
				x2 = -WALLKICK[i][0];
			}
			y2 = WALLKICK[i][1];

			if (piece.big)
			{
				x2 *= 2; y2 *= 2;
			}

			if(piece.checkCollision(x + x2, y + y2, rtNew, field) == false) {
				return new WallkickResult(x2, y2, rtNew);
			}
		}
		return null;
	}
}
