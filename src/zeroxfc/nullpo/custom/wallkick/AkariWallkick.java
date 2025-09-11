package zeroxfc.nullpo.custom.wallkick;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.WallkickResult;
import mu.nu.nullpo.game.subsystem.wallkick.Wallkick;

// The name of this wallkick system is dual-layered wordplay.
// Its aim is to resolve some "annoyances" (personal) that I have with TGM ARS.

// Specifically:
//   - The I-piece should be able to floorkick when it's not touching the floor.
//   - The I-piece should be able to wallkick when it's not touching the wall.
//   - The centre column rule is tweaked to allow ONE of the two kicks in certain
//     situations.
//   - The kicks are symmetric (1R, 1L for CCW, 1L, 1R for CW) for non-I-pieces.
//   - If there are no more floorkicks allowed for T or I, just prevent the rotation
//     completely instead of allowing the rotation and then insta-locking the piece
//     (see TGM4 for an example).

public class AkariWallkick implements Wallkick {
    private static final int[][] KICKS_CCW = { { 0, 0 }, { 1, 0 }, { -1, 0 }, { 0, -1 } };
    private static final int[][] KICKS_CW = { { 0, 0 }, { -1, 0 }, { 1, 0 }, { 0, -1 } };
    private static final int[][] KICKS_180 = { { 0, 0 } };
    private static final int[][] KICKS_I_HORIZ = { { 0, 0 }, { 0, -1 }, { 0, -2 } };
    private static final int[][] KICKS_I_VERTI = { { 0, 0 }, { -1, 0 }, { 1, 0 }, { 2, 0 } };

    private boolean fieldIsSolidAt(Field field, int x, int y) {
        if (x >= field.getWidth() || x < 0) {
            return true;
        }

        if (y >= field.getHeight()) {
            return true;
        }

        if (field.getCoordAttribute(x, y) == Field.COORD_WALL) {
            return true;
        }

        if ((field.getCoordAttribute(x, y) != Field.COORD_VANISH) && (field.getBlockColor(x, y) != Block.BLOCK_COLOR_NONE)) {
            return true;
        }

        return false;
    }

    private boolean fieldBlockedFor(Field field, int x, int y, boolean big) {
        if (big) return fieldIsSolidAt(field, x, y) || fieldIsSolidAt(field, x + 1, y) || fieldIsSolidAt(field, x, y + 1) || fieldIsSolidAt(field, x + 1, y + 1);
        else return fieldIsSolidAt(field, x, y);
    }

    private boolean performCheckCentreColumn(Piece piece, Field field, int x, int y, int rtNew) {
        for (int i = 0; i < piece.getMaxBlock(); ++i) {
            final int x2 = x + piece.dataX[rtNew][i];
            final int y2 = y + piece.dataY[rtNew][i];

            if (fieldBlockedFor(field, x2, y2, piece.big)) {
                if (piece.dataX[rtNew][i] == 1) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public WallkickResult executeWallkick(int x, int y, int rtDir, int rtOld, int rtNew, boolean allowUpward, Piece piece, Field field, Controller ctrl) {
        final int multiplier = piece.big ? 2 : 1;

        if (piece.id == Piece.PIECE_I) {
            final int[][] kicks = (rtOld == Piece.DIRECTION_UP || rtOld == Piece.DIRECTION_DOWN) ? KICKS_I_HORIZ : KICKS_I_VERTI;

            final boolean allowUpwardKick = allowUpward && (rtNew == Piece.DIRECTION_RIGHT || rtNew == Piece.DIRECTION_LEFT);
            for (final int[] kick : kicks) {
                final WallkickResult result = new WallkickResult(kick[0] * multiplier, kick[1] * multiplier, rtNew);
                if (!piece.checkCollision(x + (kick[0] * multiplier), y + (kick[1] * multiplier), rtNew, field) && (!result.isUpward() || allowUpwardKick)) return result;
            }
        } else {
            final int[][] kicks = (rtDir == 1) ? KICKS_CW : ((rtDir == 2) ? KICKS_180 : KICKS_CCW);

            final boolean allowUpwardKick = allowUpward && piece.id == Piece.PIECE_T && rtNew == Piece.DIRECTION_UP;
            final boolean checkCentreColumn = (piece.id == Piece.PIECE_T || piece.id == Piece.PIECE_L || piece.id == Piece.PIECE_J)
                && (rtOld == Piece.DIRECTION_UP || rtOld == Piece.DIRECTION_DOWN)
                && rtDir != 0
                && rtDir != 2;

            final boolean centreColumnOccupied = checkCentreColumn && performCheckCentreColumn(piece, field, x, y, rtNew);

            for (final int[] kick : kicks) {
                final WallkickResult result = new WallkickResult(kick[0] * multiplier, kick[1] * multiplier, rtNew);
                if (!piece.checkCollision(x + (kick[0] * multiplier), y + (kick[1] * multiplier), rtNew, field) && (!result.isUpward() || allowUpwardKick)) {
                    if (!checkCentreColumn || !centreColumnOccupied) return result;
                    else if (piece.id == Piece.PIECE_T) {
                        // For T:
                        //   - only allow left kick for 0 -> R or 2 -> L rotations
                        //   - only allow right kick for 0 -> L or 2 -> R rotations
                        if ((kick[0] < 0 && ((rtOld == Piece.DIRECTION_UP && rtNew == Piece.DIRECTION_RIGHT) || (rtOld == Piece.DIRECTION_DOWN && rtNew == Piece.DIRECTION_LEFT)))
                            || (kick[0] > 0 && ((rtOld == Piece.DIRECTION_UP && rtNew == Piece.DIRECTION_LEFT) || (rtOld == Piece.DIRECTION_DOWN && rtNew == Piece.DIRECTION_RIGHT)))) {
                            return result;
                        }
                    } else if (piece.id == Piece.PIECE_L || piece.id == Piece.PIECE_J) {
                        // For L & J:
                        //   - only allow right kick for 0 -> rotations
                        //   - only allow left kick for 2 -> rotations
                        if ((rtOld == Piece.DIRECTION_UP && kick[0] > 0) || (rtOld == Piece.DIRECTION_DOWN && kick[0] < 0)) {
                            return result;
                        }
                    }
                }
            }
        }

        return null;
    }
}
