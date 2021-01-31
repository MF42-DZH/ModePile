package zeroxfc.nullpo.custom.modes.objects.expressshipping;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Field;

public class Boards {
    // Static fields here.
    // Sizes = 12x6, 6x12, 12x10, 10x12
    // Place 0 for an empty space, 1 for an already-placed block.

    public static final int[][][] Boards = new int[][][] {
        // region VerticalFields
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 1, 1, 0, 0, 1, 1 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 1, 1, 0, 0 },  // Row  5
            new int[] { 0, 0, 1, 1, 0, 0 },  // Row  6
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  7
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  8
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  9
            new int[] { 1, 1, 0, 0, 1, 1 },  // Row 10
            new int[] { 0, 0, 0, 0, 0, 0 }   // Row 11
        },  // 6x12
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 1, 1, 0, 0 },  // Row  4
            new int[] { 0, 1, 0, 0, 1, 0 },  // Row  5
            new int[] { 0, 1, 0, 0, 1, 0 },  // Row  6
            new int[] { 0, 0, 1, 1, 0, 0 },  // Row  7
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  8
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  9
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row 10
            new int[] { 0, 0, 0, 0, 0, 0 }   // Row 11
        },  // 6x12
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 1, 1, 0, 0 },  // Row  5
            new int[] { 0, 0, 1, 1, 0, 0 },  // Row  6
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  7
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  8
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  9
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row 10
            new int[] { 0, 0, 0, 0, 0, 0 }   // Row 11
        },  // 6x12
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 1, 0, 0, 0, 0, 1 },  // Row  1
            new int[] { 1, 0, 0, 0, 0, 1 },  // Row  2
            new int[] { 1, 0, 0, 0, 0, 1 },  // Row  3
            new int[] { 1, 0, 0, 0, 0, 1 },  // Row  4
            new int[] { 0, 0, 1, 1, 0, 0 },  // Row  5
            new int[] { 0, 0, 1, 1, 0, 0 },  // Row  6
            new int[] { 1, 0, 0, 0, 0, 1 },  // Row  7
            new int[] { 1, 0, 0, 0, 0, 1 },  // Row  8
            new int[] { 1, 0, 0, 0, 0, 1 },  // Row  9
            new int[] { 1, 0, 0, 0, 0, 1 },  // Row 10
            new int[] { 0, 0, 0, 0, 0, 0 }   // Row 11
        },  // 6x12
        new int[][] {
            new int[] { 0, 0, 1, 1, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 1, 0, 0, 0, 0, 1 },  // Row  4
            new int[] { 1, 0, 0, 0, 0, 1 },  // Row  5
            new int[] { 1, 0, 0, 0, 0, 1 },  // Row  6
            new int[] { 1, 0, 0, 0, 0, 1 },  // Row  7
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  8
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  9
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row 10
            new int[] { 0, 0, 1, 1, 0, 0 }   // Row 11
        },  // 6x12
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 1, 0, 0, 1, 0 },  // Row  3
            new int[] { 0, 1, 0, 0, 1, 0 },  // Row  4
            new int[] { 0, 1, 0, 0, 1, 0 },  // Row  5
            new int[] { 0, 1, 0, 0, 1, 0 },  // Row  6
            new int[] { 0, 1, 0, 0, 1, 0 },  // Row  7
            new int[] { 0, 1, 0, 0, 1, 0 },  // Row  8
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  9
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row 10
            new int[] { 0, 0, 0, 0, 0, 0 }   // Row 11
        },  // 6x12
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 1, 0, 0, 0, 0, 1 },  // Row  1
            new int[] { 0, 1, 0, 0, 1, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 1, 1, 0, 0 },  // Row  4
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  5
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  6
            new int[] { 0, 0, 1, 1, 0, 0 },  // Row  7
            new int[] { 0, 0, 0, 0, 0, 0 },  // Row  8
            new int[] { 0, 1, 0, 0, 1, 0 },  // Row  9
            new int[] { 1, 0, 0, 0, 0, 1 },  // Row 10
            new int[] { 0, 0, 0, 0, 0, 0 }   // Row 11
        },  // 6x12
        // endregion VerticalFields
        // region HorizontalFields
        new int[][] {
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  0
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  4
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 }   // Row  5
        },  // 12x6
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }   // Row  5
        },  // 12x6
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }   // Row  5
        },  // 12x6
        new int[][] {
            new int[] { 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0 },  // Row  0
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0 }   // Row  5
        },  // 12x6
        new int[][] {
            new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  1
            new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },  // Row  2
            new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },  // Row  3
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 }   // Row  5
        },  // 12x6
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }   // Row  5
        },  // 12x6
        new int[][] {
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  0
            new int[] { 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0 },  // Row  4
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 }   // Row  5
        },  // 12x6
        // endregion HorizontalFields
        // region JumboVerticalFields
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 },  // Row  5
            new int[] { 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 },  // Row  6
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  7
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  8
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  9
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row 10
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }   // Row 11
        },  // 10x12
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 0, 1, 0, 0, 1, 0, 0, 0 },  // Row  5
            new int[] { 0, 0, 0, 1, 0, 0, 1, 0, 0, 0 },  // Row  6
            new int[] { 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 },  // Row  7
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  8
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  9
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row 10
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }   // Row 11
        },  // 10x12
        new int[][] {
            new int[] { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 },  // Row  0
            new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },  // Row  1
            new int[] { 0, 0, 0, 1, 1, 1, 1, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 1, 0, 0, 0, 0, 1, 0, 0 },  // Row  5
            new int[] { 0, 0, 1, 0, 0, 0, 0, 1, 0, 0 },  // Row  6
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  7
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  8
            new int[] { 0, 0, 0, 1, 1, 1, 1, 0, 0, 0 },  // Row  9
            new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },  // Row 10
            new int[] { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 }   // Row 11
        },  // 10x12
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  5
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  6
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  7
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  8
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  9
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row 10
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }   // Row 11
        },  // 10x12
        new int[][] {
            new int[] { 0, 0, 0, 1, 1, 1, 1, 0, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },  // Row  4
            new int[] { 1, 0, 0, 0, 1, 1, 0, 0, 0, 1 },  // Row  5
            new int[] { 1, 0, 0, 0, 1, 1, 0, 0, 0, 1 },  // Row  6
            new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },  // Row  7
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  8
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  9
            new int[] { 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 },  // Row 10
            new int[] { 0, 0, 0, 1, 1, 1, 1, 0, 0, 0 }   // Row 11
        },  // 10x12
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 1, 1, 1, 0, 0, 1, 1, 1, 0 },  // Row  1
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  2
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  3
            new int[] { 0, 1, 0, 1, 1, 1, 1, 0, 1, 0 },  // Row  4
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  5
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  6
            new int[] { 0, 1, 0, 1, 1, 1, 1, 0, 1, 0 },  // Row  7
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  8
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  9
            new int[] { 0, 1, 1, 1, 0, 0, 1, 1, 1, 0 },  // Row 10
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }   // Row 11
        },  // 10x12
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 1, 1, 1, 1, 0, 0, 0 },  // Row  1
            new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },  // Row  2
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  3
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 0, 1, 0, 0, 1, 0, 0, 0 },  // Row  5
            new int[] { 0, 0, 0, 1, 0, 0, 1, 0, 0, 0 },  // Row  6
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  7
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  8
            new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },  // Row  9
            new int[] { 0, 0, 0, 1, 1, 1, 1, 0, 0, 0 },  // Row 10
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }   // Row 11
        },  // 10x12
        // endregion JumboVerticalFields
        // region JumboHorizontalFields
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0 },  // Row  5
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  6
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  7
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  8
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }   // Row  9
        },  // 12x10
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0 },  // Row  5
            new int[] { 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0 },  // Row  6
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  7
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  8
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }   // Row  9
        },  // 12x10
        new int[][] {
            new int[] { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 },  // Row  0
            new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0 },  // Row  3
            new int[] { 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0 },  // Row  4
            new int[] { 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0 },  // Row  5
            new int[] { 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0 },  // Row  6
            new int[] { 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0 },  // Row  7
            new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },  // Row  8
            new int[] { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }   // Row  9
        },  // 12x10
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  3
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  5
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  6
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  7
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  8
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }   // Row  9
        },  // 12x10
        new int[][] {
            new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },  // Row  3
            new int[] { 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1 },  // Row  4
            new int[] { 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1 },  // Row  5
            new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },  // Row  6
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  7
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  8
            new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 }   // Row  9
        },  // 12x10
        new int[][] {
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  0
            new int[] { 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0 },  // Row  1
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  2
            new int[] { 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0 },  // Row  3
            new int[] { 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0 },  // Row  4
            new int[] { 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0 },  // Row  5
            new int[] { 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0 },  // Row  6
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  7
            new int[] { 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0 },  // Row  8
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }   // Row  9
        },  // 12x10
        new int[][] {
            new int[] { 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0 },  // Row  0
            new int[] { 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0 },  // Row  1
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  2
            new int[] { 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0 },  // Row  3
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  4
            new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },  // Row  5
            new int[] { 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0 },  // Row  6
            new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },  // Row  7
            new int[] { 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0 },  // Row  8
            new int[] { 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0 }   // Row  9
        }   // 12x10
        // endregion JumboHorizontalFields
    };

    public static Field getBoard(int index, int skin) {
        int dimY = Boards[index].length;
        int dimX = Boards[index][0].length;

        Field localField = new Field(dimX, dimY, 0, true);

        for (int y = 0; y < dimY; y++) {
            for (int x = 0; x < dimX; x++) {
                localField.getBlock(x, y).copy(new Block(Boards[index][y][x], skin));
                localField.getBlock(x, y).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, true);
            }
        }

        localField.setAllAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
        localField.setAllAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);

        return localField;
    }
}
