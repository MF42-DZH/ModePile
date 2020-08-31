package zeroxfc.nullpo.custom.wallkick;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.WallkickResult;
import mu.nu.nullpo.game.subsystem.wallkick.StandardWallkick;
import zeroxfc.nullpo.custom.libs.ArrayRandomiser;

public class SuperRandomWallkick extends StandardWallkick {
    /*
     * Wallkick
     */
    public WallkickResult executeWallkick( int x, int y, int rtDir, int rtOld, int rtNew, boolean allowUpward, Piece piece, Field field, Controller ctrl ) {
        int[][][] kicktable = getKickTable( x, y, rtDir, rtOld, rtNew, allowUpward, piece, field, ctrl );

        int[] arr = new int[ kicktable[ rtOld ].length ];
        for ( int i = 0; i < arr.length; i++ ) {
            arr[ i ] = i;
        }

        int v = 0;
        for ( int i : ctrl.buttonTime ) {
            v += i;
        }

        ArrayRandomiser randomiser = new ArrayRandomiser( rtOld + rtNew + piece.id + field.getHighestBlockY() + v );
        arr = randomiser.permute( arr );

        for ( int i : arr ) {
            int x2 = kicktable[ rtOld ][ i ][ 0 ];
            int y2 = kicktable[ rtOld ][ i ][ 1 ];

            if ( piece.big ) {
                x2 *= 2;
                y2 *= 2;
            }

            if ( ( y2 >= 0 ) || ( allowUpward ) ) {
                if ( !piece.checkCollision( x + x2, y + y2, rtNew, field ) ) {
                    return new WallkickResult( x2, y2, rtNew );
                }
            }
        }

        return null;
    }
}
