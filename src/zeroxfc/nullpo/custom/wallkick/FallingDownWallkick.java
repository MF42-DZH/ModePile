package zeroxfc.nullpo.custom.wallkick;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.WallkickResult;
import mu.nu.nullpo.game.subsystem.wallkick.Wallkick;

public class FallingDownWallkick implements Wallkick {
    private static final int[][][] T_KICK_TABLE_CCW = {
            { { -1, 0 }, { 0, 2 }, { -1, 2 } },  // 0 -> L
            { { 1, 0 } },  // L -> 2
            { { 1, 0 }, { 0, 1 }, { 1, 1 }, { 1, 3 } },  // 2 -> R
            { { -1, 0 }, { -1, 1 } }   // R -> 0
    };

    private static final int[][][] T_KICK_TABLE_CW = {
            { { 1, 0 }, { 0, 2, }, { 1, 2 } },  // 0 -> R
            { { -1, 0 } },  // R -> 2
            { { -1, 0 }, { 0, 1 }, { -1, 1 }, { -1, 3 } },  // 2 -> L
            { { 1, 0 }, { 1, 1 } }   // L -> 0
    };

    private static final int[][][] JL_KICK_TABLE_CCW = {
            { { 0, 0 }, { -1, 0 }, { 0, 2 }, { -1, 2 } },  // 0 -> L
            { { 0, 0 }, { 1, 0 } },  // L -> 2
            { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 }, { 0, 3 }, { 1, 3 } },  // 2 -> R
            { { 0, 0 }, { -1, 0 }, { -1, 1 } }   // R -> 0
    };

    private static final int[][][] JL_KICK_TABLE_CW = {
            { { 0, 0 }, { 1, 0 }, { 0, 2 }, { 1, 2 } },  // 0 -> R
            { { 0, 0 }, { -1, 0 } },  // R -> 2
            { { 0, 0 }, { -1, 0 }, { 0, 1 }, { -1, 1 }, { 0, 3 }, { -1, 3 } },  // 2 -> L
            { { 0, 0 }, { 1, 0 }, { 1, 1 } }   // L -> 0
    };

    private static final int[][][] Z_KICK_TABLE_CCW = {
            { { 0, 0 }, { -1, 0 }, { 0, 1 }, { -1, 1 }, { 0, 2 }, { -1, 2 }, { 0, 3 }, { -1, 3 } },  // 0 -> L
            { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } },  // L -> 2
            { { 0, 0 }, { -1, 0 }, { 0, 1 }, { -1, 1 }, { 0, 2 }, { -1, 2 }, { 0, 3 }, { -1, 3 } },  // 2 -> R
            { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } },  // R -> 0
    };

    private static final int[][][] Z_KICK_TABLE_CW = {
            { { 0, 0 }, { -1, 0 }, { 0, 1 }, { -1, 1 }, { 0, 2 }, { -1, 2 }, { 0, 3 }, { -1, 3 } },  // 0 -> R
            { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } },  // R -> 2
            { { 0, 0 }, { -1, 0 }, { 0, 1 }, { -1, 1 }, { 0, 2 }, { -1, 2 }, { 0, 3 }, { -1, 3 } },  // 2 -> L
            { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } }   // L -> 0
    };

    private static final int[][][] S_KICK_TABLE_CCW = {
            { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 }, { 0, 2 }, { 1, 2 }, { 0, 3 }, { 1, 3 } },  // 0 -> L
            { { 0, 0 }, { -1, 0 }, { 0, 1 }, { -1, 1 } },  // L -> 2
            { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 }, { 0, 2 }, { 1, 2 }, { 0, 3 }, { 1, 3 } },  // 2 -> R
            { { 0, 0 }, { -1, 0 }, { 0, 1 }, { -1, 1 } }   // R -> 0
    };

    private static final int[][][] S_KICK_TABLE_CW = {
            { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 }, { 0, 2 }, { 1, 2 }, { 0, 3 }, { 1, 3 } },  // 0 -> R
            { { 0, 0 }, { -1, 0 }, { 0, 1 }, { -1, 1 } },  // R -> 2
            { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 }, { 0, 2 }, { 1, 2 }, { 0, 3 }, { 1, 3 } },  // 2 -> L
            { { 0, 0 }, { -1, 0 }, { 0, 1 }, { -1, 1 } }   // L -> 0
    };

    private static final int[][] ORDER = { { 3, 2, 1, 0, 4, 5, 6 }, { 3, 4, 5, 6, 2, 1, 0 }, { 3, 2, 4, 1, 5, 0, 6 }, { 3, 4, 2, 5, 1, 6, 0 } };

    private static final int FLAG_FLEXIBLE = 0b01;
    private static final int FLAG_SPECIAL = 0b10;

    private static boolean check_flag( int num, int flag ) {
        return ( num & flag ) > 0;
    }

    /**
     * Execute a wallkick using Falling Down's wallkick system.
     *
     * @param x           X-coordinate
     * @param y           Y-coordinate
     * @param rtDir       Rotation button used (-1: left rotation, 1: right rotation, 2: 180-degree rotation)
     * @param rtOld       Direction before rotation
     * @param rtNew       Direction after rotation
     * @param allowUpward If true, upward wallkicks are allowed.
     * @param piece       Current piece
     * @param field       Current field
     * @param ctrl        Button input status (it may be null, when controlled by an AI)
     * @return WallkickResult object, or null if you don't want a kick
     */
    @Override
    public WallkickResult executeWallkick( int x, int y, int rtDir, int rtOld, int rtNew, boolean allowUpward, Piece piece, Field field, Controller ctrl ) {
        int multiplier = piece.big ? 2 : 1;
        int convRtOld, convRtNew;
        convRtOld = ( rtOld + 2 ) % 4;
        convRtNew = ( rtNew + 2 ) % 4;

        // There are 2 sections: Flexible and Special.
        // Flexible is procedural.
        // Special uses the tables defined above.

        // Set kick type used.
        int flags = 0;
        switch ( piece.id ) {
            case Piece.PIECE_I:
                flags |= FLAG_FLEXIBLE;
                break;
            case Piece.PIECE_O:
                break;
            default:
                flags |= FLAG_FLEXIBLE;
                flags |= FLAG_SPECIAL;
        }

        // FLEXIBLE KICK
        if ( check_flag( flags, FLAG_FLEXIBLE ) ) {
            int ordNum;
            if ( ctrl.isPress( Controller.BUTTON_LEFT ) ) ordNum = 0;
            else if ( ctrl.isPress( Controller.BUTTON_RIGHT ) ) ordNum = 1;
            else if ( rtDir != 1 ) ordNum = 2;
            else ordNum = 3;

            // Intersection testing environment setup.
            Field testField = new Field( 41, 41, 0 );
            int initX, initY;
            initX = 41 / 2;
            initY = 41 / 2;

            // New piece objects as to not mess up in-game piece.
            Piece testPieceOld, testPieceNew;
            testPieceOld = new Piece( piece );
            testPieceOld.big = false;
            testPieceNew = new Piece( piece );
            testPieceNew.big = false;

            testPieceNew.placeToField( initX, initY, rtNew, testField );
            for ( int yOffset = 0; yOffset < 4; yOffset++ ) {
                boolean[] dodge = { false, false, false, false, false, false, false };

                // Intersection testing
                for ( int xOffset = -3; xOffset < 4; xOffset++ ) {
                    if ( testPieceOld.checkCollision( initX - xOffset, initY - yOffset, rtOld, testField ) )
                        dodge[ xOffset + 3 ] = true;
                }

                // Blockage testing
                for ( int xOffset = -3; xOffset < 4; xOffset++ ) {
                    if ( piece.checkCollision( x + xOffset * multiplier, y + yOffset * multiplier, rtNew, field ) )
                        dodge[ xOffset + 3 ] = false;
                }

                // Valid kick testing
                for ( int i = 0; i < 7; i++ ) {
                    if ( dodge[ ORDER[ ordNum ][ i ] ] ) {
                        return new WallkickResult(
                                ( ORDER[ ordNum ][ i ] - 3 ) * multiplier,
                                yOffset * multiplier,
                                rtNew
                        );
                    }
                }
            }
        }

        // SPECIAL KICK
        if ( check_flag( flags, FLAG_SPECIAL ) ) {
            // Get piece kick table.
            int[][][] masterKickTable;
            switch ( piece.id ) {
                case Piece.PIECE_T:
                    masterKickTable = rtDir == 1 ? T_KICK_TABLE_CW : T_KICK_TABLE_CCW;
                    break;
                case Piece.PIECE_J:
                case Piece.PIECE_L:
                    masterKickTable = rtDir == 1 ? JL_KICK_TABLE_CW : JL_KICK_TABLE_CCW;
                    break;
                case Piece.PIECE_Z:
                    masterKickTable = rtDir == 1 ? Z_KICK_TABLE_CW : Z_KICK_TABLE_CCW;
                    break;
                case Piece.PIECE_S:
                    masterKickTable = rtDir == 1 ? S_KICK_TABLE_CW : S_KICK_TABLE_CCW;
                    break;
                default:
                    return null;
            }

            // Get specific rotation kick table.
            int[][] kicktable;
            int index = rtDir == 1 ? convRtOld : 3 - convRtNew;
            kicktable = masterKickTable[ index ];

            // Do kick tests
            for ( int[] test : kicktable ) {
                if ( !piece.checkCollision( x + test[ 0 ] * multiplier, y + test[ 1 ] * multiplier, rtNew, field ) )
                    return new WallkickResult( test[ 0 ] * multiplier, test[ 1 ] * multiplier, rtNew );
            }
        }

        return null;
    }
}
