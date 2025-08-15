package zeroxfc.nullpo.custom.wallkick;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.WallkickResult;
import mu.nu.nullpo.game.subsystem.wallkick.Wallkick;

public class STX1Wallkick implements Wallkick {
    // These kicks are for CW. CCW just inverts the X direction.
    private static final int[][] BASE_ROTATION_KICKS = {
        { -1, 0 }, { 1, 0 },
        { 0, 1 }, { -1, 1 }, { 1, 1 },
        { 0, -1 },
    };

    // Check rtOld for if the I-piece is flat or not. Same comment regarding direction.
    private static final int[][] CW_ROTATION_KICKS_I_PIECE_FLAT = {
        { -1, 0 }, { 1, 0 },
        { 0, 1 }, { -1, 1 }, { 1, 1 },
        { 0, 2 },
        { 0, -2 }
    };

    // Same comment regarding direction.
    private static final int[][] CW_ROTATION_KICKS_I_PIECE_VERTICAL = {
        { -1, 0 }, { 1, 0 },
        { 0, 1 }, { -1, 1 }, { 1, 1 },
        { -2, 0 }, { 2, 0 }
    };

    @Override
    public WallkickResult executeWallkick(int x, int y, int rtDir, int rtOld, int rtNew, boolean allowUpward, Piece piece, Field field, Controller ctrl) {
        final WallkickResult baseKick = getWallkickResult(x, y, rtDir, rtNew, piece, field, BASE_ROTATION_KICKS);
        if (baseKick != null) return baseKick;

        if (piece.id != Piece.PIECE_I) return null;

        final int[][] selectedKicksI = rtOld == Piece.DIRECTION_UP || rtOld == Piece.DIRECTION_DOWN ? CW_ROTATION_KICKS_I_PIECE_FLAT : CW_ROTATION_KICKS_I_PIECE_VERTICAL;
        return getWallkickResult(x, y, rtDir, rtNew, piece, field, selectedKicksI);
    }

    private WallkickResult getWallkickResult(int x, int y, int rtDir, int rtNew, Piece piece, Field field, int[][] selectedKicksI) {
        for (int[] basicTest : selectedKicksI) {
            int tX = rtDir == -1 ? -basicTest[0] : basicTest[0];
            int tY = basicTest[1];

            if (piece.big) {
                tX *= 2;
                tY *= 2;
            }

            if (!piece.checkCollision(x + tX, y + tY, rtNew, field)) {
                return new WallkickResult(x + tX, y + tY, rtNew);
            }
        }

        return null;
    }
}
