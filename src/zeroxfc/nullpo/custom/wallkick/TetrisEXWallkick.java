package zeroxfc.nullpo.custom.wallkick;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.WallkickResult;
import mu.nu.nullpo.game.subsystem.wallkick.Wallkick;

public class TetrisEXWallkick implements Wallkick {
    // Mirror X to get CW table.
    private static final int[][] KickTableCCW = {
        { 0, 0 },
        { -1, 0 },
        { -1, 1 },
        { 0, 1 },
        { 1, 1 },
        { 1, 0 },
        { 0, -1 }
    };

    private static final int[][] KickTableCW = {
        { 0, 0 },
        { 1, 0 },
        { 1, 1 },
        { 0, 1 },
        { -1, 1 },
        { -1, 0 },
        { 0, -1 }
    };

    // Mirror X to get CW Right table.
    private static final int[][] KickTableLeftCCW = {
        { -1, 0 },
        { -1, 1 },
        { -2, 0 },
        { 0, 0 },
        { 0, 1 },
        { 0, -1 },
        { 1, 1 },
        { 1, 0 },
    };

    private static final int[][] KickTableRightCCW = {
        { 1, 0 },
        { 1, 1 },
        { 0, 1 },
        { 1, 2 },
        { 0, 0 },
        { 0, -1 },
        { 0, 1 },
        { -1, 1 },
    };

    // Mirror X to get CCW Right table.
    private static final int[][] KickTableLeftCW = {
        { -1, 0 },
        { -1, 1 },
        { 0, 1 },
        { -1, 2 },
        { 0, 0 },
        { 0, -1 },
        { 0, 1 },
        { 1, 1 },
    };

    private static final int[][] KickTableRightCW = {
        { 1, 0 },
        { 1, 1 },
        { 2, 0 },
        { 0, 0 },
        { 0, 1 },
        { 0, -1 },
        { -1, 1 },
        { -1, 0 },
    };

    public int currentDASCharge = 0;
    public int maxDASCharge = 0;
    public int dasDirection = 1;

    @Override
    public WallkickResult executeWallkick(int x, int y, int rtDir, int rtOld, int rtNew, boolean allowUpward,
                                          Piece piece, Field field, Controller ctrl) {
        int[][] table;

        /*
         * Note about the selection of wallkicks:
         *
         * then comes to the hard part: the condition to use the "moving" tables.
         * it's not simply "the moving button is pushed" (at least both may be pushed while in the DAS system only one can be functioning).
         * the condition should be "the DAS is functioning but blocked (by mino or wall) (in the direction it's functioning)", like saying "there's pressure against the wall."
         * this is some fundamental difference and the "failed-to-move" information from DAS system needs to be passed to the wallkick subsystem.
         *
         * > as of right now, there is no implemented way of passing DAS information to the wallkick system, so button press will have to do for now.
         * > (kinda scared to modify the vanilla code directly; there might be a way to do it via reflection / mid-processing casting though)
         *
         * !! ignore the lines with an > above, the new system with mid-processing casting works.
         */

    	/*
    	 * LEGACY:
    	 *
    	if (rtDir < 0 || rtDir == 2) {
			if (ctrl.isPress(Controller.BUTTON_LEFT) && (currentDASCharge >= maxDASCharge)) {
				table = KickTableLeftCCW;
			} else if (ctrl.isPress(Controller.BUTTON_RIGHT) && (currentDASCharge >= maxDASCharge)) {
				table = KickTableRightCCW;
			} else {
				table = KickTableCCW;
			}
		} else {
			if (ctrl.isPress(Controller.BUTTON_LEFT) && (currentDASCharge >= maxDASCharge)) {
				table = KickTableLeftCW;
			} else if (ctrl.isPress(Controller.BUTTON_RIGHT) && (currentDASCharge >= maxDASCharge)) {
				table = KickTableRightCW;
			} else {
				table = KickTableCW;
			}
		}
    	 */

        if (rtDir < 0 || rtDir == 2) {
            if (dasDirection == -1 && (currentDASCharge >= maxDASCharge)) {
                table = KickTableLeftCCW;
            } else if (dasDirection == 1 && (currentDASCharge >= maxDASCharge)) {
                table = KickTableRightCCW;
            } else {
                table = KickTableCCW;
            }
        } else {
            if (dasDirection == -1 && (currentDASCharge >= maxDASCharge)) {
                table = KickTableLeftCW;
            } else if (dasDirection == 1 && (currentDASCharge >= maxDASCharge)) {
                table = KickTableRightCW;
            } else {
                table = KickTableCW;
            }
        }

        currentDASCharge = 0;
        dasDirection = 0;
        maxDASCharge = 1;

        for (int[] ints : table) {
            int x2 = ints[0];
            int y2 = ints[1];

            if (piece.big) {
                x2 *= 2;
                y2 *= 2;
            }

            if (!piece.checkCollision(x + x2, y + y2, rtNew, field)) {
                return new WallkickResult(x2, y2, rtNew);
            }
        }

        return null;
    }
}
