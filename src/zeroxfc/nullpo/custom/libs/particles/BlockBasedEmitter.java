package zeroxfc.nullpo.custom.libs.particles;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Piece;

public interface BlockBasedEmitter {
    default boolean checkLowestBlock(Piece piece, int blockIndex) {
        if (blockIndex > piece.getMaxBlock() || blockIndex < 0) return false;

        for (int i = 0; i < piece.getMaxBlock(); ++i) {
            if (i == blockIndex) continue;
            if (piece.dataX[piece.direction][i] == piece.dataX[piece.direction][blockIndex] && piece.dataY[piece.direction][i] > piece.dataY[piece.direction][blockIndex]) return false;
        }

        return true;
    }

    default int[] getColorForBlock(Block block) {
        int red, green, blue;

        switch (block.color) {
            case Block.BLOCK_COLOR_RED:
            case Block.BLOCK_COLOR_GEM_RED:
                red = 255;
                green = 32;
                blue = 32;
                break;
            case Block.BLOCK_COLOR_ORANGE:
            case Block.BLOCK_COLOR_GEM_ORANGE:
                red = 255;
                green = 128;
                blue = 0;
                break;
            case Block.BLOCK_COLOR_SQUARE_GOLD_1:
            case Block.BLOCK_COLOR_SQUARE_GOLD_2:
            case Block.BLOCK_COLOR_SQUARE_GOLD_3:
            case Block.BLOCK_COLOR_SQUARE_GOLD_4:
            case Block.BLOCK_COLOR_SQUARE_GOLD_5:
            case Block.BLOCK_COLOR_SQUARE_GOLD_6:
            case Block.BLOCK_COLOR_SQUARE_GOLD_7:
            case Block.BLOCK_COLOR_SQUARE_GOLD_8:
            case Block.BLOCK_COLOR_SQUARE_GOLD_9:
            case Block.BLOCK_COLOR_YELLOW:
            case Block.BLOCK_COLOR_GEM_YELLOW:
                red = 255;
                green = 255;
                blue = 0;
                break;
            case Block.BLOCK_COLOR_GREEN:
            case Block.BLOCK_COLOR_GEM_GREEN:
                red = 32;
                green = 255;
                blue = 32;
                break;
            case Block.BLOCK_COLOR_CYAN:
            case Block.BLOCK_COLOR_GEM_CYAN:
                red = 0;
                green = 255;
                blue = 255;
                break;
            case Block.BLOCK_COLOR_BLUE:
            case Block.BLOCK_COLOR_GEM_BLUE:
                red = 32;
                green = 32;
                blue = 255;
                break;
            case Block.BLOCK_COLOR_PURPLE:
            case Block.BLOCK_COLOR_GEM_PURPLE:
                red = 128;
                green = 32;
                blue = 255;
                break;
            default:
                red = 255;
                green = 255;
                blue = 255;
                break;
        }

        return new int[] { red, green, blue };
    }
}
