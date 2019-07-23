package zeroxfc.nullpo.custom.wallkick;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.WallkickResult;
import mu.nu.nullpo.game.subsystem.wallkick.Wallkick;

public class TetrisEXWallkick implements Wallkick {
	// Mirror X to get CW table.
    private static final int[][] KickTableCCW = {
        {0, 0},
        {-1, 0},
        {-1, 1},
        {0, 1},
        {1, 1},
        {1, 0},
        {0, -1}
    };
    
    private static final int[][] KickTableCW = {
        {0, 0},
        {-1, 0},
        {-1, 1},
        {0, 1},
        {1, 1},
        {1, 0},
        {0, -1}
    };

    // Mirror X to get CW Right table.
    private static final int[][] KickTableLeftCCW = {
        {-1, 0},
        {-1, 1},
        {-2, 0},
        {0, 0},
        {0, 1},
        {0, -1},
        {1, 1},
        {1, 0},
    };
    
    private static final int[][] KickTableRightCCW = {
        {1, 0},
        {1, 1},
        {0, 1},
        {1, 2},
        {0, 0},
        {0, -1},
        {0, 1},
        {-1, 1},
    };

    // Mirror X to get CCW Right table.
    private static final int[][] KickTableLeftCW = {
        {-1, 0},
        {-1, 1},
        {0, 1},
        {-1, 2},
        {0, 0},
        {0, -1},
        {0, 1},
        {1, 1},
    };
    
    private static final int[][] KickTableRightCW = {
        {1, 0},
        {1, 1},
        {2, 0},
        {0, 0},
        {0, 1},
        {0, -1},
        {-1, 1},
        {-1, 0},
    };

    @Override
    public WallkickResult executeWallkick(int x, int y, int rtDir, int rtOld, int rtNew, boolean allowUpward,
    		Piece piece, Field field, Controller ctrl) {
    	int[][] table;
    	
    	if (rtDir < 0 || rtDir == 2) {
			if (ctrl.isPress(Controller.BUTTON_LEFT)) {
				table = KickTableLeftCCW;
			} else if (ctrl.isPress(Controller.BUTTON_RIGHT)) {
				table = KickTableRightCCW;
			} else {
				table = KickTableCCW;
			}
		} else {
			if (ctrl.isPress(Controller.BUTTON_LEFT)) {
				table = KickTableLeftCW;
			} else if (ctrl.isPress(Controller.BUTTON_RIGHT)) {
				table = KickTableRightCW;
			} else {
				table = KickTableCW;
			}
		}
    	
    	for (int i = 0; i < table.length; i++) {
			int x2 = table[i][0];
			int y2 = table[i][1];
			
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
